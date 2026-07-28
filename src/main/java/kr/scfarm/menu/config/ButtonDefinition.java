package kr.scfarm.menu.config;

import kr.scfarm.menu.button.ButtonType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 버튼 정의(§5-2). slot 은 int / list / "범위문자열" 을 모두 파싱해 최종 int[] 로 전개한다(§5-2b).
 */
public final class ButtonDefinition {

    /** {@code command} 실행 주체. */
    public enum RunAs {
        PLAYER,
        CONSOLE;

        static RunAs from(String raw) {
            if (raw == null) {
                return PLAYER;
            }
            try {
                return RunAs.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return PLAYER;
            }
        }
    }

    private final int[] slots;
    private final ItemSpec item;
    private final SoundSpec sound;
    private final ButtonType type;

    // type 별 필드
    private final String warp;
    private final String command;
    private final RunAs runAs;
    private final String open;
    private final MessageSpec message;

    // 조건부(§8)
    private final Requirement viewRequirement;
    private final Requirement clickRequirement;

    private ButtonDefinition(int[] slots, ItemSpec item, SoundSpec sound,
                             ButtonType type, String warp, String command, RunAs runAs, String open,
                             MessageSpec message, Requirement viewRequirement, Requirement clickRequirement) {
        this.slots = slots;
        this.item = item;
        this.sound = sound;
        this.type = type;
        this.warp = warp;
        this.command = command;
        this.runAs = runAs;
        this.open = open;
        this.message = message;
        this.viewRequirement = viewRequirement;
        this.clickRequirement = clickRequirement;
    }

    public static ButtonDefinition from(ConfigurationSection section) {
        int[] slots = parseSlots(section);
        ItemSpec item = ItemSpec.from(section.getConfigurationSection("item"));
        SoundSpec sound = SoundSpec.from(section.getConfigurationSection("sound"));
        ButtonType type = ButtonType.from(section.getString("type"));

        String warp = section.getString("warp");
        String command = section.getString("command");
        RunAs runAs = RunAs.from(section.getString("as"));
        String open = section.getString("open");
        MessageSpec message = MessageSpec.from(section.getConfigurationSection("message"));

        Requirement viewRequirement = Requirement.from(section.getConfigurationSection("view-requirement"));
        Requirement clickRequirement = Requirement.from(section.getConfigurationSection("click-requirement"));

        return new ButtonDefinition(slots, item, sound, type, warp, command, runAs,
                open, message, viewRequirement, clickRequirement);
    }

    /**
     * slot / slots 값을 int[] 로 전개.
     * <ul>
     *   <li>int → 단일 칸</li>
     *   <li>list → 각 원소를 재귀적으로 파싱(정수 또는 "범위문자열")</li>
     *   <li>"0-2, 9-11" → 범위(-) + 묶음(,) 전개</li>
     * </ul>
     */
    static int[] parseSlots(ConfigurationSection section) {
        Object raw = section.contains("slot") ? section.get("slot") : section.get("slots");
        Set<Integer> out = new LinkedHashSet<>();
        expand(raw, out);
        int[] result = new int[out.size()];
        int i = 0;
        for (int slot : out) {
            result[i++] = slot;
        }
        return result;
    }

    private static void expand(Object raw, Set<Integer> out) {
        if (raw == null) {
            return;
        }
        if (raw instanceof Number number) {
            out.add(number.intValue());
        } else if (raw instanceof List<?> list) {
            for (Object element : list) {
                expand(element, out);
            }
        } else if (raw instanceof String str) {
            expandString(str, out);
        }
    }

    private static void expandString(String str, Set<Integer> out) {
        for (String group : str.split(",")) {
            String token = group.trim();
            if (token.isEmpty()) {
                continue;
            }
            int dash = token.indexOf('-');
            if (dash > 0) {
                try {
                    int start = Integer.parseInt(token.substring(0, dash).trim());
                    int end = Integer.parseInt(token.substring(dash + 1).trim());
                    int lo = Math.min(start, end);
                    int hi = Math.max(start, end);
                    for (int s = lo; s <= hi; s++) {
                        out.add(s);
                    }
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    out.add(Integer.parseInt(token));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public int[] slots() {
        return slots;
    }

    public ItemSpec item() {
        return item;
    }

    public SoundSpec sound() {
        return sound;
    }

    public ButtonType type() {
        return type;
    }

    public String warp() {
        return warp;
    }

    public String command() {
        return command;
    }

    public RunAs runAs() {
        return runAs;
    }

    public String open() {
        return open;
    }

    public MessageSpec message() {
        return message;
    }

    public Requirement viewRequirement() {
        return viewRequirement;
    }

    public Requirement clickRequirement() {
        return clickRequirement;
    }
}
