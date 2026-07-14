package kr.scfarm.menu.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * 조건부 표시/클릭(§8). LuckPerms 등 Bukkit 권한 기준.
 *
 * <p>view-requirement 와 click-requirement 를 한 타입으로 표현한다.
 * <ul>
 *   <li>표시 실패 처리: {@link OnFail#HIDE} 또는 {@link OnFail#LOCKED}(+ lockedItem)</li>
 *   <li>클릭 거부 메시지: {@code denyMessage}</li>
 * </ul>
 */
public final class Requirement {

    public enum OnFail {
        HIDE,
        LOCKED;

        static OnFail from(String raw) {
            if (raw == null) {
                return HIDE;
            }
            try {
                return OnFail.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return HIDE;
            }
        }
    }

    private final String permission;
    private final OnFail onFail;
    private final ItemSpec lockedItem;
    private final String denyMessage;

    private Requirement(String permission, OnFail onFail, ItemSpec lockedItem, String denyMessage) {
        this.permission = permission;
        this.onFail = onFail;
        this.lockedItem = lockedItem;
        this.denyMessage = denyMessage;
    }

    public static Requirement from(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String permission = section.getString("permission");
        OnFail onFail = OnFail.from(section.getString("on-fail"));
        ItemSpec lockedItem = section.isConfigurationSection("locked-item")
                ? ItemSpec.from(section.getConfigurationSection("locked-item"))
                : null;
        String denyMessage = section.getString("deny-message");
        return new Requirement(permission, onFail, lockedItem, denyMessage);
    }

    /** 플레이어가 이 요구조건을 만족하는가. permission 이 비면 항상 true. */
    public boolean test(Player player) {
        if (permission == null || permission.isBlank()) {
            return true;
        }
        return player.hasPermission(permission);
    }

    public OnFail onFail() {
        return onFail;
    }

    public ItemSpec lockedItem() {
        return lockedItem;
    }

    public String denyMessage() {
        return denyMessage;
    }
}
