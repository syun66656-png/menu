package kr.scfarm.menu.menu;

import kr.scfarm.menu.button.ButtonType;
import kr.scfarm.menu.button.handler.ButtonHandler;
import kr.scfarm.menu.button.handler.CloseHandler;
import kr.scfarm.menu.button.handler.CommandHandler;
import kr.scfarm.menu.button.handler.MessageHandler;
import kr.scfarm.menu.button.handler.OpenHandler;
import kr.scfarm.menu.button.handler.WarpHandler;
import kr.scfarm.menu.config.ButtonDefinition;
import kr.scfarm.menu.config.MenuDefinition;
import kr.scfarm.menu.config.MenuLoader;
import kr.scfarm.menu.config.PluginConfig;
import kr.scfarm.menu.config.Requirement;
import kr.scfarm.menu.cooldown.CooldownManager;
import kr.scfarm.menu.integration.WarpBridge;
import kr.scfarm.menu.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

/** 메뉴 레지스트리 + 오픈/쿨다운/버튼 실행의 중심(§10). */
public final class MenuManager {

    private final Plugin plugin;
    private final MenuLoader menuLoader;
    private final CooldownManager cooldownManager = new CooldownManager();

    private PluginConfig config;
    private Map<String, MenuDefinition> menus;
    private WarpBridge warpBridge;
    private Map<ButtonType, ButtonHandler> handlers;
    private Runnable postReload;

    public MenuManager(Plugin plugin) {
        this.plugin = plugin;
        this.menuLoader = new MenuLoader(plugin);
    }

    /** 최초 로드 및 리로드 공통 경로: config + menus 재로딩, 핸들러/브리지 재구성. */
    public void reload() {
        // 열려 있는 우리 GUI 를 모두 닫는다 — 리로드 이전 정의(MenuView)가
        // 계속 클릭을 처리하는 상태를 남기지 않기 위함.
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) {
                online.closeInventory();
            }
        }

        plugin.reloadConfig();
        this.config = PluginConfig.from(plugin.getConfig());
        this.menus = menuLoader.loadAll();
        this.warpBridge = new WarpBridge(config);
        this.handlers = buildHandlers();
        if (postReload != null) {
            postReload.run();
        }
    }

    /** 리로드 후 실행할 콜백(명령어 재등록 등). */
    public void onReload(Runnable callback) {
        this.postReload = callback;
    }

    private Map<ButtonType, ButtonHandler> buildHandlers() {
        Map<ButtonType, ButtonHandler> map = new EnumMap<>(ButtonType.class);
        map.put(ButtonType.WARP, new WarpHandler(warpBridge));
        map.put(ButtonType.COMMAND, new CommandHandler());
        map.put(ButtonType.MESSAGE, new MessageHandler());
        map.put(ButtonType.OPEN, new OpenHandler(this));
        map.put(ButtonType.CLOSE, new CloseHandler());
        return map;
    }

    public PluginConfig config() {
        return config;
    }

    public Map<String, MenuDefinition> menus() {
        return menus;
    }

    public MenuDefinition menu(String id) {
        return menus.get(id);
    }

    /**
     * 메뉴를 연다.
     *
     * @param bypassCooldown open 타입 전환처럼 쿨다운을 무시할지
     * @return 실제로 열렸으면 true
     */
    public boolean open(Player player, String menuId, boolean bypassCooldown) {
        MenuDefinition definition = menus.get(menuId);
        if (definition == null) {
            return false;
        }

        // open-permission 검사.
        String openPermission = definition.openPermission();
        if (openPermission != null && !openPermission.isBlank() && !player.hasPermission(openPermission)) {
            sendMessage(player, "no-permission", Map.of());
            return false;
        }

        // 쿨다운 검사(§9-1).
        if (!bypassCooldown && config.cooldownEnabled()) {
            long millis = config.cooldownSeconds() * 1000L;
            long remaining = cooldownManager.tryConsume(player.getUniqueId(), millis);
            if (remaining > 0) {
                // 경고는 쿨다운 사이클당 1회만 — 연타해도 메시지가 반복 출력되지 않는다.
                if (cooldownManager.shouldWarn(player.getUniqueId())) {
                    sendMessage(player, "cooldown", Map.of("seconds", String.valueOf(config.cooldownSeconds())));
                }
                return false;
            }
        }

        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, definition.size(), definition.titleComponent());
        MenuView view = new MenuView(definition, inventory);
        holder.attach(view);

        render(player, definition, view);

        player.openInventory(inventory);
        if (definition.openSound() != null) {
            definition.openSound().play(player);
        }
        return true;
    }

    /** 필러 + 버튼을 view-requirement 를 반영해 배치. */
    private void render(Player player, MenuDefinition definition, MenuView view) {
        Inventory inventory = view.inventory();
        int size = definition.size();

        // 필러 먼저(버튼이 덮어씀).
        if (definition.fillerEnabled() && definition.fillerItem() != null) {
            ItemStack filler = definition.fillerItem().build();
            for (int slot = 0; slot < size; slot++) {
                inventory.setItem(slot, filler);
            }
        }

        for (ButtonDefinition button : definition.buttons().values()) {
            Requirement viewReq = button.viewRequirement();
            boolean visible = viewReq == null || viewReq.test(player);

            if (visible) {
                ItemStack item = button.item().build();
                for (int slot : button.slots()) {
                    if (slot < 0 || slot >= size) {
                        continue;
                    }
                    inventory.setItem(slot, item);
                    view.map(slot, button);
                }
            } else if (viewReq.onFail() == Requirement.OnFail.LOCKED && viewReq.lockedItem() != null) {
                // 잠금 아이템 표시(클릭 매핑 없음 → 클릭해도 동작 없음).
                ItemStack locked = viewReq.lockedItem().build();
                for (int slot : button.slots()) {
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, locked);
                    }
                }
            }
            // HIDE: 아무것도 배치하지 않음.
        }
    }

    /** 클릭된 버튼 실행(click-requirement 검사 포함). */
    public void execute(Player player, ButtonDefinition button) {
        Requirement clickReq = button.clickRequirement();
        if (clickReq != null && !clickReq.test(player)) {
            String deny = clickReq.denyMessage();
            if (deny != null && !deny.isBlank()) {
                player.sendMessage(Text.mm(deny));
            }
            return;
        }

        if (button.sound() != null) {
            button.sound().play(player);
        }

        // 모든 버튼은 클릭 즉시 GUI 를 닫는다(타입 무관).
        // 이벤트가 항상 취소되어 커서 아이템이 없으므로 동기 close 는 안전하고,
        // 창이 바로 닫혀 같은 틱 더블클릭으로 인한 동작 이중 실행도 차단된다.
        player.closeInventory();

        ButtonHandler handler = handlers.get(button.type());
        if (handler == null) {
            return;
        }

        // 명령을 실행하는 타입(warp/command)은 GUI 닫힘 후 0.5초(10틱) 뒤 실행,
        // 나머지(message/open/close)는 다음 틱 실행.
        // → 명령이 다른 GUI 를 여는 경우의 인벤토리 꼬임/충돌 방지.
        long delayTicks = (button.type() == ButtonType.WARP || button.type() == ButtonType.COMMAND)
                ? 10L : 1L;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            handler.handle(player, button);
        }, delayTicks);
    }

    public CooldownManager cooldownManager() {
        return cooldownManager;
    }

    private void sendMessage(Player player, String key, Map<String, String> placeholders) {
        String raw = config.message(key);
        if (raw != null && !raw.isBlank()) {
            player.sendMessage(Text.mm(raw, placeholders));
        }
    }
}
