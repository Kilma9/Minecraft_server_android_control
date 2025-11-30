package com.kilma.minecraft.data

import com.kilma.minecraft.R

object ItemsRepository {
    
    fun getPopularItems(): List<MinecraftItem> {
        return listOf(
            // Resources
            MinecraftItem("Diamond", "minecraft:diamond", R.drawable.mc_diamond, "Resources"),
            MinecraftItem("Emerald", "minecraft:emerald", R.drawable.mc_emerald, "Resources"),
            MinecraftItem("Iron Ingot", "minecraft:iron_ingot", R.drawable.mc_iron_ingot, "Resources"),
            MinecraftItem("Obsidian", "minecraft:obsidian", R.drawable.mc_obsidian, "Resources"),
            
            // Tools & Weapons
            MinecraftItem("Diamond Sword", "minecraft:diamond_sword", R.drawable.mc_diamond_sword, "Combat"),
            MinecraftItem("Iron Pickaxe", "minecraft:iron_pickaxe", R.drawable.mc_iron_pickaxe, "Tools"),
            
            // Special Items
            MinecraftItem("Elytra", "minecraft:elytra", R.drawable.mc_elytra, "Special"),
            MinecraftItem("Totem of Undying", "minecraft:totem_of_undying", R.drawable.mc_totem_of_undying, "Special"),
            MinecraftItem("Ender Pearl", "minecraft:ender_pearl", R.drawable.mc_ender_pearl, "Special"),
            MinecraftItem("Enchanted Book", "minecraft:enchanted_book", R.drawable.mc_enchanted_book, "Special"),
            
            // Food
            MinecraftItem("Golden Apple", "minecraft:golden_apple", R.drawable.mc_golden_apple, "Food"),
            MinecraftItem("Enchanted Golden Apple", "minecraft:enchanted_golden_apple", R.drawable.mc_golden_apple, "Food"),
            
            // Blocks
            MinecraftItem("TNT", "minecraft:tnt", R.drawable.mc_tnt, "Blocks"),
            
            // Additional Popular Items
            MinecraftItem("Netherite Sword", "minecraft:netherite_sword", R.drawable.mc_diamond_sword, "Combat"),
            MinecraftItem("Netherite Pickaxe", "minecraft:netherite_pickaxe", R.drawable.mc_iron_pickaxe, "Tools"),
            MinecraftItem("Bow", "minecraft:bow", R.drawable.mc_diamond_sword, "Combat"),
            MinecraftItem("Arrow (64)", "minecraft:arrow", R.drawable.mc_ender_pearl, "Combat"),
            MinecraftItem("Ender Chest", "minecraft:ender_chest", R.drawable.mc_obsidian, "Blocks"),
            MinecraftItem("Shulker Box", "minecraft:shulker_box", R.drawable.mc_obsidian, "Blocks"),
            MinecraftItem("Beacon", "minecraft:beacon", R.drawable.mc_emerald, "Blocks"),
            MinecraftItem("Diamond Block", "minecraft:diamond_block", R.drawable.mc_diamond, "Blocks"),
            MinecraftItem("Netherite Ingot", "minecraft:netherite_ingot", R.drawable.mc_iron_ingot, "Resources"),
            MinecraftItem("Ancient Debris", "minecraft:ancient_debris", R.drawable.mc_obsidian, "Resources"),
            MinecraftItem("Nether Star", "minecraft:nether_star", R.drawable.mc_emerald, "Resources"),
            MinecraftItem("Trident", "minecraft:trident", R.drawable.mc_diamond_sword, "Combat"),
            MinecraftItem("Mending Book", "minecraft:enchanted_book{Enchantments:[{id:mending,lvl:1}]}", R.drawable.mc_enchanted_book, "Enchanted"),
            MinecraftItem("Sharpness V Book", "minecraft:enchanted_book{Enchantments:[{id:sharpness,lvl:5}]}", R.drawable.mc_enchanted_book, "Enchanted"),
            MinecraftItem("Protection IV Book", "minecraft:enchanted_book{Enchantments:[{id:protection,lvl:4}]}", R.drawable.mc_enchanted_book, "Enchanted"),
            MinecraftItem("Fortune III Book", "minecraft:enchanted_book{Enchantments:[{id:fortune,lvl:3}]}", R.drawable.mc_enchanted_book, "Enchanted"),
            MinecraftItem("Silk Touch Book", "minecraft:enchanted_book{Enchantments:[{id:silk_touch,lvl:1}]}", R.drawable.mc_enchanted_book, "Enchanted"),
        )
    }
    
    fun searchItems(query: String): List<MinecraftItem> {
        if (query.isBlank()) return getPopularItems()
        return getPopularItems().filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.category.contains(query, ignoreCase = true)
        }
    }
    
    fun getCategories(): List<String> {
        return getPopularItems()
            .map { it.category }
            .distinct()
            .sorted()
    }
    
    fun getItemsByCategory(category: String): List<MinecraftItem> {
        return getPopularItems().filter { it.category == category }
    }
}
