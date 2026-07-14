package kr.scfarm.menu.cooldown;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 플레이어별 마지막 메뉴 오픈 시각으로 쿨다운을 판정(§9-1). */
public final class CooldownManager {

    private final ConcurrentHashMap<UUID, Long> lastOpen = new ConcurrentHashMap<>();

    /**
     * 쿨다운이 남아 있으면 남은 밀리초, 통과면 0 을 반환한다.
     * 통과 시 마지막 오픈 시각을 갱신한다.
     */
    public long tryConsume(UUID player, long cooldownMillis) {
        if (cooldownMillis <= 0) {
            return 0;
        }
        long now = System.currentTimeMillis();
        Long previous = lastOpen.get(player);
        if (previous != null) {
            long elapsed = now - previous;
            if (elapsed < cooldownMillis) {
                return cooldownMillis - elapsed;
            }
        }
        lastOpen.put(player, now);
        return 0;
    }

    public void clear(UUID player) {
        lastOpen.remove(player);
    }
}
