package kr.scfarm.menu.config;

import kr.scfarm.menu.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 하나의 메뉴 정의(§5-3): 제목/줄 수/버튼맵/여는 명령어 등. */
public final class MenuDefinition {

    private final String id;
    private final List<String> openCommands;
    private final String openPermission;
    private final int rows;
    private final String title;
    private final SoundSpec openSound;
    private final boolean fillerEnabled;
    private final ItemSpec fillerItem;
    private final Map<String, ButtonDefinition> buttons;

    /** 정적 타이틀은 열 때마다 파싱하지 않도록 캐싱(§TPS). */
    private Component titleComponent;

    private MenuDefinition(String id, List<String> openCommands, String openPermission, int rows, String title,
                           SoundSpec openSound, boolean fillerEnabled, ItemSpec fillerItem,
                           Map<String, ButtonDefinition> buttons) {
        this.id = id;
        this.openCommands = openCommands;
        this.openPermission = openPermission;
        this.rows = rows;
        this.title = title;
        this.openSound = openSound;
        this.fillerEnabled = fillerEnabled;
        this.fillerItem = fillerItem;
        this.buttons = buttons;
    }

    public static MenuDefinition from(String id, FileConfiguration config) {
        List<String> openCommands = config.getStringList("open-commands");
        String openPermission = config.getString("open-permission", "");

        ConfigurationSection gui = config.getConfigurationSection("gui");
        int rows = gui != null ? gui.getInt("rows", 6) : 6;
        rows = Math.max(1, Math.min(6, rows));
        String title = gui != null ? gui.getString("title", "") : "";
        SoundSpec openSound = gui != null ? SoundSpec.from(gui.getConfigurationSection("open-sound")) : null;

        boolean fillerEnabled = false;
        ItemSpec fillerItem = null;
        ConfigurationSection filler = config.getConfigurationSection("filler");
        if (filler != null) {
            fillerEnabled = filler.getBoolean("enabled", false);
            fillerItem = ItemSpec.from(filler.getConfigurationSection("item"));
        }

        Map<String, ButtonDefinition> buttons = new LinkedHashMap<>();
        ConfigurationSection buttonsSection = config.getConfigurationSection("buttons");
        if (buttonsSection != null) {
            for (String key : buttonsSection.getKeys(false)) {
                ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(key);
                if (buttonSection != null) {
                    buttons.put(key, ButtonDefinition.from(key, buttonSection));
                }
            }
        }

        return new MenuDefinition(id, new ArrayList<>(openCommands), openPermission, rows, title, openSound,
                fillerEnabled, fillerItem, Collections.unmodifiableMap(buttons));
    }

    public String id() {
        return id;
    }

    public List<String> openCommands() {
        return openCommands;
    }

    public String openPermission() {
        return openPermission;
    }

    public int rows() {
        return rows;
    }

    public int size() {
        return rows * 9;
    }

    public String title() {
        return title;
    }

    /** 파싱된 타이틀 컴포넌트(최초 1회 파싱 후 캐시). */
    public Component titleComponent() {
        Component cached = titleComponent;
        if (cached == null) {
            cached = Text.mm(title);
            titleComponent = cached;
        }
        return cached;
    }

    public SoundSpec openSound() {
        return openSound;
    }

    public boolean fillerEnabled() {
        return fillerEnabled;
    }

    public ItemSpec fillerItem() {
        return fillerItem;
    }

    public Map<String, ButtonDefinition> buttons() {
        return buttons;
    }
}
