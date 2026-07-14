package kr.scfarm.menu.button.handler;

import kr.scfarm.menu.config.ButtonDefinition;
import kr.scfarm.menu.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * {@code type: command} — 명령어 실행(§7).
 * <ul>
 *   <li>{@code as: player}(기본) → 플레이어가 실행</li>
 *   <li>{@code as: console} → 콘솔이 실행, {player} 치환</li>
 * </ul>
 */
public final class CommandHandler implements ButtonHandler {

    @Override
    public void handle(Player player, ButtonDefinition button) {
        String command = button.command();
        if (command == null || command.isBlank()) {
            return;
        }
        // '/' 접두 제거(있으면).
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (button.runAs() == ButtonDefinition.RunAs.CONSOLE) {
            String resolved = Text.replace(command, Map.of("player", player.getName()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
        } else {
            player.performCommand(command);
        }
    }
}
