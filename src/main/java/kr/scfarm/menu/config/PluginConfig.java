package kr.scfarm.menu.config;

import org.bukkit.configuration.file.FileConfiguration;

/** config.yml 전역 설정(§4). */
public final class PluginConfig {

    private final boolean cooldownEnabled;
    private final int cooldownSeconds;

    private final String warpMode;
    private final String warpCommandFormat;

    private final String reloadCommand;
    private final String reloadPermission;

    private final boolean sneakSwapEnabled;
    private final String sneakSwapMenu;

    private final FileConfiguration messages;

    private PluginConfig(FileConfiguration config) {
        this.cooldownEnabled = config.getBoolean("cooldown.enabled", true);
        this.cooldownSeconds = config.getInt("cooldown.seconds", 3);

        this.warpMode = config.getString("warp.mode", "console-command");
        this.warpCommandFormat = config.getString("warp.command-format", "warpother {player} {warp}");

        this.reloadCommand = config.getString("reload.command", "메뉴리로드");
        this.reloadPermission = config.getString("reload.permission", "menu.reload");

        this.sneakSwapEnabled = config.getBoolean("sneak-swap-open.enabled", true);
        this.sneakSwapMenu = config.getString("sneak-swap-open.menu", "hub");

        this.messages = config;
    }

    public static PluginConfig from(FileConfiguration config) {
        return new PluginConfig(config);
    }

    public boolean cooldownEnabled() {
        return cooldownEnabled;
    }

    public int cooldownSeconds() {
        return cooldownSeconds;
    }

    public String warpMode() {
        return warpMode;
    }

    public String warpCommandFormat() {
        return warpCommandFormat;
    }

    public String reloadCommand() {
        return reloadCommand;
    }

    public String reloadPermission() {
        return reloadPermission;
    }

    public boolean sneakSwapEnabled() {
        return sneakSwapEnabled;
    }

    public String sneakSwapMenu() {
        return sneakSwapMenu;
    }

    /** MiniMessage 원문 메시지 조회. 키 없으면 빈 문자열. */
    public String message(String key) {
        return messages.getString("messages." + key, "");
    }
}
