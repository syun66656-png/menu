package kr.scfarm.menu.button.handler;

import kr.scfarm.menu.config.ButtonDefinition;
import org.bukkit.entity.Player;

/** {@code type: close} — 닫기만(§7). */
public final class CloseHandler implements ButtonHandler {

    @Override
    public void handle(Player player, ButtonDefinition button) {
        player.closeInventory();
    }
}
