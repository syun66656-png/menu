package kr.scfarm.menu.button.handler;

import kr.scfarm.menu.config.ButtonDefinition;
import kr.scfarm.menu.config.MessageSpec;
import org.bukkit.entity.Player;

/** {@code type: message} — 메시지(+선택 링크) 전송(§7). */
public final class MessageHandler implements ButtonHandler {

    @Override
    public void handle(Player player, ButtonDefinition button) {
        MessageSpec message = button.message();
        if (message == null) {
            return;
        }
        player.sendMessage(message.render());
    }
}
