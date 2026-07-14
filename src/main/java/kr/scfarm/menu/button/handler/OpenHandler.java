package kr.scfarm.menu.button.handler;

import kr.scfarm.menu.config.ButtonDefinition;
import kr.scfarm.menu.menu.MenuManager;
import org.bukkit.entity.Player;

/** {@code type: open} — 다른 메뉴 열기(§7). 쿨다운 없이 즉시 전환. */
public final class OpenHandler implements ButtonHandler {

    private final MenuManager menuManager;

    public OpenHandler(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void handle(Player player, ButtonDefinition button) {
        String target = button.open();
        if (target == null || target.isBlank()) {
            return;
        }
        menuManager.open(player, target, true);
    }
}
