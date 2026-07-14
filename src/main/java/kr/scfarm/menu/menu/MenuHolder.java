package kr.scfarm.menu.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * 우리 GUI 식별용 커스텀 홀더(§9-2). 글리프 타이틀 비교는 불안정하므로
 * 인벤토리 홀더 타입으로 식별한다.
 */
public final class MenuHolder implements InventoryHolder {

    private MenuView view;

    public void attach(MenuView view) {
        this.view = view;
    }

    public MenuView view() {
        return view;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return view.inventory();
    }
}
