package kr.scfarm.menu.button;

import java.util.Locale;

/** 버튼 동작 유형(§7). config 의 {@code type} 값과 1:1. */
public enum ButtonType {
    WARP,
    COMMAND,
    MESSAGE,
    OPEN,
    CLOSE;

    public static ButtonType from(String raw) {
        if (raw == null) {
            return CLOSE;
        }
        try {
            return ButtonType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return CLOSE;
        }
    }
}
