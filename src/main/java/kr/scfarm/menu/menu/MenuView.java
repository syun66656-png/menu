package kr.scfarm.menu.menu;

import kr.scfarm.menu.config.ButtonDefinition;
import kr.scfarm.menu.config.MenuDefinition;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/** 런타임 인벤토리 + slot→buttonId 매핑(§9-2). 플레이어별로 생성된다. */
public final class MenuView {

    private final MenuDefinition definition;
    private final Inventory inventory;
    private final Map<Integer, ButtonDefinition> bySlot = new HashMap<>();

    public MenuView(MenuDefinition definition, Inventory inventory) {
        this.definition = definition;
        this.inventory = inventory;
    }

    public void map(int slot, ButtonDefinition button) {
        bySlot.put(slot, button);
    }

    /** 슬롯의 버튼을 O(1) 로 조회. 없으면 null. */
    public ButtonDefinition button(int slot) {
        return bySlot.get(slot);
    }

    public MenuDefinition definition() {
        return definition;
    }

    public Inventory inventory() {
        return inventory;
    }
}
