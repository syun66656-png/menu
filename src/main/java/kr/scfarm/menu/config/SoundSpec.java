package kr.scfarm.menu.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/** 사운드 정의 ({@code { key, volume, pitch }}). */
public record SoundSpec(String key, float volume, float pitch) {

    public static SoundSpec from(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String key = section.getString("key");
        if (key == null || key.isBlank()) {
            return null;
        }
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        return new SoundSpec(key, volume, pitch);
    }

    public void play(Player player) {
        try {
            Sound sound = Sound.sound(Key.key(key), Sound.Source.MASTER, volume, pitch);
            player.playSound(sound);
        } catch (Exception ignored) {
            // 잘못된 키는 조용히 무시.
        }
    }
}
