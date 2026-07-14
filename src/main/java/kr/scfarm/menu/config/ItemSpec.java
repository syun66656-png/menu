package kr.scfarm.menu.config;

import kr.scfarm.menu.util.ItemBuilder;
import kr.scfarm.menu.util.NexoHook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

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

    /** map 형태(예: {@code { material: PAPER, name: "..." }}) 또는 섹션에서 파싱. */
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

    /** ItemStack 을 생성한다. */
    public ItemStack build() {
        ItemStack base = null;
        if (nexoItem != null && !nexoItem.isBlank()) {
            base = NexoHook.itemFromId(nexoItem);
        }
        ItemBuilder builder = (base != null) ? ItemBuilder.of(base) : ItemBuilder.of(material);

        builder.amount(amount);
        if (name != null) {
            builder.name(name);
        }
        if (lore != null && !lore.isEmpty()) {
            builder.lore(lore);
        }
        if (customModelData != null) {
            builder.customModelData(customModelData);
        }
        if (itemModel != null && !itemModel.isBlank()) {
            builder.itemModel(NamespacedKey.fromString(itemModel));
        }
        if (glow) {
            builder.glow();
        }
        return builder.build();
    }

    public Material material() {
        return material;
    }
}
