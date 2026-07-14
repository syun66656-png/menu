package kr.scfarm.menu.listener;

import kr.scfarm.menu.menu.MenuManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** 퇴장 시 쿨다운 맵에서 플레이어를 제거해 무한 증가(메모리 누수)를 막는다. */
public final class PlayerCleanupListener implements Listener {

    private final MenuManager menuManager;

    public PlayerCleanupListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        menuManager.cooldownManager().clear(event.getPlayer().getUniqueId());
    }
}
