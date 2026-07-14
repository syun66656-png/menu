package kr.scfarm.menu;

import kr.scfarm.menu.command.CommandRegistrar;
import kr.scfarm.menu.listener.MenuClickListener;
import kr.scfarm.menu.listener.SneakSwapListener;
import kr.scfarm.menu.menu.MenuManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MenuPlugin 진입점(§10). 완전 config 기반 메뉴 — 코드는 config 를 "실행"만 한다.
 * DB 미사용(이동은 WarpCraft 위임) → 콘솔 로드 메시지는 메인 줄만 출력(§9-4).
 */
public final class MenuPlugin extends JavaPlugin {

    private MenuManager menuManager;
    private CommandRegistrar commandRegistrar;

    @Override
    public void onEnable() {
        MiniMessage mm = MiniMessage.miniMessage();
        try {
            // 기본 리소스 배포.
            saveDefaultConfig();

            // 매니저 초기화 (config + menus 로드).
            menuManager = new MenuManager(this);
            menuManager.reload();

            // 명령어 동적 등록 + 리로드 시 재등록.
            commandRegistrar = new CommandRegistrar(this, menuManager);
            commandRegistrar.register();
            menuManager.onReload(() -> commandRegistrar.register());

            // 리스너 등록.
            getServer().getPluginManager().registerEvents(new MenuClickListener(menuManager), this);
            getServer().getPluginManager().registerEvents(new SneakSwapListener(menuManager), this);

            // 활성화 성공 (주황) — 전 플러그인 통일 형식(§9-4).
            getComponentLogger().info(mm.deserialize(
                    "<white>MenuPlugin</white><gray> - </gray><gold>연결 성공</gold>"));
        } catch (Throwable ex) {
            // 활성화 실패 (빨강).
            getComponentLogger().error(mm.deserialize(
                    "<white>MenuPlugin</white><gray> - </gray><red>연결 실패</red>"), ex);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (commandRegistrar != null) {
            commandRegistrar.unregister(null);
        }
    }

    public MenuManager menuManager() {
        return menuManager;
    }
}
