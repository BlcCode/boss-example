package ru.blc.example.boss.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ItemBuilder {

    public static ItemBuilder get(@NotNull Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    private final ItemStack result;

    private ItemBuilder(ItemStack stack) {
        this.result = stack;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        ItemMeta itemMeta = result.getItemMeta();
        itemMeta.addEnchant(enchantment, level, true);
        result.setItemMeta(itemMeta);
        return this;
    }

    public ItemStack build() {
        return result.clone();
    }
}
