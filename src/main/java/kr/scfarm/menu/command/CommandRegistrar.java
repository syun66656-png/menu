package kr.scfarm.menu.command;

import kr.scfarm.menu.config.MenuDefinition;
import kr.scfarm.menu.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * open-commands / reload.command 를 plugin.yml 수정 없이 런타임 동적 등록/해제한다(§9-1, §11).
 * CommandMap 은 리플렉션으로 접근(버전 간 안전).
 */
public final class CommandRegistrar {

    private static final String FALLBACK_PREFIX = "menuplugin";

    private final Plugin plugin;
    private final MenuManager menuManager;
    private final List<Command> registered = new ArrayList<>();

    public CommandRegistrar(Plugin plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    /** 기존 등록 해제 후 현재 config 기준으로 재등록. */
    public void register() {
        CommandMap commandMap = commandMap();
        if (commandMap == null) {
            plugin.getLogger().warning("CommandMap 을 찾지 못해 명령어를 등록하지 못했습니다.");
            return;
        }
        unregister(commandMap);

        // 메뉴별 open-commands.
        for (MenuDefinition menu : menuManager.menus().values()) {
            List<String> openCommands = menu.openCommands();
            if (openCommands == null || openCommands.isEmpty()) {
                continue;
            }
            String name = openCommands.get(0);
            List<String> aliases = openCommands.size() > 1
                    ? new ArrayList<>(openCommands.subList(1, openCommands.size()))
                    : List.of();
            MenuOpenCommand command = new MenuOpenCommand(name, aliases, menuManager, menu.id());
            commandMap.register(FALLBACK_PREFIX, command);
            registered.add(command);
        }

        // 리로드 명령어.
        String reloadName = menuManager.config().reloadCommand();
        if (reloadName != null && !reloadName.isBlank()) {
            ReloadCommand reloadCommand = new ReloadCommand(reloadName, menuManager,
                    menuManager.config().reloadPermission());
            commandMap.register(FALLBACK_PREFIX, reloadCommand);
            registered.add(reloadCommand);
        }

        // 클라이언트 탭완성/명령 트리에 반영.
        syncCommands();
    }

    /** 등록했던 명령어를 모두 해제(리로드/비활성 시). */
    public void unregister(CommandMap commandMap) {
        if (commandMap == null) {
            commandMap = commandMap();
        }
        if (commandMap == null || registered.isEmpty()) {
            registered.clear();
            return;
        }
        Map<String, Command> known = knownCommands(commandMap);
        for (Command command : registered) {
            command.unregister(commandMap);
        }
        // 라벨이 아니라 "우리가 등록한 인스턴스"만 제거한다.
        // (다른 플러그인과 이름이 겹쳐도 그 명령어를 지우지 않기 위함)
        if (known != null) {
            known.values().removeIf(registered::contains);
        }
        registered.clear();
    }

    private void syncCommands() {
        try {
            Method sync = Bukkit.getServer().getClass().getMethod("syncCommands");
            sync.invoke(Bukkit.getServer());
        } catch (Throwable ignored) {
            // syncCommands 미지원 서버면 무시(실행에는 영향 없음).
        }
    }

    private CommandMap commandMap() {
        try {
            Method getCommandMap = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object map = getCommandMap.invoke(Bukkit.getServer());
            if (map instanceof CommandMap commandMap) {
                return commandMap;
            }
        } catch (Throwable ex) {
            plugin.getLogger().warning("CommandMap 접근 실패: " + ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> knownCommands(CommandMap commandMap) {
        try {
            Method getKnown = commandMap.getClass().getMethod("getKnownCommands");
            Object known = getKnown.invoke(commandMap);
            if (known instanceof Map<?, ?> map) {
                return (Map<String, Command>) map;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
