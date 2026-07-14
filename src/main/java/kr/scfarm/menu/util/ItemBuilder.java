package kr.scfarm.menu.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** ItemStack 구성용 얇은 fluent 헬퍼. 텍스트는 모두 {@link Text} 경로를 탄다. */
public final class ItemBuilder {

    private final ItemStack stack;

    private ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(ItemStack base) {
        return new ItemBuilder(base.clone());
    }

    public ItemBuilder amount(int amount) {
        stack.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemBuilder name(String miniMessage) {
        edit(meta -> meta.displayName(Text.item(miniMessage)));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        edit(meta -> {
            List<Component> components = new ArrayList<>(lines.size());
            for (String line : lines) {
                components.add(Text.item(line));
            }
            meta.lore(components);
        });
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder customModelData(int cmd) {
        edit(meta -> meta.setCustomModelData(cmd));
        return this;
    }

    public ItemBuilder itemModel(NamespacedKey key) {
        if (key != null) {
            edit(meta -> meta.setItemModel(key));
        }
        return this;
    }

    public ItemBuilder glow() {
        edit(meta -> {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        return this;
    }

    public ItemStack build() {
        return stack;
    }

    private void edit(java.util.function.Consumer<ItemMeta> consumer) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            consumer.accept(meta);
            stack.setItemMeta(meta);
        }
    }
}
