package kr.scfarm.menu.command;

import kr.scfarm.menu.menu.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** {@code /메뉴 /menu /apsb} 등 메뉴 여는 명령어(§9-1). 런타임 동적 등록. */
public final class MenuOpenCommand extends Command {

    private final MenuManager menuManager;
    private final String menuId;

    public MenuOpenCommand(String name, List<String> aliases, MenuManager menuManager, String menuId) {
        super(name);
        setAliases(aliases);
        this.menuManager = menuManager;
        this.menuId = menuId;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        menuManager.open(player, menuId, false);
        return true;
    }
}
