package kr.scfarm.menu.command;

import kr.scfarm.menu.menu.MenuManager;
import kr.scfarm.menu.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * {@code /메뉴리로드}(OP 전용, §9-3).
 *
 * <p>권한이 없으면 존재하지 않는 명령어처럼 취급한다:
 * 탭완성에 뜨지 않고({@link #testPermissionSilent}), 실행 시 unknown-command 메시지.
 */
public final class ReloadCommand extends Command {

    private final MenuManager menuManager;
    private final String permission;

    public ReloadCommand(String name, MenuManager menuManager, String permission) {
        super(name);
        this.menuManager = menuManager;
        this.permission = permission;
    }

    private boolean allowed(CommandSender sender) {
        if (permission == null || permission.isBlank()) {
            // 권한 미지정 → OP 전용.
            return sender.isOp();
        }
        return sender.hasPermission(permission);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!allowed(sender)) {
            // 없는 명령어처럼 처리.
            String msg = menuManager.config().message("unknown-command");
            if (msg != null && !msg.isBlank()) {
                sender.sendMessage(Text.mm(msg));
            }
            return true;
        }
        menuManager.reload();
        String success = menuManager.config().message("reload-success");
        if (success != null && !success.isBlank()) {
            sender.sendMessage(Text.mm(success));
        }
        return true;
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        return allowed(target);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias,
                                             @NotNull String[] args) {
        return Collections.emptyList();
    }
}
