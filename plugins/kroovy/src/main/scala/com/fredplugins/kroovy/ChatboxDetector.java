package com.fredplugins.kroovy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.ScriptEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import scala.Int;
import scala.Tuple2;
import scala.collection.immutable.Seq;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;

import static net.runelite.api.ItemID.*;

/**
 * Detects actions initiated from the chatbox crafting interface (Eg: Fletching, Glassblowing, Leather-work)
 */
@Slf4j
@Singleton
public class ChatboxDetector extends ActionDetector {

	static {
		((Logger) log).setLevel(Level.DEBUG);
	}

	/**
	 * Indicates how many items are to be created in the crafting dialogue.
	 */
	private static final int VAR_MAKE_AMOUNT = 200;

	/**
	 * Indicates the selected product in the crafting dialogue.
	 */
	private static final int VAR_SELECTED_INDEX = 2673;

	private static final int WIDGET_MAKE_PARENT = 270;

	private static final int WIDGET_MAKE_QUESTION = 5;
	private static final int WIDGET_MAKE_SLOT_START = 14;
	private static final int WIDGET_MAKE_SLOT_COUNT = 9;
	private static final int WIDGET_MAKE_SLOT_ITEM = 38;

	private static final int WIDGET_ID_CHATBOX_FIRST_MAKE_BUTTON = 17694734;

	private static final int MAKE_X_SETUP = 2046;
	private static final int MAKE_X_BUTTON_CLICK = 2050;
	private static final int MAKE_X_BUTTON_KEY = 2051;
	private static final int MAKE_X_BUTTON_TRIGGERED = 2052;

