package kr.scfarm.menu.integration;

import kr.scfarm.menu.config.PluginConfig;
import kr.scfarm.menu.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * WarpCraft 연동(§3). 이동은 전적으로 WarpCraft 에 위임한다.
 *
 * <p>{@code type: warp} → 콘솔로 {@code warpother {player} {warp}} 실행.
 * WarpCraft 가 MariaDB 좌표 조회·서버 이동·도착지 정확 텔포까지 처리한다.
 * 메뉴는 DB 를 만지지 않는다.
 */
public final class WarpBridge {

    private final PluginConfig config;

    public WarpBridge(PluginConfig config) {
        this.config = config;
    }

    /** WarpCraft 가 설치·활성화되어 있는가. */
    public boolean isAvailable() {
        var plugin = Bukkit.getPluginManager().getPlugin("WarpCraft");
        return plugin != null && plugin.isEnabled();
    }

    /**
     * warp 이동을 위임 실행한다. WarpCraft 미작동이면 안내 메시지 후 false.
     *
     * @return 위임에 성공(명령 디스패치)하면 true
     */
    public boolean warp(Player player, String warp) {
        if (warp == null || warp.isBlank()) {
            return false;
        }
        if (!isAvailable()) {
            String msg = config.message("warp-unavailable");
            if (msg != null && !msg.isBlank()) {
                player.sendMessage(Text.mm(msg));
            }
            return false;
        }
        String command = Text.replace(config.warpCommandFormat(), Map.of(
                "player", player.getName(),
                "warp", warp
        ));
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
