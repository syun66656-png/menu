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
import net.kyori.adventure.text.Component;
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
                sendMessage(player, "cooldown", Map.of("seconds", String.valueOf(config.cooldownSeconds())));
                return false;
            }
        }

        MenuHolder holder = new MenuHolder();
        Component title = Text.mm(definition.title());
        Inventory inventory = Bukkit.createInventory(holder, definition.size(), title);
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

        // close-on-click(기본 true): open 타입은 새 메뉴가 대체하므로 강제 닫지 않음.
        if (button.closeOnClick() && button.type() != ButtonType.OPEN) {
            player.closeInventory();
        }

        ButtonHandler handler = handlers.get(button.type());
        if (handler != null) {
            handler.handle(player, button);
        }
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
