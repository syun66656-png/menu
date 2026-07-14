package kr.scfarm.menu.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/** menus/*.yml → {@link MenuDefinition} 로딩. */
public final class MenuLoader {

    private final Plugin plugin;

    public MenuLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    /** menus/ 폴더를 스캔해 모든 메뉴를 파싱한다. 없으면 기본 hub.yml 을 배포. */
    public Map<String, MenuDefinition> loadAll() {
        File menusDir = new File(plugin.getDataFolder(), "menus");
        if (!menusDir.exists()) {
            // 최초 실행 시 기본 hub.yml 배포.
            plugin.saveResource("menus/hub.yml", false);
        }

        Map<String, MenuDefinition> menus = new LinkedHashMap<>();
        File[] files = menusDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) {
            return menus;
        }
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - ".yml".length());
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                menus.put(id, MenuDefinition.from(id, config));
            } catch (Exception ex) {
                plugin.getLogger().warning("메뉴 로드 실패: " + file.getName() + " - " + ex.getMessage());
            }
        }
        return menus;
    }
}
