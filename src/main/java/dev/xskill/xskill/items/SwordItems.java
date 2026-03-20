package dev.xskill.xskill.items;

import dev.xskill.xskill.model.SwordDefinition;
import dev.xskill.xskill.util.Keys;
import dev.xskill.xskill.util.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public final class SwordItems {
    private SwordItems() {
    }

    public static ItemStack create(SwordDefinition def) {
        Material m = def.material() != null ? def.material() : Material.DIAMOND_SWORD;
        ItemStack item = new ItemStack(m);
        applyFull(item, def);
        return item;
    }

    public static boolean applyFull(ItemStack item, SwordDefinition def) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        meta.displayName(Text.component(def.name()));
        List<String> lore = def.lore();
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore.stream().map(Text::component).toList());
        } else {
            meta.lore(null);
        }
        if (def.customModelData() > 0) {
            meta.setCustomModelData(def.customModelData());
        } else {
            meta.setCustomModelData(null);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(Keys.SWORD_ID, PersistentDataType.STRING, def.id());
        meta.getPersistentDataContainer().set(Keys.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());
        item.setItemMeta(meta);
        return true;
    }

    public static boolean applySwordIdOnly(ItemStack item, SwordDefinition def) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        meta.getPersistentDataContainer().set(Keys.SWORD_ID, PersistentDataType.STRING, def.id());
        meta.getPersistentDataContainer().set(Keys.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());
        item.setItemMeta(meta);
        return true;
    }

    public static String getSwordId(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(Keys.SWORD_ID, PersistentDataType.STRING);
    }

    public static boolean isSpecial(ItemStack item) {
        return getSwordId(item) != null;
    }
}

