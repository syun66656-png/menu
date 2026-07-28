package kr.scfarm.menu.cooldown;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 플레이어별 마지막 메뉴 오픈 시각으로 쿨다운을 판정(§9-1). */
public final class CooldownManager {

    private final ConcurrentHashMap<UUID, Long> lastOpen = new ConcurrentHashMap<>();

    /** 현재 쿨다운 사이클에서 이미 경고 메시지를 받은 플레이어(사이클당 1회만 경고). */
    private final Set<UUID> warned = ConcurrentHashMap.newKeySet();

    /**
     * 쿨다운을 통과하면 true(마지막 오픈 시각 갱신 + 경고 플래그 초기화),
     * 아직 쿨다운 중이면 false.
     */
    public boolean tryConsume(UUID player, long cooldownMillis) {
        if (cooldownMillis <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        Long previous = lastOpen.get(player);
        if (previous != null && now - previous < cooldownMillis) {
            return false;
        }
        lastOpen.put(player, now);
        warned.remove(player);
        return true;
    }

    /**
     * 쿨다운 경고를 보낼지 판정한다. 한 쿨다운 사이클에서 최초 1회만 true 를
     * 반환하고, 이후 같은 사이클의 반복 시도에는 false 를 반환한다
     * (연타 시 경고 메시지 도배 방지). 사이클이 끝나고 메뉴가 정상적으로
     * 열리면 {@link #tryConsume} 에서 플래그가 초기화된다.
     */
    public boolean shouldWarn(UUID player) {
        return warned.add(player);
    }

    public void clear(UUID player) {
        lastOpen.remove(player);
        warned.remove(player);
    }
}
