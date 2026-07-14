package kr.scfarm.menu.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;

/**
 * 모든 텍스트 렌더링의 단일 진입점.
 *
 * <p>MiniMessage 를 기반으로 하며, Nexo 가 설치되어 있으면 그 glyph/shift 태그
 * 리졸버를 리플렉션으로 연동한다({@link NexoHook}). Nexo 가 없으면
 * {@code <glyph:...>} / {@code <shift:...>} 태그를 우아하게 무시(빈 컴포넌트)하는
 * 폴백 리졸버를 사용해 파싱 예외를 방지한다.
 */
public final class Text {

    private Text() {
    }

    /** Nexo 부재 시에도 파싱이 깨지지 않도록 glyph/shift 를 흡수하는 폴백 MiniMessage. */
    private static final MiniMessage FALLBACK = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(TagResolver.standard())
                    .resolver(fallbackTag("glyph"))
                    .resolver(fallbackTag("shift"))
                    .build())
            .build();

    /**
     * 문자열을 컴포넌트로 변환한다. Nexo 연동이 가능하면 Nexo 경로를,
     * 아니면 폴백 MiniMessage 를 사용한다. 어느 경우든 GUI 아이템 이름/로어처럼
     * 기본 이탤릭이 붙는 상황을 대비해 명시 지정이 없는 한 이탤릭을 끈다.
     */
    public static Component mm(String raw) {
        if (raw == null) {
            return Component.empty();
        }
        // 잘못된 MiniMessage 한 줄이 메뉴 오픈 전체를 중단시키지 않도록 격리.
        // 파싱 실패 시 원문을 평문으로 표시한다.
        try {
            return deserialize(raw);
        } catch (Exception ex) {
            return Component.text(raw);
        }
    }

    /** 아이템 이름/로어용: 바닐라 기본 이탤릭을 제거한 컴포넌트. */
    public static Component item(String raw) {
        return mm(raw).decoration(TextDecoration.ITALIC, false);
    }

    /** {placeholder} 치환 후 파싱. */
    public static Component mm(String raw, Map<String, String> placeholders) {
        return mm(replace(raw, placeholders));
    }

    public static String replace(String raw, Map<String, String> placeholders) {
        if (raw == null || placeholders == null || placeholders.isEmpty()) {
            return raw;
        }
        String out = raw;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return out;
    }

    private static Component deserialize(String raw) {
        if (NexoHook.isAvailable()) {
            Component nexo = NexoHook.deserialize(raw);
            if (nexo != null) {
                return nexo;
            }
        }
        return FALLBACK.deserialize(raw);
    }

    /** 인자를 무시하고 빈 컴포넌트를 반환하는 태그 리졸버(폴백용). */
    private static TagResolver fallbackTag(String name) {
        return TagResolver.resolver(name, (args, ctx) -> {
            // 인자를 모두 소비하고 아무것도 렌더링하지 않음.
            while (args.hasNext()) {
                args.pop();
            }
            return Tag.selfClosingInserting(Component.empty());
        });
    }
}
