package kr.scfarm.menu.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Nexo 소프트 연동 계층(리플렉션 기반).
 *
 * <p>컴파일 타임에 Nexo 를 강제하지 않으면서, 런타임에 Nexo 가 존재하면:
 * <ul>
 *   <li>{@code <glyph:...>} / {@code <shift:...>} 를 포함한 MiniMessage 를 Nexo 의
 *       MiniMessage 인스턴스로 파싱({@link #deserialize})</li>
 *   <li>{@code nexo-item} ID 로 아이템 생성({@link #itemFromId})</li>
 * </ul>
 * Nexo 가 없거나 API 가 예상과 다르면 조용히 비활성화되어 폴백 경로가 사용된다.
 */
public final class NexoHook {

    private NexoHook() {
    }

    private static boolean resolved = false;
    private static boolean available = false;

    // Nexo MiniMessage: AdventureUtils.MINI_MESSAGE.deserialize(String)
    private static Object nexoMiniMessage;
    private static Method deserializeMethod;

    // Nexo item: NexoItems.itemFromId(String) -> (ItemBuilder) build() -> ItemStack
    private static Method itemFromIdMethod;
    private static Method itemBuilderBuildMethod;

    public static synchronized boolean isAvailable() {
        if (!resolved) {
            resolve();
        }
        return available;
    }

    private static void resolve() {
        resolved = true;
        if (Bukkit.getPluginManager().getPlugin("Nexo") == null) {
            available = false;
            return;
        }
        // 태그 리졸버(MiniMessage) 연동 시도 — 실패해도 아이템 연동은 별도로 가능.
        try {
            Class<?> adventureUtils = Class.forName("com.nexomc.nexo.utils.AdventureUtils");
            Object mm = adventureUtils.getField("MINI_MESSAGE").get(null);
            Method des = mm.getClass().getMethod("deserialize", String.class);
            nexoMiniMessage = mm;
            deserializeMethod = des;
        } catch (Throwable ignored) {
            nexoMiniMessage = null;
            deserializeMethod = null;
        }
        // 아이템 연동 시도.
        try {
            Class<?> nexoItems = Class.forName("com.nexomc.nexo.api.NexoItems");
            itemFromIdMethod = nexoItems.getMethod("itemFromId", String.class);
        } catch (Throwable ignored) {
            itemFromIdMethod = null;
        }
        // glyph/shift 렌더가 목적이므로 MiniMessage 연동이 되면 available=true.
        available = deserializeMethod != null || itemFromIdMethod != null;
    }

    /** Nexo MiniMessage 로 파싱. 실패 시 null(→ 폴백). */
    public static Component deserialize(String raw) {
        if (deserializeMethod == null) {
            return null;
        }
        try {
            Object result = deserializeMethod.invoke(nexoMiniMessage, raw);
            if (result instanceof Component c) {
                return c;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /** Nexo 아이템 ID 로 ItemStack 생성. 실패/미설치 시 null. */
    public static ItemStack itemFromId(String id) {
        if (id == null || itemFromIdMethod == null) {
            return null;
        }
        try {
            Object builder = itemFromIdMethod.invoke(null, id);
            if (builder == null) {
                return null;
            }
            if (itemBuilderBuildMethod == null) {
                itemBuilderBuildMethod = builder.getClass().getMethod("build");
            }
            Object stack = itemBuilderBuildMethod.invoke(builder);
            if (stack instanceof ItemStack is) {
                return is.clone();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
