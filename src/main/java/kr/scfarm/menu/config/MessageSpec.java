package kr.scfarm.menu.config;

import kr.scfarm.menu.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;

/** {@code type: message} 의 message 블록 (text / url / hover). */
public record MessageSpec(String text, String url, String hover) {

    public static MessageSpec from(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String text = section.getString("text", "");
        String url = section.getString("url");
        String hover = section.getString("hover");
        return new MessageSpec(text, url, hover);
    }

    /** 클릭 시 링크 열림(url) / 호버(hover) 를 적용한 컴포넌트로 렌더. */
    public Component render() {
        Component component = Text.mm(text);
        if (url != null && !url.isBlank()) {
            component = component.clickEvent(ClickEvent.openUrl(url));
        }
        if (hover != null && !hover.isBlank()) {
            component = component.hoverEvent(HoverEvent.showText(Text.mm(hover)));
        }
        return component;
    }
}
