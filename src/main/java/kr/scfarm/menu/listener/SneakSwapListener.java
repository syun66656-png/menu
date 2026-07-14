package kr.scfarm.menu.listener;

import kr.scfarm.menu.menu.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * Shift + F(스니크 + 오프핸드 교체)로 메뉴 열기(§9-1).
 * 스니크 없는 F 단독 교체는 정상 통과시킨다.
 */
public final class SneakSwapListener implements Listener {

    private final MenuManager menuManager;

    public SneakSwapListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!menuManager.config().sneakSwapEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        event.setCancelled(true);
        menuManager.open(player, menuManager.config().sneakSwapMenu(), false);
    }
}
