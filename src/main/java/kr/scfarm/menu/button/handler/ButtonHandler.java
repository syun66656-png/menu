package kr.scfarm.menu.button.handler;

import kr.scfarm.menu.config.ButtonDefinition;
import org.bukkit.entity.Player;

/** 버튼 타입별 실행 전략(§7). */
public interface ButtonHandler {

    void handle(Player player, ButtonDefinition button);
}
