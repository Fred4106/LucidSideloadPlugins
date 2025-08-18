package com.fredplugins.common.magic;

import net.runelite.api.ItemID;
import net.runelite.api.SpriteID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellIds {

    private static final List<SpellInfo> Spells = new ArrayList<>();
    private static final Map<SpellProduct, SpellInfo> SpellProductMap = new HashMap<>();
    private static final Map<String, SpellInfo> SpellNameMap = new HashMap<>();
    private static final Map<Integer, SpellInfo> SpellSpriteMap = new HashMap<>();

    public static List<SpellInfo> getAllSpells()
    {
        return Spells;
    }
    public static SpellInfo getSpellBySpriteId(int spriteId)
    {
        return SpellSpriteMap.getOrDefault(spriteId, null);
    }
    public static SpellInfo getSpellByName(String spellName)
    {
        return SpellNameMap.getOrDefault(spellName.toLowerCase(), null);
    }
    public static SpellInfo getSpellByProduct(int itemId, int quantity)
    {
        return SpellProductMap.getOrDefault(new SpellProduct(itemId, quantity), null);
    }

    static
    {
        // Standard Spellbook

        // Lumbridge Home Teleport
        Spells.add(new SpellInfo("Wind Strike", SpriteID.SPELL_WIND_STRIKE, new SpellCost(new int[] {ItemID.MIND_RUNE, ItemID.AIR_RUNE}, new int[] {1, 1})));
        Spells.add(new SpellInfo("Confuse", SpriteID.SPELL_CONFUSE, new SpellCost(new int[] {ItemID.BODY_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 3, 2})));

        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Opal)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.AIR_RUNE, ItemID.COSMIC_RUNE, ItemID.OPAL_BOLTS}, new int[] {2, 1, 10}),
                new SpellProduct(ItemID.OPAL_BOLTS_E, 10), new SpellProduct(ItemID.OPAL_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Sapphire)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.WATER_RUNE, ItemID.COSMIC_RUNE, ItemID.MIND_RUNE, ItemID.SAPPHIRE_BOLTS}, new int[] {1, 1, 1, 10}),
                new SpellProduct(ItemID.SAPPHIRE_BOLTS_E, 10), new SpellProduct(ItemID.SAPPHIRE_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Jade)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.EARTH_RUNE, ItemID.COSMIC_RUNE, ItemID.JADE_BOLTS}, new int[] {2, 1, 10}),
                new SpellProduct(ItemID.JADE_BOLTS_E, 10), new SpellProduct(ItemID.JADE_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Pearl)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.WATER_RUNE, ItemID.COSMIC_RUNE, ItemID.PEARL_BOLTS}, new int[] {2, 1, 10}),
                new SpellProduct(ItemID.PEARL_BOLTS_E, 10), new SpellProduct(ItemID.PEARL_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Emerald)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.AIR_RUNE, ItemID.COSMIC_RUNE, ItemID.NATURE_RUNE, ItemID.EMERALD_BOLTS}, new int[] {3, 1, 1, 10}),
                new SpellProduct(ItemID.EMERALD_BOLTS_E, 10), new SpellProduct(ItemID.EMERALD_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Red Topaz)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.FIRE_RUNE, ItemID.COSMIC_RUNE, ItemID.TOPAZ_BOLTS}, new int[] {2, 1, 10}),
                new SpellProduct(ItemID.TOPAZ_BOLTS_E, 10), new SpellProduct(ItemID.TOPAZ_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Ruby)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.FIRE_RUNE, ItemID.BLOOD_RUNE, ItemID.COSMIC_RUNE, ItemID.RUBY_BOLTS}, new int[] {5, 1, 1, 10}),
                new SpellProduct(ItemID.RUBY_BOLTS_E, 10), new SpellProduct(ItemID.RUBY_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Diamond)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.EARTH_RUNE, ItemID.COSMIC_RUNE, ItemID.LAW_RUNE, ItemID.DIAMOND_BOLTS}, new int[] {10, 1, 2, 10}),
                new SpellProduct(ItemID.DIAMOND_BOLTS_E, 10), new SpellProduct(ItemID.DIAMOND_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Dragonstone)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.EARTH_RUNE, ItemID.COSMIC_RUNE, ItemID.SOUL_RUNE, ItemID.DRAGONSTONE_BOLTS}, new int[] {15, 1, 1, 10}),
                new SpellProduct(ItemID.DRAGONSTONE_BOLTS_E, 10), new SpellProduct(ItemID.DRAGONSTONE_DRAGON_BOLTS_E, 10)));
        Spells.add(new SpellInfo("Enchant Crossbow Bolt (Onyx)", SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, new SpellCost(new int[] {ItemID.FIRE_RUNE, ItemID.COSMIC_RUNE, ItemID.DEATH_RUNE, ItemID.ONYX_BOLTS}, new int[] {20, 1, 1, 10}),
                new SpellProduct(ItemID.ONYX_BOLTS_E, 10), new SpellProduct(ItemID.ONYX_DRAGON_BOLTS_E, 10)));

        Spells.add(new SpellInfo("Water Strike", SpriteID.SPELL_WATER_STRIKE, new SpellCost(new int[] {ItemID.MIND_RUNE, ItemID.WATER_RUNE, ItemID.AIR_RUNE}, new int[] {1, 1, 1})));
        Spells.add(
			new SpellInfo(
				"Lvl-1 Enchant", SpriteID.SPELL_LVL_1_ENCHANT,
				new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.WATER_RUNE, ItemID.OPAL_RING}, new int[] {1, 1, 1}),
				new SpellProduct(ItemID.RING_OF_RECOIL, 1),
				new SpellProduct(ItemID.GAMES_NECKLACE8, 1),
				new SpellProduct(ItemID.AMULET_OF_MAGIC, 1),
				new SpellProduct(ItemID.BRACELET_OF_CLAY, 1),
				new SpellProduct(ItemID.RING_OF_PURSUIT, 1),
				new SpellProduct(ItemID.NECKLACE_OF_PASSAGE5, 1),
				new SpellProduct(ItemID.EXPEDITIOUS_BRACELET, 1),
				new SpellProduct(ItemID.AMULET_OF_BOUNTY, 1)
			)
		);
        Spells.add(new SpellInfo("Earth Strike", SpriteID.SPELL_EARTH_STRIKE, new SpellCost(new int[] {ItemID.MIND_RUNE, ItemID.EARTH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 2, 1})));
        Spells.add(new SpellInfo("Weaken", SpriteID.SPELL_WEAKEN, new SpellCost(new int[] {ItemID.BODY_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 3, 2})));
        Spells.add(new SpellInfo("Fire Strike", SpriteID.SPELL_FIRE_STRIKE, new SpellCost(new int[] {ItemID.MIND_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE}, new int[] {1, 3, 2})));
        Spells.add(new SpellInfo("Bones to Bananas", SpriteID.SPELL_BONES_TO_BANANAS, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 2, 2})));
        Spells.add(new SpellInfo("Wind Bolt", SpriteID.SPELL_WIND_BOLT, new SpellCost(new int[] {ItemID.CHAOS_RUNE, ItemID.AIR_RUNE}, new int[] {1, 2})));
        Spells.add(new SpellInfo("Curse", SpriteID.SPELL_CURSE, new SpellCost(new int[] {ItemID.BODY_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 2, 3})));
        Spells.add(new SpellInfo("Bind", SpriteID.SPELL_BIND, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {2, 3, 3})));
        Spells.add(new SpellInfo("Low Level Alchemy", SpriteID.SPELL_LOW_LEVEL_ALCHEMY, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 3})));
        Spells.add(new SpellInfo("Water Bolt", SpriteID.SPELL_WATER_BOLT, new SpellCost(new int[] {ItemID.CHAOS_RUNE, ItemID.WATER_RUNE, ItemID.AIR_RUNE}, new int[] {1, 2, 2})));
        Spells.add(new SpellInfo("Varrock Teleport", SpriteID.SPELL_VARROCK_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 3, 1})));
        Spells.add(new SpellInfo("Lvl-2 Enchant", SpriteID.SPELL_LVL_2_ENCHANT, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.AIR_RUNE}, new int[] {1, 3})));
        Spells.add(new SpellInfo("Earth Bolt", SpriteID.SPELL_EARTH_BOLT, new SpellCost(new int[] {ItemID.CHAOS_RUNE, ItemID.EARTH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 3, 2})));
        Spells.add(new SpellInfo("Lumbridge Teleport", SpriteID.SPELL_LUMBRIDGE_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 3, 1})));
        Spells.add(new SpellInfo("Telekinetic Grab", SpriteID.SPELL_TELEKINETIC_GRAB, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE}, new int[] {1, 1})));
        Spells.add(new SpellInfo("Fire Bolt", SpriteID.SPELL_FIRE_BOLT, new SpellCost(new int[] {ItemID.CHAOS_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE}, new int[] {1, 4, 3})));
        Spells.add(new SpellInfo("Falador Teleport", SpriteID.SPELL_FALADOR_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.WATER_RUNE}, new int[] {1, 3, 1})));
        Spells.add(new SpellInfo("Crumble Undead", SpriteID.SPELL_CRUMBLE_UNDEAD, new SpellCost(new int[] {ItemID.CHAOS_RUNE, ItemID.EARTH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 2, 2})));
        Spells.add(new SpellInfo("Teleport to House", SpriteID.SPELL_TELEPORT_TO_HOUSE, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Wind Blast", SpriteID.SPELL_WIND_BLAST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 3})));
        Spells.add(new SpellInfo("Superheat Item", SpriteID.SPELL_SUPERHEAT_ITEM, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 4})));
        Spells.add(new SpellInfo("Camelot Teleport", SpriteID.SPELL_CAMELOT_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE}, new int[] {1, 5})));
        Spells.add(new SpellInfo("Water Blast", SpriteID.SPELL_WATER_BLAST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.WATER_RUNE, ItemID.AIR_RUNE}, new int[] {1, 3, 3})));
        Spells.add(new SpellInfo("Lvl-3 Enchant", SpriteID.SPELL_LVL_3_ENCHANT, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 5})));
        Spells.add(new SpellInfo("Iban Blast", SpriteID.SPELL_IBAN_BLAST, new SpellCost(new int[] {ItemID.IBANS_STAFF, ItemID.DEATH_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 1, 5})));
        Spells.add(new SpellInfo("Snare", SpriteID.SPELL_SNARE, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {3, 4, 4})));
        Spells.add(new SpellInfo("Magic Dart", SpriteID.SPELL_MAGIC_DART, new SpellCost(new int[] {ItemID.SLAYERS_STAFF, ItemID.DEATH_RUNE, ItemID.MIND_RUNE}, new int[] {1, 1, 4})));
        Spells.add(new SpellInfo("Ardougne Teleport", SpriteID.SPELL_ARDOUGNE_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.WATER_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Earth Blast", SpriteID.SPELL_EARTH_BLAST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.EARTH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 4, 3})));
        Spells.add(new SpellInfo("High Level Alchemy", SpriteID.SPELL_HIGH_LEVEL_ALCHEMY, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 5})));
        Spells.add(new SpellInfo("Charge Water Orb", SpriteID.SPELL_CHARGE_WATER_ORB, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.WATER_RUNE, ItemID.UNPOWERED_ORB}, new int[] {3, 30, 1})));
        Spells.add(new SpellInfo("Lvl-4 Enchant", SpriteID.SPELL_LVL_4_ENCHANT, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 10})));
        Spells.add(new SpellInfo("Watchtower Teleport", SpriteID.SPELL_WATCHTOWER_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.EARTH_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Fire Blast", SpriteID.SPELL_FIRE_BLAST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE}, new int[] {1, 5, 4})));
        Spells.add(new SpellInfo("Charge Earth Orb", SpriteID.SPELL_CHARGE_EARTH_ORB, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.EARTH_RUNE, ItemID.UNPOWERED_ORB}, new int[] {3, 30, 1})));
        Spells.add(new SpellInfo("Bones to Peaches", SpriteID.SPELL_BONES_TO_PEACHES, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {2, 4, 4})));
        Spells.add(new SpellInfo("Saradomin Strike", SpriteID.SPELL_SARADOMIN_STRIKE, new SpellCost(new int[] {ItemID.SARADOMIN_STAFF, ItemID.BLOOD_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 2, 4, 2})));
        Spells.add(new SpellInfo("Claws of Guthix", SpriteID.SPELL_CLAWS_OF_GUTHIX, new SpellCost(new int[] {ItemID.GUTHIX_STAFF, ItemID.BLOOD_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 2, 4, 1})));
        Spells.add(new SpellInfo("Flames of Zamorak", SpriteID.SPELL_FLAMES_OF_ZAMORAK, new SpellCost(new int[] {ItemID.ZAMORAK_STAFF, ItemID.BLOOD_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 2, 1, 4})));
        Spells.add(new SpellInfo("Trollheim Teleport", SpriteID.SPELL_TROLLHEIM_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Wind Wave", SpriteID.SPELL_WIND_WAVE, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.AIR_RUNE}, new int[] {1, 5})));
        Spells.add(new SpellInfo("Charge Fire Orb", SpriteID.SPELL_CHARGE_FIRE_ORB, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.FIRE_RUNE, ItemID.UNPOWERED_ORB}, new int[] {3, 30, 1})));
        Spells.add(new SpellInfo("Ape Atoll Teleport", SpriteID.SPELL_APE_ATOLL_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.WATER_RUNE, ItemID.FIRE_RUNE, ItemID.BANANA}, new int[] {2, 2, 2, 1})));
        Spells.add(new SpellInfo("Water Wave", SpriteID.SPELL_WATER_WAVE, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.WATER_RUNE, ItemID.AIR_RUNE}, new int[] {1, 7, 5})));
        Spells.add(new SpellInfo("Charge Air Orb", SpriteID.SPELL_CHARGE_AIR_ORB, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.AIR_RUNE, ItemID.UNPOWERED_ORB}, new int[] {3, 30, 1})));
        Spells.add(new SpellInfo("Vulnerability", SpriteID.SPELL_VULNERABILITY, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 5, 5})));
        Spells.add(new SpellInfo("Lvl-5 Enchant", SpriteID.SPELL_LVL_5_ENCHANT, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 15, 15})));
        Spells.add(new SpellInfo("Kourend Castle Teleport", SpriteID.SPELL_TELEPORT_TO_KOUREND, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.WATER_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2, 4, 5})));
        Spells.add(new SpellInfo("Earth Wave", SpriteID.SPELL_EARTH_WAVE, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.EARTH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 7, 5})));
        Spells.add(new SpellInfo("Enfeeble", SpriteID.SPELL_ENFEEBLE, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 8, 8})));
        Spells.add(new SpellInfo("Teleother Lumbridge", SpriteID.SPELL_TELEOTHER_LUMBRIDGE, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Fire Wave", SpriteID.SPELL_FIRE_WAVE, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE}, new int[] {1, 7, 5})));
        Spells.add(new SpellInfo("Entangle", SpriteID.SPELL_ENTANGLE, new SpellCost(new int[] {ItemID.NATURE_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {4, 5, 5})));
        Spells.add(new SpellInfo("Stun", SpriteID.SPELL_STUN, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 12, 12})));
        Spells.add(new SpellInfo("Charge", SpriteID.SPELL_CHARGE, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {3, 3, 3})));
        Spells.add(new SpellInfo("Wind Surge", SpriteID.SPELL_WIND_SURGE, new SpellCost(new int[] {ItemID.WRATH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 7})));
        Spells.add(new SpellInfo("Teleother Falador", SpriteID.SPELL_TELEOTHER_FALADOR, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.WATER_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Water Surge", SpriteID.SPELL_WATER_SURGE, new SpellCost(new int[] {ItemID.WRATH_RUNE, ItemID.WATER_RUNE, ItemID.AIR_RUNE}, new int[] {1, 10, 7})));
        Spells.add(new SpellInfo("Tele Block", SpriteID.SPELL_TELE_BLOCK, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Teleport to Target", SpriteID.SPELL_TELEPORT_TO_BOUNTY_TARGET, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Lvl-6 Enchant", SpriteID.SPELL_LVL_6_ENCHANT, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.EARTH_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 20, 20})));
        Spells.add(new SpellInfo("Teleother Camelot", SpriteID.SPELL_TELEOTHER_CAMELOT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {1, 2})));
        Spells.add(new SpellInfo("Earth Surge", SpriteID.SPELL_EARTH_SURGE, new SpellCost(new int[] {ItemID.WRATH_RUNE, ItemID.EARTH_RUNE, ItemID.AIR_RUNE}, new int[] {1, 10, 7})));
        Spells.add(new SpellInfo("Lvl-7 Enchant", SpriteID.SPELL_LVL_7_ENCHANT, new SpellCost(new int[] {ItemID.COSMIC_RUNE, ItemID.SOUL_RUNE, ItemID.BLOOD_RUNE}, new int[] {1, 20, 20})));
        Spells.add(new SpellInfo("Fire Surge", SpriteID.SPELL_FIRE_SURGE, new SpellCost(new int[] {ItemID.WRATH_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE}, new int[] {1, 10, 7})));

        // Ancients Spellbook

        // Edgeville Home Teleport
        Spells.add(new SpellInfo("Smoke Rush", SpriteID.SPELL_SMOKE_RUSH, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2, 1, 1})));
        Spells.add(new SpellInfo("Shadow Rush", SpriteID.SPELL_SHADOW_RUSH, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.SOUL_RUNE, ItemID.AIR_RUNE}, new int[] {2, 2, 1, 1})));
        Spells.add(new SpellInfo("Paddewwa Teleport", SpriteID.SPELL_PADDEWWA_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 1, 1})));
        Spells.add(new SpellInfo("Blood Rush", SpriteID.SPELL_BLOOD_RUSH, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 2, 1})));
        Spells.add(new SpellInfo("Ice Rush", SpriteID.SPELL_ICE_RUSH, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.WATER_RUNE}, new int[] {2, 2, 2})));
        Spells.add(new SpellInfo("Senntisten Teleport", SpriteID.SPELL_SENNTISTEN_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {2, 1})));
        Spells.add(new SpellInfo("Smoke Burst", SpriteID.SPELL_SMOKE_BURST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 4, 2, 2})));
        Spells.add(new SpellInfo("Shadow Burst", SpriteID.SPELL_SHADOW_BURST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.SOUL_RUNE, ItemID.AIR_RUNE}, new int[] {2, 4, 2, 1})));
        Spells.add(new SpellInfo("Kharyrll Teleport", SpriteID.SPELL_KHARYRLL_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 1})));
        Spells.add(new SpellInfo("Blood Burst", SpriteID.SPELL_BLOOD_BURST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 4, 2})));
        Spells.add(new SpellInfo("Ice Burst", SpriteID.SPELL_ICE_BURST, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE, ItemID.WATER_RUNE}, new int[] {2, 4, 4})));
        Spells.add(new SpellInfo("Lassar Teleport", SpriteID.SPELL_LASSAR_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.WATER_RUNE}, new int[] {2, 4})));
        Spells.add(new SpellInfo("Smoke Blitz", SpriteID.SPELL_SMOKE_BLITZ, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2, 2, 2})));
        Spells.add(new SpellInfo("Shadow Blitz", SpriteID.SPELL_SHADOW_BLITZ, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.SOUL_RUNE, ItemID.AIR_RUNE}, new int[] {2, 2, 2, 2})));
        Spells.add(new SpellInfo("Dareeyak Teleport", SpriteID.SPELL_DAREEYAK_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2, 3})));
        Spells.add(new SpellInfo("Blood Blitz", SpriteID.SPELL_BLOOD_BLITZ, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 4})));
        Spells.add(new SpellInfo("Ice Blitz", SpriteID.SPELL_ICE_BLITZ, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.WATER_RUNE}, new int[] {2, 2, 3})));
        Spells.add(new SpellInfo("Carrallangar Teleport", SpriteID.SPELL_CARRALLANGAR_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Teleport to Target", SpriteID.SPELL_TELEPORT_TO_BOUNTY_TARGET, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Smoke Barrage", SpriteID.SPELL_SMOKE_BARRAGE, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {4, 2, 4, 4})));
        Spells.add(new SpellInfo("Shadow Barrage", SpriteID.SPELL_SHADOW_BARRAGE, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.SOUL_RUNE, ItemID.AIR_RUNE}, new int[] {4, 2, 3, 4})));
        Spells.add(new SpellInfo("Annakarl Teleport", SpriteID.SPELL_ANNAKARL_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Blood Barrage", SpriteID.SPELL_BLOOD_BARRAGE, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.SOUL_RUNE}, new int[] {4, 4, 1})));
        Spells.add(new SpellInfo("Ice Barrage", SpriteID.SPELL_ICE_BARRAGE, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.WATER_RUNE}, new int[] {4, 2, 6})));
        Spells.add(new SpellInfo("Ghorrock Teleport", SpriteID.SPELL_GHORROCK_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.WATER_RUNE}, new int[] {2, 8})));

        //Normal Ancient Teleports compatibility (name map only)
        Spells.add(new SpellInfo("Edgeville Teleport", SpriteID.SPELL_PADDEWWA_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 1, 1})));
        Spells.add(new SpellInfo("Digsite Teleport", SpriteID.SPELL_SENNTISTEN_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {2, 1})));
        Spells.add(new SpellInfo("Canifis Teleport", SpriteID.SPELL_KHARYRLL_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 1})));
        Spells.add(new SpellInfo("Ice Mountain Teleport", SpriteID.SPELL_LASSAR_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.WATER_RUNE}, new int[] {2, 4})));
        Spells.add(new SpellInfo("Forgotten Cemetery Teleport", SpriteID.SPELL_DAREEYAK_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.AIR_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2, 3})));
        Spells.add(new SpellInfo("Graveyard of Shadows Teleport", SpriteID.SPELL_CARRALLANGAR_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Demonic Ruins Teleport", SpriteID.SPELL_ANNAKARL_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Frozen Waste Plateau Teleport", SpriteID.SPELL_GHORROCK_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.WATER_RUNE}, new int[] {2, 8})));

        // Lunar Spellbook

        // Lunar Home Teleport
        Spells.add(new SpellInfo("Bake Pie", SpriteID.SPELL_BAKE_PIE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 4, 5})));
        Spells.add(new SpellInfo("Geomancy", SpriteID.SPELL_GEOMANCY, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.NATURE_RUNE, ItemID.EARTH_RUNE}, new int[] {3, 3, 8})));
        Spells.add(new SpellInfo("Cure Plant", SpriteID.SPELL_CURE_PLANT, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 8})));
        Spells.add(new SpellInfo("Monster Examine", SpriteID.SPELL_MONSTER_EXAMINE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.COSMIC_RUNE, ItemID.MIND_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("NPC Contact", SpriteID.SPELL_NPC_CONTACT, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.COSMIC_RUNE, ItemID.AIR_RUNE}, new int[] {1, 1, 2})));
        Spells.add(new SpellInfo("Cure Other", SpriteID.SPELL_CURE_OTHER, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.LAW_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 1, 10})));
        Spells.add(new SpellInfo("Humidify", SpriteID.SPELL_HUMIDIFY, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 3, 1})));
        Spells.add(new SpellInfo("Moonclan Teleport", SpriteID.SPELL_MOONCLAN_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 2, 2})));
        Spells.add(new SpellInfo("Tele Group Moonclan", SpriteID.SPELL_TELE_GROUP_MOONCLAN, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 2, 4})));
        Spells.add(new SpellInfo("Cure Me", SpriteID.SPELL_CURE_ME, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.COSMIC_RUNE, ItemID.LAW_RUNE}, new int[] {2, 2, 1})));
        Spells.add(new SpellInfo("Ourania Teleport", SpriteID.SPELL_OURANIA_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 2, 6})));
        Spells.add(new SpellInfo("Hunter Kit", SpriteID.SPELL_HUNTER_KIT, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.EARTH_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Waterbirth Teleport", SpriteID.SPELL_WATERBIRTH_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {1, 2, 1})));
        Spells.add(new SpellInfo("Tele Group Waterbirth", SpriteID.SPELL_TELE_GROUP_WATERBIRTH, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {1, 2, 5})));
        Spells.add(new SpellInfo("Cure Group", SpriteID.SPELL_CURE_GROUP, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.COSMIC_RUNE, ItemID.LAW_RUNE}, new int[] {2, 2, 2})));
        Spells.add(new SpellInfo("Stat Spy", SpriteID.SPELL_STAT_SPY, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.COSMIC_RUNE, ItemID.BODY_RUNE}, new int[] {2, 2, 5})));
        Spells.add(new SpellInfo("Barbarian Teleport", SpriteID.SPELL_BARBARIAN_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2, 3})));
        Spells.add(new SpellInfo("Tele Group Barbarian", SpriteID.SPELL_TELE_GROUP_BARBARIAN, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 2, 6})));
        Spells.add(new SpellInfo("Spin Flax", SpriteID.SPELL_SPIN_FLAX, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.NATURE_RUNE, ItemID.AIR_RUNE}, new int[] {1, 2, 5})));
        Spells.add(new SpellInfo("Superglass Make", SpriteID.SPELL_SUPERGLASS_MAKE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE}, new int[] {2, 6, 10})));
        Spells.add(new SpellInfo("Tan Leather", SpriteID.SPELL_TAN_LEATHER, new SpellCost(new int[] {ItemID.FIRE_RUNE, ItemID.ASTRAL_RUNE, ItemID.NATURE_RUNE}, new int[] {5, 2, 1})));
        Spells.add(new SpellInfo("Khazard Teleport", SpriteID.SPELL_KHAZARD_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {2, 2, 4})));
        Spells.add(new SpellInfo("Tele Group Khazard", SpriteID.SPELL_TELE_GROUP_KHAZARD, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {2, 2, 8})));
        Spells.add(new SpellInfo("Dream", SpriteID.SPELL_DREAM, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.COSMIC_RUNE, ItemID.BODY_RUNE}, new int[] {2, 1, 5})));
        Spells.add(new SpellInfo("String Jewellery", SpriteID.SPELL_STRING_JEWELLERY, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {2, 5, 10})));
        Spells.add(new SpellInfo("Stat Restore Pot Share", SpriteID.SPELL_STAT_RESTORE_POT_SHARE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {2, 10, 10})));
        Spells.add(new SpellInfo("Magic Imbue", SpriteID.SPELL_MAGIC_IMBUE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 7, 7})));
        Spells.add(new SpellInfo("Fertile Soil", SpriteID.SPELL_FERTILE_SOIL, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.NATURE_RUNE, ItemID.EARTH_RUNE}, new int[] {3, 2, 15})));
        Spells.add(new SpellInfo("Boost Potion Share", SpriteID.SPELL_BOOST_POTION_SHARE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {3, 10, 12})));
        Spells.add(new SpellInfo("Fishing Guild Teleport", SpriteID.SPELL_FISHING_GUILD_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {3, 3, 10})));
        Spells.add(new SpellInfo("Teleport to Target", SpriteID.SPELL_TELEPORT_TO_BOUNTY_TARGET, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.DEATH_RUNE, ItemID.CHAOS_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Tele Group Fishing Guild", SpriteID.SPELL_TELE_GROUP_FISHING_GUILD, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {3, 3, 14})));
        Spells.add(new SpellInfo("Plank Make", SpriteID.SPELL_PLANK_MAKE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.NATURE_RUNE, ItemID.EARTH_RUNE}, new int[] {2, 1, 15})));
        Spells.add(new SpellInfo("Catherby Teleport", SpriteID.SPELL_CATHERBY_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {3, 3, 10})));
        Spells.add(new SpellInfo("Tele Group Catherby", SpriteID.SPELL_TELE_GROUP_CATHERBY, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {3, 3, 15})));
        Spells.add(new SpellInfo("Recharge Dragonstone", SpriteID.SPELL_RECHARGE_DRAGONSTONE, new SpellCost(new int[] {ItemID.WATER_RUNE, ItemID.ASTRAL_RUNE, ItemID.SOUL_RUNE}, new int[] {4, 1, 1})));
        Spells.add(new SpellInfo("Ice Plateau Teleport", SpriteID.SPELL_ICE_PLATEAU_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {3, 3, 8})));
        Spells.add(new SpellInfo("Tele Group Ice Plateau", SpriteID.SPELL_TELE_GROUP_ICE_PLATEAU, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.ASTRAL_RUNE, ItemID.WATER_RUNE}, new int[] {3, 3, 16})));
        Spells.add(new SpellInfo("Energy Transfer", SpriteID.SPELL_ENERGY_TRANSFER, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.LAW_RUNE, ItemID.NATURE_RUNE}, new int[] {3, 2, 1})));
        Spells.add(new SpellInfo("Heal Other", SpriteID.SPELL_HEAL_OTHER, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.LAW_RUNE, ItemID.BLOOD_RUNE}, new int[] {3, 3, 1})));
        Spells.add(new SpellInfo("Vengeance Other", SpriteID.SPELL_VENGEANCE_OTHER, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.DEATH_RUNE, ItemID.EARTH_RUNE}, new int[] {3, 2, 10})));
        Spells.add(new SpellInfo("Vengeance", SpriteID.SPELL_VENGEANCE, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.DEATH_RUNE, ItemID.EARTH_RUNE}, new int[] {4, 2, 10})));
        Spells.add(new SpellInfo("Heal Group", SpriteID.SPELL_HEAL_GROUP, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.LAW_RUNE, ItemID.BLOOD_RUNE}, new int[] {4, 6, 3})));
        Spells.add(new SpellInfo("Spellbook Swap", SpriteID.SPELL_SPELLBOOK_SWAP, new SpellCost(new int[] {ItemID.ASTRAL_RUNE, ItemID.COSMIC_RUNE, ItemID.LAW_RUNE}, new int[] {3, 2, 1})));

        // Arceuus spellbook

        // Arceuus Home Teleport
        Spells.add(new SpellInfo("Arceuus Library Teleport", SpriteID.SPELL_ARCEUUS_LIBRARY_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 2})));
        Spells.add(new SpellInfo("Basic Reanimation", SpriteID.SPELL_BASIC_REANIMATION, new SpellCost(new int[] {ItemID.BODY_RUNE, ItemID.NATURE_RUNE}, new int[] {4, 2})));
        Spells.add(new SpellInfo("Draynor Manor Teleport", SpriteID.SPELL_DRAYNOR_MANOR_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.WATER_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Battlefront Teleport", 1255, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.EARTH_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Mind Altar Teleport", SpriteID.SPELL_MIND_ALTAR_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.MIND_RUNE}, new int[] {1, 2})));
        Spells.add(new SpellInfo("Respawn Teleport", SpriteID.SPELL_RESPAWN_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {1, 1})));
        Spells.add(new SpellInfo("Ghostly Grasp", SpriteID.SPELL_GHOSTLY_GRASP, new SpellCost(new int[] {ItemID.CHAOS_RUNE, ItemID.AIR_RUNE}, new int[] {1, 4})));
        Spells.add(new SpellInfo("Resurrect Lesser Ghost", SpriteID.SPELL_RESURRECT_LESSER_GHOST, new SpellCost(new int[] {ItemID.MIND_RUNE, ItemID.AIR_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Resurrect Lesser Skeleton", 1271, new SpellCost(new int[] {ItemID.MIND_RUNE, ItemID.AIR_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Resurrect Lesser Zombie", 1300, new SpellCost(new int[] {ItemID.MIND_RUNE, ItemID.AIR_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Salve Graveyard Teleport", SpriteID.SPELL_SALVE_GRAVEYARD_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {1, 2})));
        Spells.add(new SpellInfo("Adept Reanimation", SpriteID.SPELL_ADEPT_REANIMATION, new SpellCost(new int[] {ItemID.BODY_RUNE, ItemID.NATURE_RUNE, ItemID.SOUL_RUNE}, new int[] {4, 3, 1})));
        Spells.add(new SpellInfo("Inferior Demonbane", SpriteID.SPELL_INFERIOR_DEMONBANE, new SpellCost(new int[] {ItemID.CHAOS_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 4})));
        Spells.add(new SpellInfo("Shadow Veil", SpriteID.SPELL_SHADOW_VEIL, new SpellCost(new int[] {ItemID.EARTH_RUNE, ItemID.FIRE_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 5, 5})));
        Spells.add(new SpellInfo("Fenkenstrain's Castle Teleport", SpriteID.SPELL_FENKENSTRAINS_CASTLE_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Dark Lure", SpriteID.SPELL_DARK_LURE, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.NATURE_RUNE}, new int[] {1, 1})));
        Spells.add(new SpellInfo("Skeletal Grasp", SpriteID.SPELL_SKELETAL_GRASP, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.EARTH_RUNE}, new int[] {1, 8})));
        Spells.add(new SpellInfo("Resurrect Superior Ghost", 2979, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.EARTH_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Resurrect Superior Skeleton", SpriteID.SPELL_RESURRECT_SUPERIOR_SKELETON, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.EARTH_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Resurrect Superior Zombie", 2983, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.EARTH_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Mark of Darkness", SpriteID.SPELL_MARK_OF_DARKNESS, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.COSMIC_RUNE}, new int[] {1, 1})));
        Spells.add(new SpellInfo("West Ardougne Teleport", SpriteID.SPELL_WEST_ARDOUGNE_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE}, new int[] {2, 2})));
        Spells.add(new SpellInfo("Superior Demonbane", SpriteID.SPELL_SUPERIOR_DEMONBANE, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 8})));
        Spells.add(new SpellInfo("Lesser Corruption", SpriteID.SPELL_LESSER_CORRUPTION, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.SOUL_RUNE}, new int[] {1, 2})));
        Spells.add(new SpellInfo("Harmony Island Teleport", SpriteID.SPELL_HARMONY_ISLAND_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.NATURE_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Vile Vigour", SpriteID.SPELL_VILE_VIGOUR, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.AIR_RUNE}, new int[] {1, 3})));
        Spells.add(new SpellInfo("Degrime", SpriteID.SPELL_DEGRIME, new SpellCost(new int[] {ItemID.EARTH_RUNE, ItemID.NATURE_RUNE}, new int[] {4, 2})));
        Spells.add(new SpellInfo("Cemetery Teleport", SpriteID.SPELL_CEMETERY_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.BLOOD_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Expert Reanimation", SpriteID.SPELL_EXPERT_REANIMATION, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.NATURE_RUNE, ItemID.SOUL_RUNE}, new int[] {1, 3, 2})));
        Spells.add(new SpellInfo("Ward of Arceuus", SpriteID.SPELL_WARD_OF_ARCEUUS, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.NATURE_RUNE, ItemID.COSMIC_RUNE}, new int[] {4, 2, 1})));
        Spells.add(new SpellInfo("Resurrect Greater Ghost", 2980, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.FIRE_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Resurrect Greater Skeleton", 2982, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.FIRE_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Resurrect Greater Zombie", SpriteID.SPELL_RESURRECT_GREATER_ZOMBIE, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.FIRE_RUNE, ItemID.COSMIC_RUNE}, new int[] {5, 10, 1})));
        Spells.add(new SpellInfo("Resurrect Crops", SpriteID.SPELL_RESURRECT_CROPS, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.NATURE_RUNE, ItemID.BLOOD_RUNE, ItemID.EARTH_RUNE}, new int[] {8, 12, 8, 25})));
        Spells.add(new SpellInfo("Undead Grasp", SpriteID.SPELL_UNDEAD_GRASP, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.FIRE_RUNE}, new int[] {1, 12})));
        Spells.add(new SpellInfo("Death Charge", SpriteID.SPELL_DEATH_CHARGE, new SpellCost(new int[] {ItemID.DEATH_RUNE, ItemID.BLOOD_RUNE, ItemID.SOUL_RUNE}, new int[] {1, 1, 1})));
        Spells.add(new SpellInfo("Dark Demonbane", SpriteID.SPELL_DARK_DEMONBANE, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.FIRE_RUNE}, new int[] {2, 12})));
        Spells.add(new SpellInfo("Barrows Teleport", SpriteID.SPELL_BARROWS_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 2, 1})));
        Spells.add(new SpellInfo("Demonic Offering", SpriteID.SPELL_DEMONIC_OFFERING, new SpellCost(new int[] {ItemID.SOUL_RUNE, ItemID.WRATH_RUNE}, new int[] {1, 1})));
        Spells.add(new SpellInfo("Greater Corruption", SpriteID.SPELL_GREATER_CORRUPTION, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.SOUL_RUNE}, new int[] {1, 3})));
        Spells.add(new SpellInfo("Master Reanimation", SpriteID.SPELL_MASTER_REANIMATION, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.NATURE_RUNE, ItemID.SOUL_RUNE}, new int[] {2, 4, 4})));
        Spells.add(new SpellInfo("Ape Atoll Teleport", SpriteID.SPELL_APE_ATOLL_TELEPORT, new SpellCost(new int[] {ItemID.LAW_RUNE, ItemID.SOUL_RUNE, ItemID.BLOOD_RUNE}, new int[] {2, 2, 2})));
        Spells.add(new SpellInfo("Sinister Offering", SpriteID.SPELL_SINISTER_OFFERING, new SpellCost(new int[] {ItemID.BLOOD_RUNE, ItemID.WRATH_RUNE}, new int[] {1, 1})));

        // Some keys do collide (some spells name/sprite) - no longer bothering to map through cost because unlimited rune supply complicates things.
        // We remove existing sprite keys and ignore them because we don't want the first occurrence mapped if it's not unique.
        // Don't need to remove/ignore keys from name map, because we only use that for tooltips and the rune costs are the same regardless (as of writing, only Teleport to Target is shared).

        // SpellProductMap currently only used for detecting Enchant crossbow bolts.

        final List<Integer> spriteIgnore = new ArrayList<Integer>();

        Spells.forEach(s -> {
            final SpellCost cost = s.getSpellCost();
            final int spriteId = s.getSpriteId();
            final String name = s.getName();

            if (s.getSpellProducts() != null)
            {
                final SpellProduct[] products = s.getSpellProducts();
                for (SpellProduct p : products)
                    SpellProductMap.put(p, s);
            }

            if (!spriteIgnore.contains(spriteId))
            {
                if (!SpellSpriteMap.containsKey(spriteId))
                {
                    SpellSpriteMap.put(spriteId, s);
                }
                else
                {
                    SpellSpriteMap.remove(spriteId);
                    spriteIgnore.add(spriteId);
                }
            }

            if (!SpellNameMap.containsKey(name))
                SpellNameMap.put(name.toLowerCase(), s);
        });
    }
}