package kr.scfarm.menu.button.handler;

import kr.scfarm.menu.config.ButtonDefinition;
import kr.scfarm.menu.integration.WarpBridge;
import org.bukkit.entity.Player;

/** {@code type: warp} — 이동을 WarpCraft 에 위임(§7). */
public final class WarpHandler implements ButtonHandler {

    private final WarpBridge warpBridge;

    public WarpHandler(WarpBridge warpBridge) {
        this.warpBridge = warpBridge;
    }

    @Override
    public void handle(Player player, ButtonDefinition button) {
        warpBridge.warp(player, button.warp());
    }
}