	private static final Product[] MULTI_MATERIAL_PRODUCTS = {
		// @formatter:off
			new Product(ActionEnum.CRAFT_LEATHER, GREEN_DHIDE_BODY, new Ingredient(GREEN_DRAGON_LEATHER, 3)),
			new Product(ActionEnum.CRAFT_LEATHER, GREEN_DHIDE_CHAPS, new Ingredient(GREEN_DRAGON_LEATHER, 2)),
			new Product(ActionEnum.CRAFT_LEATHER, BLUE_DHIDE_BODY, new Ingredient(BLUE_DRAGON_LEATHER, 3)),
			new Product(ActionEnum.CRAFT_LEATHER, BLUE_DHIDE_CHAPS, new Ingredient(BLUE_DRAGON_LEATHER, 2)),
			new Product(ActionEnum.CRAFT_LEATHER, RED_DHIDE_BODY, new Ingredient(RED_DRAGON_LEATHER, 3)),
			new Product(ActionEnum.CRAFT_LEATHER, RED_DHIDE_CHAPS, new Ingredient(RED_DRAGON_LEATHER, 2)),
			new Product(ActionEnum.CRAFT_LEATHER, BLACK_DHIDE_BODY, new Ingredient(BLACK_DRAGON_LEATHER, 3)),
			new Product(ActionEnum.CRAFT_LEATHER, BLACK_DHIDE_CHAPS, new Ingredient(BLACK_DRAGON_LEATHER, 2)),
			new Product(ActionEnum.CRAFT_LEATHER, SNAKESKIN_BANDANA, new Ingredient(SNAKESKIN, 5)),
			new Product(ActionEnum.CRAFT_LEATHER, SNAKESKIN_BODY, new Ingredient(SNAKESKIN, 15)),
			new Product(ActionEnum.CRAFT_LEATHER, SNAKESKIN_BOOTS, new Ingredient(SNAKESKIN, 6)),
			new Product(ActionEnum.CRAFT_LEATHER, SNAKESKIN_CHAPS, new Ingredient(SNAKESKIN, 12)),
			new Product(ActionEnum.CRAFT_LEATHER, SNAKESKIN_VAMBRACES, new Ingredient(SNAKESKIN, 8)),
			new Product(ActionEnum.CRAFT_LEATHER, XERICIAN_HAT, new Ingredient(XERICIAN_FABRIC, 3)),
			new Product(ActionEnum.CRAFT_LEATHER, XERICIAN_TOP, new Ingredient(XERICIAN_FABRIC, 5)),
			new Product(ActionEnum.CRAFT_LEATHER, XERICIAN_ROBE, new Ingredient(XERICIAN_FABRIC, 4)),
			new Product(ActionEnum.CRAFT_LEATHER, XERICIAN_ROBE, new Ingredient(XERICIAN_FABRIC, 4)),
			new Product(ActionEnum.CRAFT_LEATHER, LEATHER_GLOVES, new Ingredient(LEATHER)),
			new Product(ActionEnum.CRAFT_LEATHER, LEATHER_BOOTS, new Ingredient(LEATHER)),
			new Product(ActionEnum.CRAFT_LEATHER, LEATHER_COWL, new Ingredient(LEATHER)),
			new Product(ActionEnum.CRAFT_LEATHER, LEATHER_VAMBRACES, new Ingredient(LEATHER)),
			new Product(ActionEnum.CRAFT_LEATHER, LEATHER_BODY, new Ingredient(LEATHER)),
			new Product(ActionEnum.CRAFT_LEATHER, LEATHER_CHAPS, new Ingredient(LEATHER)),
			new Product(ActionEnum.CRAFT_LEATHER, COIF, new Ingredient(LEATHER)),
			new Product(ActionEnum.CRAFT_HARD_LEATHER, HARDLEATHER_BODY, new Ingredient(HARD_LEATHER, 1)),
			new Product(ActionEnum.CRAFT_BATTLESTAVES, AIR_BATTLESTAFF, new Ingredient(AIR_ORB), new Ingredient(BATTLESTAFF)),
			new Product(ActionEnum.CRAFT_BATTLESTAVES, FIRE_BATTLESTAFF, new Ingredient(FIRE_ORB), new Ingredient(BATTLESTAFF)),
			new Product(ActionEnum.CRAFT_BATTLESTAVES, EARTH_BATTLESTAFF, new Ingredient(EARTH_ORB), new Ingredient(BATTLESTAFF)),
			new Product(ActionEnum.CRAFT_BATTLESTAVES, WATER_BATTLESTAFF, new Ingredient(WATER_ORB), new Ingredient(BATTLESTAFF)),
			new Product(ActionEnum.SMELTING, BRONZE_BAR, new Ingredient(TIN_ORE), new Ingredient(COPPER_ORE)),
			new Product(ActionEnum.SMELTING, IRON_BAR, new Ingredient(IRON_ORE)),
			new Product(ActionEnum.SMELTING, SILVER_BAR, new Ingredient(SILVER_ORE)),
			new Product(ActionEnum.SMELTING, STEEL_BAR, new Ingredient(IRON_ORE), new Ingredient(COAL, 2)),
			new Product(ActionEnum.SMELTING, GOLD_BAR, new Ingredient(GOLD_ORE)),
			new Product(ActionEnum.SMELTING, MITHRIL_BAR, new Ingredient(MITHRIL_ORE), new Ingredient(COAL, 4)),
			new Product(ActionEnum.SMELTING, ADAMANTITE_BAR, new Ingredient(ADAMANTITE_ORE), new Ingredient(COAL, 6)),
			new Product(ActionEnum.SMELTING, RUNITE_BAR, new Ingredient(RUNITE_ORE), new Ingredient(COAL, 8)),
			new Product(ActionEnum.SMELTING_CANNONBALLS, CANNONBALL, new Ingredient[]{new Ingredient(STEEL_BAR)}, new Ingredient(AMMO_MOULD)),
			new Product(ActionEnum.SMELTING_CANNONBALLS, CANNONBALL, new Ingredient[]{new Ingredient(STEEL_BAR)}, new Ingredient(DOUBLE_AMMO_MOULD)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, OPAL, true, new Ingredient(UNCUT_OPAL)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, JADE, true, new Ingredient(UNCUT_JADE)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, RED_TOPAZ, true, new Ingredient(UNCUT_RED_TOPAZ)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, SAPPHIRE, true, new Ingredient(UNCUT_SAPPHIRE)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, EMERALD, true, new Ingredient(UNCUT_EMERALD)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, RUBY, true, new Ingredient(UNCUT_RUBY)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, DIAMOND, true, new Ingredient(UNCUT_DIAMOND)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, DRAGONSTONE, true, new Ingredient(UNCUT_DRAGONSTONE)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, ONYX, true, new Ingredient(UNCUT_ONYX)),
			new Product(ActionEnum.CRAFT_CUT_GEMS, ZENYTE, true, new Ingredient(UNCUT_ZENYTE)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, STRUNG_RABBIT_FOOT, new Ingredient(RABBIT_FOOT), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, HOLY_SYMBOL, new Ingredient(UNSTRUNG_SYMBOL), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, UNHOLY_SYMBOL, new Ingredient(UNSTRUNG_EMBLEM), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, OPAL_AMULET, new Ingredient(OPAL_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, JADE_AMULET, new Ingredient(JADE_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, SAPPHIRE_AMULET, new Ingredient(SAPPHIRE_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, TOPAZ_AMULET, new Ingredient(TOPAZ_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, EMERALD_AMULET, new Ingredient(EMERALD_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, RUBY_AMULET, new Ingredient(RUBY_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, GOLD_AMULET, new Ingredient(GOLD_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, DIAMOND_AMULET, new Ingredient(DIAMOND_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, DRAGONSTONE_AMULET, new Ingredient(DRAGONSTONE_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, ONYX_AMULET, new Ingredient(ONYX_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_STRING_JEWELLERY, ZENYTE_AMULET, new Ingredient(ZENYTE_AMULET_U), new Ingredient(BALL_OF_WOOL)),
			new Product(ActionEnum.CRAFT_MOLTEN_GLASS, MOLTEN_GLASS, new Ingredient(BUCKET_OF_SAND), new Ingredient(SODA_ASH)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, BEER_GLASS, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, EMPTY_CANDLE_LANTERN, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, EMPTY_OIL_LAMP, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, VIAL, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, EMPTY_FISHBOWL, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, UNPOWERED_ORB, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, LANTERN_LENS, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_BLOW_GLASS, EMPTY_LIGHT_ORB, new Ingredient(MOLTEN_GLASS)),
			new Product(ActionEnum.CRAFT_LOOM, BASKET, new Ingredient(WILLOW_BRANCH, 6)),
			new Product(ActionEnum.CRAFT_LOOM, EMPTY_SACK, new Ingredient(JUTE_FIBRE, 4)),
			new Product(ActionEnum.CRAFT_LOOM_DRIFT_NET, DRIFT_NET, new Ingredient(JUTE_FIBRE, 2)),
			new Product(ActionEnum.CRAFT_LOOM, STRIP_OF_CLOTH, new Ingredient(BALL_OF_WOOL, 4)),
			new Product(ActionEnum.CRAFT_SHIELD, HARD_LEATHER_SHIELD, new Ingredient(GREEN_DRAGON_LEATHER, 2), new Ingredient (MAPLE_SHIELD), new Ingredient(BRONZE_NAILS, 15)),
			new Product(ActionEnum.CRAFT_SHIELD, SNAKESKIN_SHIELD, new Ingredient(SNAKESKIN, 2), new Ingredient (WILLOW_SHIELD), new Ingredient(IRON_NAILS, 15)),
			new Product(ActionEnum.CRAFT_SHIELD, GREEN_DHIDE_SHIELD, new Ingredient(GREEN_DRAGON_LEATHER, 2), new Ingredient (MAPLE_SHIELD), new Ingredient(STEEL_NAILS, 15)),
			new Product(ActionEnum.CRAFT_SHIELD, BLUE_DHIDE_SHIELD, new Ingredient(BLUE_DRAGON_LEATHER, 2), new Ingredient (YEW_SHIELD), new Ingredient(MITHRIL_NAILS, 15)),
			new Product(ActionEnum.CRAFT_SHIELD, RED_DHIDE_SHIELD, new Ingredient(RED_DRAGON_LEATHER, 2), new Ingredient (MAGIC_SHIELD), new Ingredient(ADAMANTITE_NAILS, 15)),
			new Product(ActionEnum.CRAFT_SHIELD, BLACK_DHIDE_SHIELD, new Ingredient(BLACK_DRAGON_LEATHER, 2), new Ingredient (REDWOOD_SHIELD), new Ingredient(RUNE_NAILS, 15)),
			new Product(ActionEnum.FLETCH_CUT_BOW, LONGBOW_U, new Ingredient(LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, OAK_LONGBOW_U, new Ingredient(OAK_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, WILLOW_LONGBOW_U, new Ingredient(WILLOW_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, MAPLE_LONGBOW_U, new Ingredient(MAPLE_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, YEW_LONGBOW_U, new Ingredient(YEW_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, MAGIC_LONGBOW_U, new Ingredient(MAGIC_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, SHORTBOW_U, new Ingredient(LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, OAK_SHORTBOW_U, new Ingredient(OAK_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, WILLOW_SHORTBOW_U, new Ingredient(WILLOW_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, MAPLE_SHORTBOW_U, new Ingredient(MAPLE_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, YEW_SHORTBOW_U, new Ingredient(YEW_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_BOW, MAGIC_SHORTBOW_U, new Ingredient(MAGIC_LOGS)),
			new Product(ActionEnum.FLETCH_STRING_BOW, LONGBOW, new Ingredient(LONGBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, OAK_LONGBOW, new Ingredient(OAK_LONGBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, WILLOW_LONGBOW, new Ingredient(WILLOW_LONGBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, MAPLE_LONGBOW, new Ingredient(MAPLE_LONGBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, YEW_LONGBOW, new Ingredient(YEW_LONGBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, MAGIC_LONGBOW, new Ingredient(MAGIC_LONGBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, SHORTBOW, new Ingredient(SHORTBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, OAK_SHORTBOW, new Ingredient(OAK_SHORTBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, WILLOW_SHORTBOW, new Ingredient(WILLOW_SHORTBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, MAPLE_SHORTBOW, new Ingredient(MAPLE_SHORTBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, YEW_SHORTBOW, new Ingredient(YEW_SHORTBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_BOW, MAGIC_SHORTBOW, new Ingredient(MAGIC_SHORTBOW_U), new Ingredient(BOW_STRING)),
			new Product(ActionEnum.FLETCH_SPINNING, BOW_STRING, new Ingredient(FLAX)),
			new Product(ActionEnum.FLETCH_SHIELD, OAK_SHIELD, new Ingredient(OAK_LOGS, 2)),
			new Product(ActionEnum.FLETCH_SHIELD, WILLOW_SHIELD, new Ingredient(WILLOW_LOGS, 2)),
			new Product(ActionEnum.FLETCH_SHIELD, MAPLE_SHIELD, new Ingredient(MAPLE_LOGS, 2)),
			new Product(ActionEnum.FLETCH_SHIELD, YEW_SHIELD, new Ingredient(YEW_LOGS, 2)),
			new Product(ActionEnum.FLETCH_SHIELD, MAGIC_SHIELD, new Ingredient(MAGIC_LOGS, 2)),
			new Product(ActionEnum.FLETCH_SHIELD, REDWOOD_SHIELD, new Ingredient(REDWOOD_LOGS, 2)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, WOODEN_STOCK, new Ingredient(LOGS)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, OAK_STOCK, new Ingredient(OAK_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, WILLOW_STOCK, new Ingredient(WILLOW_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, TEAK_STOCK, new Ingredient(TEAK_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, MAPLE_STOCK, new Ingredient(MAPLE_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, MAHOGANY_STOCK, new Ingredient(MAHOGANY_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, YEW_STOCK, new Ingredient(YEW_LOGS)),
			new Product(ActionEnum.FLETCH_CUT_CROSSBOW, MAGIC_STOCK, new Ingredient(MAGIC_LOGS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, ItemID.BRONZE_CROSSBOW_U, new Ingredient(ItemID.WOODEN_STOCK), new Ingredient(BRONZE_LIMBS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, ItemID.BLURITE_CROSSBOW_U, new Ingredient(ItemID.OAK_STOCK), new Ingredient(BLURITE_LIMBS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, ItemID.IRON_CROSSBOW_U, new Ingredient(ItemID.WILLOW_STOCK), new Ingredient(IRON_LIMBS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, ItemID.STEEL_CROSSBOW_U, new Ingredient(ItemID.TEAK_STOCK), new Ingredient(STEEL_LIMBS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, ItemID.MITHRIL_CROSSBOW_U, new Ingredient(ItemID.MAPLE_STOCK), new Ingredient(MITHRIL_LIMBS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, ItemID.ADAMANT_CROSSBOW_U, new Ingredient(ItemID.MAHOGANY_STOCK), new Ingredient(ADAMANTITE_LIMBS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, RUNITE_CROSSBOW_U, new Ingredient(ItemID.YEW_STOCK), new Ingredient(RUNITE_LIMBS)),
			new Product(ActionEnum.FLETCH_ATTACH_CROSSBOW, ItemID.DRAGON_CROSSBOW_U, new Ingredient(ItemID.MAGIC_STOCK), new Ingredient(DRAGON_LIMBS)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.BRONZE_CROSSBOW, new Ingredient(ItemID.BRONZE_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.BLURITE_CROSSBOW, new Ingredient(ItemID.BLURITE_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.IRON_CROSSBOW, new Ingredient(ItemID.IRON_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.STEEL_CROSSBOW, new Ingredient(ItemID.STEEL_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.MITHRIL_CROSSBOW, new Ingredient(ItemID.MITHRIL_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.ADAMANT_CROSSBOW,new Ingredient(ItemID.ADAMANT_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.RUNE_CROSSBOW, new Ingredient(RUNITE_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_STRING_CROSSBOW, ItemID.DRAGON_CROSSBOW, new Ingredient(ItemID.DRAGON_CROSSBOW_U), new Ingredient(CROSSBOW_STRING)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, DIAMOND_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, DRAGONSTONE_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, EMERALD_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, JADE_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, ONYX_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, OPAL_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, PEARL_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, RUBY_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, SAPPHIRE_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_ATTACH_TIPS, TOPAZ_DRAGON_BOLTS, new Ingredient(DRAGON_BOLTS, 10), new Ingredient(DIAMOND_BOLT_TIPS, 10)),
			new Product(ActionEnum.FLETCH_DART, BRONZE_DART, new Ingredient(BRONZE_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FLETCH_DART, IRON_DART, new Ingredient(IRON_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FLETCH_DART, STEEL_DART, new Ingredient(STEEL_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FLETCH_DART, MITHRIL_DART, new Ingredient(MITHRIL_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FLETCH_DART, ADAMANT_DART, new Ingredient(ADAMANT_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FLETCH_DART, RUNE_DART, new Ingredient(RUNE_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FLETCH_DART, AMETHYST_DART, new Ingredient(AMETHYST_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FLETCH_DART, DRAGON_DART, new Ingredient(DRAGON_DART_TIP, 10), new Ingredient(FEATHER, 10)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, ACHEY_TREE_LOGS, new Ingredient(ACHEY_TREE_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, LOGS, new Ingredient(LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, OAK_LOGS, new Ingredient(OAK_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, WILLOW_LOGS, new Ingredient(WILLOW_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, TEAK_LOGS, new Ingredient(TEAK_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, ARCTIC_PINE_LOGS, new Ingredient(ARCTIC_PINE_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, MAPLE_LOGS, new Ingredient(MAPLE_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, MAHOGANY_LOGS, new Ingredient(MAHOGANY_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, YEW_LOGS, new Ingredient(YEW_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, BLISTERWOOD_LOGS, new Ingredient(BLISTERWOOD_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, MAGIC_LOGS, new Ingredient(MAGIC_LOGS)),
			new Product(ActionEnum.FIREMAKING_CAMPFIRE, REDWOOD_LOGS, new Ingredient(REDWOOD_LOGS)),
			new Product(ActionEnum.FLETCH_ATTACH, OPAL_BOLTS, new Ingredient(BRONZE_BOLTS,10) , new Ingredient(OPAL_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, JADE_BOLTS, new Ingredient(BLURITE_BOLTS,10) , new Ingredient(JADE_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, PEARL_BOLTS, new Ingredient(IRON_BOLTS,10) , new Ingredient(PEARL_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, TOPAZ_BOLTS, new Ingredient(STEEL_BOLTS,10) , new Ingredient(TOPAZ_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, BARBED_BOLTS, new Ingredient(BRONZE_BOLTS, 1) , new Ingredient(BARB_BOLTTIPS,1)),
			new Product(ActionEnum.FLETCH_ATTACH, SAPPHIRE_BOLTS, new Ingredient(MITHRIL_BOLTS,10) , new Ingredient(SAPPHIRE_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, EMERALD_BOLTS, new Ingredient(MITHRIL_BOLTS,10) , new Ingredient(EMERALD_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, RUBY_BOLTS, new Ingredient(ADAMANT_BOLTS,10) , new Ingredient(RUBY_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, DIAMOND_BOLTS, new Ingredient(ADAMANT_BOLTS,10) , new Ingredient(DIAMOND_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, DRAGONSTONE_BOLTS, new Ingredient(RUNITE_BOLTS,10) , new Ingredient(DRAGONSTONE_BOLT_TIPS,10)),
			new Product(ActionEnum.FLETCH_ATTACH, ONYX_BOLTS, new Ingredient(RUNITE_BOLTS,10) , new Ingredient(ONYX_BOLT_TIPS,10)),
			new Product(ActionEnum.FARM_ULTRA_COMPOST, ULTRACOMPOST, new Ingredient(VOLCANIC_ASH,2), new Ingredient(SUPERCOMPOST)),
			new Product(ActionEnum.CHURNING_CREAM, POT_OF_CREAM, new Ingredient(BUCKET_OF_MILK)),
			new Product(ActionEnum.CHURNING_BUTTER_WITH_MILK, PAT_OF_BUTTER, new Ingredient(BUCKET_OF_MILK)),
			new Product(ActionEnum.CHURNING_BUTTER_WITH_CREAM, PAT_OF_BUTTER, new Ingredient(POT_OF_CREAM)),
			new Product(ActionEnum.CHURNING_CHEESE_WITH_MILK, CHEESE, new Ingredient(BUCKET_OF_MILK)),
			new Product(ActionEnum.CHURNING_CHEESE_WITH_CREAM, CHEESE, new Ingredient(POT_OF_CREAM)),
			new Product(ActionEnum.CHURNING_CHEESE_WITH_BUTTER, CHEESE, new Ingredient(PAT_OF_BUTTER)),
			new Product(ActionEnum.CHURNING_CHEESE_WITH_GARLIC, CHEESE, new Ingredient(PAT_OF_NOT_GARLIC_BUTTER)),
			// @formatter:on
	};

	private final int[] widgetProductIds = new int[WIDGET_MAKE_SLOT_COUNT];

	@Inject
	private Client client;

	@Inject
	private InventoryManager inventoryManager;

	@Inject
	private ActionUtils actionUtils;

	@Inject
	private ActionManager actionManager;

	private int selectedIndex = -1;

	private String question;

	@Subscribe
	public void onVarbitChanged(VarbitChanged evt) {
		if (evt.getValue() == VAR_SELECTED_INDEX) {
			this.selectedIndex = this.client.getVarpValue(evt.getValue());
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired evt) {
		if (evt.getScriptId() == MAKE_X_BUTTON_KEY ||
			evt.getScriptId() == MAKE_X_BUTTON_CLICK) {
			ScriptEvent se = evt.getScriptEvent();
			Widget source = se == null ? null : se.getSource();
			if (source != null) {
				this.selectedIndex = (source.getId() - WIDGET_ID_CHATBOX_FIRST_MAKE_BUTTON);
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(@NonNull ScriptPostFired evt) {
		if (evt.getScriptId() == MAKE_X_SETUP) {
			log.debug("[proc_itembutton_draw] updating products");
			this.updateProducts();
		} else if (evt.getScriptId() == MAKE_X_BUTTON_TRIGGERED) {
			this.onQuestionAnswered();
		}
	}

	protected void unhandled(int itemId) {
		log.warn("[*] Unhandled chatbox action");
		log.warn(" |-> Question: {}", this.question);
		log.warn(" |-> Item ID: {}", itemId);
	}

	@Override
	public void setup() {
		/*
		 * Cooking
		 */
		this.registerAction(ActionEnum.COOKING_TOP_PIZZA, ItemID.INCOMPLETE_PIZZA, ItemID.UNCOOKED_PIZZA, ItemID.PINEAPPLE_PIZZA, ItemID.ANCHOVY_PIZZA,
			ItemID.MEAT_PIZZA
		);
		this.registerAction(ActionEnum.COOKING_MIX_GRAPES, ItemID.UNFERMENTED_WINE, ItemID.UNFERMENTED_WINE_1996, ItemID.ZAMORAKS_UNFERMENTED_WINE);
		this.registerAction(ActionEnum.COOKING_MIX_DOUGH, ItemID.BREAD_DOUGH, ItemID.PASTRY_DOUGH, ItemID.PITTA_DOUGH, ItemID.PIZZA_BASE);
		/*
		 * Fletching
		 */
		this.registerAction(ActionEnum.FLETCH_ATTACH, Fletching.UNENCHANTED_BOLTS_AND_ARROWS);
		this.registerAction(ActionEnum.FLETCH_ATTACH_3T, KEBBIT_BOLTS, MOONLIGHT_ANTELOPE_ANTLER, SUNLIGHT_ANTELOPE_ANTLER);
		this.registerAction(ActionEnum.FLETCH_ATTACH, HEADLESS_ARROW, FLIGHTED_OGRE_ARROW, AMETHYST_BROAD_BOLTS, AMETHYST_ARROW);
		this.registerAction(ActionEnum.FLETCH_JAVELIN, Fletching.JAVELINS);
		this.registerAction(ActionEnum.FLETCH_CUT_ARROW_SHAFT, ARROW_SHAFT, BRUMA_KINDLING, OGRE_ARROW_SHAFT);
		this.registerAction(ActionEnum.FLETCH_CUT_TIPS, Fletching.BOLT_TIPS);
		/*
		 *  Crafting
		 */
		this.registerAction(ActionEnum.CRAFT_AMETHYST_HEADS_AND_TIPS, Crafting.AMETHYST_HEADS_AND_TIPS);
		/*
		 * Herblore
		 */
		this.registerAction(ActionEnum.HERB_MIX_TAR, ItemID.GUAM_TAR, ItemID.MARRENTILL_TAR, ItemID.TARROMIN_TAR, ItemID.HARRALANDER_TAR);
		this.registerAction(ActionEnum.HERB_MIX_POTIONS_3T, ItemID.GUTHIX_REST3);
		for (Recipe recipe : Herblore.UNFINISHED_POTIONS) {
			this.registerAction(ActionEnum.HERB_MIX_UNFINISHED, recipe.getProductId());
		}
		for (Recipe recipe : Herblore.POTIONS) {
			this.registerAction(ActionEnum.HERB_MIX_POTIONS, recipe.getIsSelectingIngredientAsProduct() ? recipe.getRequirements()[0].getItemId() : recipe.getProductId()); //TODO Find way to display product when getIsSelectingIngredientAsProduct = true
		}
		for (int leaveItem : Woodcutting.LEAVES){
			for (int foodItem : Woodcutting.RATION_FOOD) {
				this.registerAction(ActionEnum.MAKING_FORESTERS_RATION, ItemID.FORESTERS_RATION, leaveItem, foodItem);
			}
		}
		/*
		 * Magic
		 */
//		this.registerAction(ActionEnum.MAGIC_ENCHANT_BOLTS, Fletching.);
	}

	@Override
	public void shutDown() {

	}

	private void onQuestionAnswered()
	{
		int currentProductId = this.widgetProductIds[this.selectedIndex];
		int amount = this.getActionCount(currentProductId);
		String question = this.question == null ? "?" : this.question;
		switch (question) {
			case "How many would you like to cook?":
			case "What would you like to cook?":
				this.actionManager.setAction(ActionEnum.COOKING_UNKNOWN, amount, currentProductId);
				break;
			case "How would you like to cut the pineapple?":
				if (currentProductId == PINEAPPLE_RING) {
					amount = Math.min(amount, this.actionUtils.getActionsUntilFull(4, 1));
				}
				this.actionManager.setAction(ActionEnum.COOKING_CUT_FRUIT, amount, currentProductId);
				break;
			case "How many would you like to charge?":
				Magic.ChargeOrbSpell spell = Magic.ChargeOrbSpell.byProduct(currentProductId);
				Objects.requireNonNull(spell, "No charge orb spell found for product: " + currentProductId);
				this.actionManager.setAction(
						ActionEnum.MAGIC_CHARGE_ORB,
						Math.min(amount, spell.getSpell().getAvailableCasts(this.client)),
						currentProductId
				);
				break;
			case "How many sets of bolts to enchant?":
				int enchantCrossbolBoltAmount = Magic.EnchantCrossbowBoltSpell.getAvailableCasts(client, currentProductId);
				this.actionManager.setAction(
						ActionEnum.MAGIC_ENCHANT_BOLTS,
						Math.min(amount, enchantCrossbolBoltAmount),
						currentProductId
				);
				break;
			case "What would you like to smelt?": // Smelting bars
				Product smithingProduct = Recipe.forProduct(MULTI_MATERIAL_PRODUCTS, currentProductId, this.inventoryManager);
				if (smithingProduct != null) {
					if (amount > 0) {
						this.actionManager.setAction(
								smithingProduct.getAction(),
								amount,
								smithingProduct.getIsSelectingIngredientAsProduct() ? smithingProduct.getProductId() : currentProductId
						);
					}
				}
				break;
			case "How many would you like to string?": // Fletching/Stringing
			case "What would you like to string?": // Fletching/Stringing
			case "What would you like to make?": // Various
			case "How many batches would you like?":
			case "How many bars would you like to smith?": // Cannonballs
			case "How many gems would you like to cut?": // Cutting gems
			case "How many do you wish to make?": // Various
			case "How many sets of 15 do you wish to complete?": // Arrows
			case "How many sets of 15 do you wish to feather?": // Headless arrows
			case "?":
			default:
				Product recipe = Recipe.forProduct(MULTI_MATERIAL_PRODUCTS, currentProductId, this.inventoryManager);
				if (recipe != null) {
					amount = Math.min(amount, recipe.getMakeProductCount(this.inventoryManager));
					if (amount > 0) {
						this.actionManager.setAction(
								recipe.getAction(),
								amount,
								recipe.getIsSelectingIngredientAsProduct() ? recipe.getProductId() : currentProductId
						);
					}
				} else {
					this.setActionByItemId(currentProductId, amount);
				}
				break;
		}
	}
	private void updateProducts()
	{
		for (int slotIndex = 0; slotIndex < WIDGET_MAKE_SLOT_COUNT; slotIndex++) {
			Widget slotWidget = this.client.getWidget(WIDGET_MAKE_PARENT, WIDGET_MAKE_SLOT_START + slotIndex);
			Widget container = slotWidget == null ? null : slotWidget.getChild(WIDGET_MAKE_SLOT_ITEM);
			int id = container == null ? -1 : container.getItemId();
			if (id != HOURGLASS && id != HOURGLASS_12841) {
				this.widgetProductIds[slotIndex] = id;
			}
		}
		Widget questionWidget = this.client.getWidget(WIDGET_MAKE_PARENT, WIDGET_MAKE_QUESTION);
		if (questionWidget != null) {
			this.question = questionWidget.getText();
		}
		log.debug("updated products: {} {}", this.question, Arrays.toString(this.widgetProductIds));
	}

	private int getActionCount(int productId)
	{
		int n = this.client.getVarcIntValue(VAR_MAKE_AMOUNT);
//		for (Smithing.Bar bar : Smithing.Bar.values()) {
//			if (productId == bar.getItemId()) {
//				return Math.min(n, bar.countAvailableOres(this.client));
//			}
//		}
//		Tuple2<Object, Seq<Object>> found = Cookable2.findMakeAmount(productId, inventoryManager);
//		log.debug("foundMakeAmount {}", found);
//		int fishQty = Int.unbox(found._1());
//		if(fishQty > 0) return Math.min(n, fishQty);
//		else return n;
//		for (Cookable entry : Cookable.values()) {
//			IDs raw = entry.getRaw();
//			IDs cooked = entry.getCooked();
//			if (cooked.contains(productId)) {
//				int rawFish = this.inventoryManager.getItemCount(raw::contains);
//				return Math.min(n, rawFish);
//			}
//		}
		return n;
	}
}
