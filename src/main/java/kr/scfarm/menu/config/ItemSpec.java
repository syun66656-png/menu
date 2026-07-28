package kr.scfarm.menu.config;

import kr.scfarm.menu.util.NexoHook;
import kr.scfarm.menu.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * config 의 아이템 정의(§6)를 파싱해 {@link ItemStack} 으로 만든다.
 *
 * <p>우선순위: {@code nexo-item} 이 지정되면 Nexo 로 생성 후 name/lore/CMD 를 덮어쓰고,
 * 없으면 {@code material} 기반으로 생성한다.
 */
public final class ItemSpec {

    private final String nexoItem;
    private final Material material;
    private final Integer customModelData;
    private final String itemModel;
    private final String name;
    private final List<String> lore;
    private final int amount;
    private final boolean glow;

    /** 정적 아이템은 열 때마다 MiniMessage 파싱/재생성하지 않도록 템플릿을 캐싱(§TPS). */
    private ItemStack template;

    private ItemSpec(String nexoItem, Material material, Integer customModelData, String itemModel,
                     String name, List<String> lore, int amount, boolean glow) {
        this.nexoItem = nexoItem;
        this.material = material;
        this.customModelData = customModelData;
        this.itemModel = itemModel;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.glow = glow;
    }

    public static ItemSpec from(ConfigurationSection section) {
        if (section == null) {
            return new ItemSpec(null, Material.PAPER, null, null, null, List.of(), 1, false);
        }
        String nexoItem = section.getString("nexo-item");
        String materialName = section.getString("material", "PAPER");
        Material material = Material.matchMaterial(materialName == null ? "PAPER" : materialName.toUpperCase());
        if (material == null) {
            material = Material.PAPER;
        }
        Integer cmd = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;
        String itemModel = section.getString("item-model");
        String name = section.getString("name");
        List<String> lore = section.contains("lore") ? new ArrayList<>(section.getStringList("lore")) : List.of();
        int amount = Math.max(1, section.getInt("amount", 1));
        boolean glow = section.getBoolean("glow", false);
        return new ItemSpec(nexoItem, material, cmd, itemModel, name, lore, amount, glow);
    }

    /**
     * ItemStack 을 반환한다. 최초 1회만 실제로 구성(파싱 포함)하고 이후에는 캐시를
     * 복제해 돌려주므로, 메뉴를 열 때마다 발생하던 MiniMessage 파싱 비용을 없앤다.
     * ItemSpec 은 리로드마다 새로 생성되므로 캐시는 리로드 시 자동 무효화된다.
     */
    public ItemStack build() {
        ItemStack cached = template;
        if (cached == null) {
            cached = construct();
            template = cached;
        }
        return cached.clone();
    }

    /** 메타를 한 번만 꺼내고 한 번만 되쓰는 단일 왕복 구성. */
    @SuppressWarnings("deprecation")
    private ItemStack construct() {
        ItemStack stack = null;
        if (nexoItem != null && !nexoItem.isBlank()) {
            stack = NexoHook.itemFromId(nexoItem);
        }
        if (stack == null) {
            stack = new ItemStack(material);
        }
        stack.setAmount(amount);

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(Text.item(name));
            }
            if (lore != null && !lore.isEmpty()) {
                List<Component> lines = new ArrayList<>(lore.size());
                for (String line : lore) {
                    lines.add(Text.item(line));
                }
                meta.lore(lines);
            }
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            if (itemModel != null && !itemModel.isBlank()) {
                NamespacedKey key = NamespacedKey.fromString(itemModel);
                if (key != null) {
                    meta.setItemModel(key);
                }
            }
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
