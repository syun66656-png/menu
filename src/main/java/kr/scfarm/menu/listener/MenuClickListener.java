package kr.scfarm.menu.listener;

import kr.scfarm.menu.config.ButtonDefinition;
import kr.scfarm.menu.menu.MenuHolder;
import kr.scfarm.menu.menu.MenuManager;
import kr.scfarm.menu.menu.MenuView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** 클릭/드래그 처리(§9-2): 우리 GUI 는 항상 취소, 상단 클릭만 라우팅. */
public final class MenuClickListener implements Listener {

    private final MenuManager menuManager;

    public MenuClickListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder();
        if (!(holder instanceof MenuHolder menuHolder)) {
            return;
        }

        // 우리 GUI 면 무조건 취소(아이템 탈취/쉬프트 이동/드래그 방지).
        event.setCancelled(true);

        // 상단 인벤토리 클릭만 처리.
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= top.getSize()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        MenuView view = menuHolder.view();
        if (view == null) {
            return;
        }
        ButtonDefinition button = view.button(rawSlot);
        if (button == null) {
            return;
        }
        menuManager.execute(player, button);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }
}
