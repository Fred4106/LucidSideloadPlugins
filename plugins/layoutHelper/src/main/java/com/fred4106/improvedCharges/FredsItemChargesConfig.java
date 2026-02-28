package com.fred4106.improvedCharges;

import net.runelite.client.config.*;
import com.fred4106.improvedCharges.store.ids.ChargeId;

import java.awt.Color;

import static com.fred4106.improvedCharges.Constants.*;

@ConfigGroup(GROUP)
public interface FredsItemChargesConfig extends Config {
	@ConfigSection(
		name = "General",
		description = "General settings",
		position = 1
	)
	String general = "general";

	@ConfigItem(
		keyName = "show_infoboxes",
		name = "Show infoboxes",
		description = "Show or hide all charges infoboxes simultaneously.",
		section = general,
		position = 1
	)
	default boolean showInfoboxes() {
		return true;
	}

	@ConfigItem(
		keyName = "show_overlays",
		name = "Show overlays",
		description = "Show or hide all charges overlays on top of items simultaneously.",
		section = general,
		position = 2
	)
	default boolean showOverlays() {
		return true;
	}

	@ConfigItem(
		keyName = "bank_overlays",
		name = "Show overlays in bank",
		description = "Show charges of the items in bank",
		section = general,
		position = 3
	)
	default boolean showBankOverlays() {
		return true;
	}

	@ConfigItem(
		keyName = "hide_outside_bank_overlays",
		name = "Show overlays only while in bank",
		description = "Shows item charges overlays only when in bank",
		section = general,
		position = 4
	)
	default boolean showOverlaysOnlyInBank() {
		return false;
	}

	@ConfigItem(
		keyName = "item_overlay_location",
		name = "Item overlay location",
		description = "Location of the charges for item overlays",
		section = general,
		position = 5
	)
	default ItemOverlayLocation itemOverlayLocation() {
		return ItemOverlayLocation.BOTTOM_LEFT;
	}

	@ConfigItem(
		keyName = "storage_tooltips",
		name = "Show storage tooltips",
		description = "Show tooltips for items with storage",
		section = general,
		position = 6
	)
	default boolean showStorageTooltips() {
		return true;
	}

	@ConfigItem(
		keyName = "hide_destroy_menu_entries",
		name = "Hide destroy menu entries",
		description = "Hide destroy menu entry from items that make no sense to destroy",
		section = general,
		position = 7
	)
	default boolean hideDestroyMenuEntries() {
		return false;
	}

	@ConfigItem(
		keyName = "show_unlimited_charges",
		name = "Show unlimited charges",
		description = "Show infinity symbol for items with unlimited charges",
		section = general,
		position = 8
	)
	default boolean showUnlimited() {
		return true;
	}

	@ConfigItem(
		keyName = "combat_degradable_style",
		name = "Time degradable style",
		description = "How to show charges for combat time degradable gear",
		section = general,
		position = 9
	)
	default CombatTimeDegradableStyle combatTimeDegradableStyle() {
		return CombatTimeDegradableStyle.CHARGES;
	}

	@ConfigItem(
		keyName = "show_daily_reset",
		name = "Show daily reset message",
		description = "Show message in chatbox when items daily charges have been reset",
		section = general,
		position = 10
	)
	default boolean showDailyReset() {
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "colors_default",
		name = "Default",
		description = "Color of default charges",
		position = 11,
		section = general
	)
	default Color getColorDefault() {
		return Color.white;
	}

	@Alpha
	@ConfigItem(
		keyName = "colors_unknown",
		name = "Unknown",
		description = "Color of unknown charges",
		position = 12,
		section = general
	)
	default Color getColorUnknown() {
		return Color.gray;
	}

	@Alpha
	@ConfigItem(
		keyName = "colors_empty",
		name = "Empty",
		description = "Color of empty charges",
		position = 13,
		section = general
	)
	default Color getColorEmpty() {
		return Color.red;
	}

	@Alpha
	@ConfigItem(
		keyName = "colors_activated",
		name = "Activated",
		description = "Color of activated charges",
		position = 14,
		section = general
	)
	default Color getColorActivated() {
		return Color.green;
	}

	@ConfigSection(
		name = "Potions",
		description = "Potions",
		position = 2,
		closedByDefault = true
	)
	String potions = "potion";

	@ConfigItem(
		keyName = potions + _INFOBOX,
		name = "Infoboxes",
		description = "Show potions infoboxes",
		section = potions
	)
	default boolean potionsInfoboxes() {
		return false;
	}

	@ConfigItem(
		keyName = potions + _OVERLAY,
		name = "Overlays",
		description = "Show potions overlays",
		section = potions
	)
	default boolean potionsOverlays() {
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "dose_4",
		name = "4 doses",
		description = "Color of 4 doses overlay",
		position = 1,
		section = potions
	)
	default Color get4DoseColor() {
		return Color.white;
	}

	@Alpha
	@ConfigItem(
		keyName = "dose_3",
		name = "3 doses",
		description = "Color of 3 doses overlay",
		position = 2,
		section = potions
	)
	default Color get3DoseColor() {
		return Color.yellow;
	}

	@Alpha
	@ConfigItem(
		keyName = "dose_2",
		name = "2 doses",
		description = "Color of 2 doses overlay",
		position = 3,
		section = potions
	)
	default Color get2DoseColor() {
		return new Color(230, 120, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "dose_1",
		name = "1 dose",
		description = "Color of 1 dose overlay",
		position = 4,
		section = potions
	)
	default Color get1DoseColor() {
		return Color.red;
	}


	@ConfigSection(
		name = "Escape Crystal",
		description = "Escape Crystal",
		position = 3,
		closedByDefault = true
	)
	String escape_crystal_section = "escape_crystal_section";

	@ConfigItem(
		keyName = ESCAPE_CRYSTAL_TIME_REMAINING_WARNING,
		name = "Time remaining alert",
		description = "Time before you are warned about Escape crystal activating",
		position = 4,
		section = escape_crystal_section
	)
	default int getEscapeCrystalTimeRemainingWarning() {
		return 2;
	}

	@ConfigItem(
		keyName = ESCAPE_CRYSTAL_TIME_REMAINING_UNIT,
		name = "Time remaining unit",
		description = "Unit to use for Escape crystal activation warning",
		position = 5,
		section = escape_crystal_section
	)
	default EscapeCrystalTimeRemainingUnit getEscapeCrystalTimeRemainingUnit() {
		return EscapeCrystalTimeRemainingUnit.SECONDS;
	}

	@ConfigSection(
		name = "Infoboxes",
		description = "Choose for which charged items infobox is visible",
		position = 4,
		closedByDefault = true
	)
	String infoboxes = "infoboxes";

	@ConfigItem(
		keyName = BINDING_NECKLACE + _INFOBOX,
		name = "Binding necklace",
		description = "",
		section = infoboxes
	)
	default boolean bindingNecklaceInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = PENDANT_OF_ATES + _INFOBOX,
		name = "Pendant of ates",
		description = "",
		section = infoboxes
	)
	default boolean pendantOfAtesInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = DIGSITE_PENDANT + _INFOBOX,
		name = "Digsite pendant",
		description = "",
		section = infoboxes
	)
	default boolean digsitePendantInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TUMEKENS_SHADOW + _INFOBOX,
		name = "Tumeken's shadow",
		description = "",
		section = infoboxes
	)
	default boolean tumekensShadowInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = MASTER_SCROLL_BOOK + _INFOBOX,
		name = "Master scroll book",
		description = "",
		section = infoboxes
	)
	default boolean masterScrollBookInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = REAGENT_POUCH + _INFOBOX,
		name = "Reagent pouch",
		description = "",
		section = infoboxes
	)
	default boolean reagentPouchInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ROYAL_SEED_POD + _INFOBOX,
		name = "Royal seed pod",
		description = "",
		section = infoboxes
	)
	default boolean royalSeedPodInfobox() {
		return false;
	}

	@ConfigItem(
		keyName = RING_OF_DUELING + _INFOBOX,
		name = "Ring of dueling",
		description = "",
		section = infoboxes
	)
	default boolean ringOfDuelingInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_FORGING + _INFOBOX,
		name = "Ring of forging",
		description = "",
		section = infoboxes
	)
	default boolean ringOfForgingInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_PURSUIT + _INFOBOX,
		name = "Ring of pursuit",
		description = "",
		section = infoboxes
	)
	default boolean ringOfPursuitInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = HUNTSMANS_KIT + _INFOBOX,
		name = "Huntsman's kit",
		description = "",
		section = infoboxes
	)
	default boolean huntsmansKitInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = IMP_IN_A_BOX + _INFOBOX,
		name = "Imp in a box",
		description = "",
		section = infoboxes
	)
	default boolean impInABoxInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BOW_OF_FAERDHINEN + _INFOBOX,
		name = "Bow of faerdhinen",
		description = "",
		section = infoboxes
	)
	default boolean bowOfFaerdhinenInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = VENATOR_BOW + _INFOBOX,
		name = "Venator bow",
		description = "",
		section = infoboxes
	)
	default boolean venatorBowInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = MEAT_POUCH + _INFOBOX,
		name = "Meat pouch",
		description = "",
		section = infoboxes
	)
	default boolean meatPouchInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = WESTERN_BANNER + _INFOBOX,
		name = "Western banner",
		description = "",
		section = infoboxes
	)
	default boolean westernBannerInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BARROWS_GEAR + _INFOBOX,
		name = "Barrows armor",
		description = "",
		section = infoboxes
	)
	default boolean barrowsInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = MOONS_GEAR + _INFOBOX,
		name = "Moons armor",
		description = "",
		section = infoboxes
	)
	default boolean moonsSetInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_BODY + _INFOBOX,
		name = "Crystal body",
		description = "",
		section = infoboxes
	)
	default boolean crystalBodyInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_HELM + _INFOBOX,
		name = "Crystal helm",
		description = "",
		section = infoboxes
	)
	default boolean crystalHelmInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_LEGS + _INFOBOX,
		name = "Crystal legs",
		description = "",
		section = infoboxes
	)
	default boolean crystalLegsInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FREMENNIK_SEA_BOOTS + _INFOBOX,
		name = "Fremennik sea boots",
		description = "",
		section = infoboxes
	)
	default boolean fremennikSeaBootsInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ARDOUGNE_CLOAK + _INFOBOX,
		name = "Ardougne cloak",
		description = "",
		section = infoboxes
	)
	default boolean ardougneCloakInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = COFFIN + _INFOBOX,
		name = "Coffin",
		description = "",
		section = infoboxes
	)
	default boolean coffinInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FORESTRY_BASKET + _INFOBOX,
		name = "Forestry basket",
		description = "",
		section = infoboxes
	)
	default boolean forestryBasketInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FORESTRY_KIT + _INFOBOX,
		name = "Forestry kit",
		description = "",
		section = infoboxes
	)
	default boolean forestryKitInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FUR_POUCH + _INFOBOX,
		name = "Fur pouch",
		description = "",
		section = infoboxes
	)
	default boolean furPouchInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = MAGIC_CAPE + _INFOBOX,
		name = "Magic cape",
		description = "",
		section = infoboxes
	)
	default boolean magicCapeInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CIRCLET_OF_WATER + _INFOBOX,
		name = "Circlet of water",
		description = "",
		section = infoboxes
	)
	default boolean circletOfWaterInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CHUGGING_BARREL + _INFOBOX,
		name = "Chugging barrel",
		description = "",
		section = infoboxes
	)
	default boolean chuggingBarrelInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = KANDARIN_HEADGEAR + _INFOBOX,
		name = "Kandarin Headgear",
		description = "",
		section = infoboxes
	)
	default boolean kandarinHeadgearInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BRACELET_OF_CLAY + _INFOBOX,
		name = "Bracelet of clay",
		description = "",
		section = infoboxes
	)
	default boolean braceletOfClayInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = EXPEDITIOUS_BRACELET + _INFOBOX,
		name = "Expeditious bracelet",
		description = "",
		section = infoboxes
	)
	default boolean expeditiousBraceletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FLAMTAER_BRACELET + _INFOBOX,
		name = "Flamtaer bracelet",
		description = "",
		section = infoboxes
	)
	default boolean flamtaerBraceletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = GAMES_NECKLACE + _INFOBOX,
		name = "Games necklace",
		description = "",
		section = infoboxes
	)
	default boolean gamesNecklaceInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BRACELET_OF_SLAUGHTER + _INFOBOX,
		name = "Bracelet of slaughter",
		description = "",
		section = infoboxes
	)
	default boolean braceletOfSlaughterInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CAMULET + _INFOBOX,
		name = "Camulet",
		description = "",
		section = infoboxes
	)
	default boolean camuletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CASTLE_WARS_BRACELET + _INFOBOX,
		name = "Castle wars bracelet",
		description = "",
		section = infoboxes
	)
	default boolean castleWarsBraceletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = DESERT_AMULET + _INFOBOX,
		name = "Desert amulet",
		description = "",
		section = infoboxes
	)
	default boolean desertAmuletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ESCAPE_CRYSTAL + _INFOBOX,
		name = "Escape crystal",
		description = "",
		section = infoboxes
	)
	default boolean escapeCrystalInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = DODGY_NECKLACE + _INFOBOX,
		name = "Dodgy necklace",
		description = "",
		section = infoboxes
	)
	default boolean dodgyNecklaceInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = NECKLACE_OF_PASSAGE + _INFOBOX,
		name = "Necklace of passage",
		description = "",
		section = infoboxes
	)
	default boolean necklaceOfPassageInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = PHOENIX_NECKLACE + _INFOBOX,
		name = "Phoenix necklace",
		description = "",
		section = infoboxes
	)
	default boolean phoenixNecklaceInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CELESTIAL_RING + _INFOBOX,
		name = "Celestial ring",
		description = "",
		section = infoboxes
	)
	default boolean celestialRingInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = COMBAT_BRACELET + _INFOBOX,
		name = "Combat bracelet",
		description = "",
		section = infoboxes
	)
	default boolean combatBraceletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_THE_ELEMENTS + _INFOBOX,
		name = "Ring of the elements",
		description = "",
		section = infoboxes
	)
	default boolean ringOfTheElementsInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_WEALTH + _INFOBOX,
		name = "Ring of wealth",
		description = "",
		section = infoboxes
	)
	default boolean ringOfWealthInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_ENDURANCE + _INFOBOX,
		name = "Ring of endurance",
		description = "",
		section = infoboxes
	)
	default boolean ringOfEnduranceInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = EXPLORERS_RING + _INFOBOX,
		name = "Explorer's ring",
		description = "",
		section = infoboxes
	)
	default boolean explorersRingInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_RECOIL + _INFOBOX,
		name = "Ring of recoil",
		description = "",
		section = infoboxes
	)
	default boolean ringOfRecoilInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_RETURNING + _INFOBOX,
		name = "Ring of returning",
		description = "",
		section = infoboxes
	)
	default boolean ringOfReturningInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_SHADOWS + _INFOBOX,
		name = "Ring of shadows",
		description = "",
		section = infoboxes
	)
	default boolean ringOfShadowsInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SLAYER_RING + _INFOBOX,
		name = "Slayer ring",
		description = "",
		section = infoboxes
	)
	default boolean slayerRingInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_SUFFERING + _INFOBOX,
		name = "Ring of suffering",
		description = "",
		section = infoboxes
	)
	default boolean ringOfSufferingInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = XERICS_TALISMAN + _INFOBOX,
		name = "Xeric's talisman",
		description = "",
		section = infoboxes
	)
	default boolean xericsTalismanInfobox() {
		return true;
	}

	@ConfigItem(
		keyName =  SAILORS_AMULET + _INFOBOX,
		name = "Sailors' Amulet",
		description = "",
		section = infoboxes
	) default boolean sailorsAmuletInfobox() { return true; }

	@ConfigItem(
		keyName = CHRONICLE + _INFOBOX,
		name = "Chronicle",
		description = "",
		section = infoboxes
	)
	default boolean chronicleInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_SHIELD + _INFOBOX,
		name = "Crystal shield",
		description = "",
		section = infoboxes
	)
	default boolean crystalShieldInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = DRAGONFIRE_SHIELD + _INFOBOX,
		name = "Dragonfire shield",
		description = "",
		section = infoboxes
	)
	default boolean dragonfireShieldInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FALADOR_SHIELD + _INFOBOX,
		name = "Falador shield",
		description = "",
		section = infoboxes
	)
	default boolean faladorShieldInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = KHAREDSTS_MEMOIRS + _INFOBOX,
		name = "Kharedst's memoirs",
		description = "",
		section = infoboxes
	)
	default boolean kharedstsMemoirsInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = KHAREDSTS_MEMOIRS + _INFOBOX,
		name = "Book of the dead",
		description = "",
		section = infoboxes
	)
	default boolean bookOfTheDeadInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TOME_OF_EARTH + _INFOBOX,
		name = "Tome of earth",
		description = "",
		section = infoboxes
	)
	default boolean tomeOfEarthInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TOME_OF_FIRE + _INFOBOX,
		name = "Tome of fire",
		description = "",
		section = infoboxes
	)
	default boolean tomeOfFireInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TOME_OF_WATER + _INFOBOX,
		name = "Tome of water",
		description = "",
		section = infoboxes
	)
	default boolean tomeOfWaterInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ASH_SANCTIFIER + _INFOBOX,
		name = "Ash sanctifier",
		description = "",
		section = infoboxes
	)
	default boolean ashSanctifierInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BLOOD_ESSENCE + _INFOBOX,
		name = "Blood essence",
		description = "",
		section = infoboxes
	)
	default boolean bloodEssenceInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BONECRUSHER + _INFOBOX,
		name = "Bonecrusher",
		description = "",
		section = infoboxes
	)
	default boolean bonecrusherInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BOTTOMLESS_COMPOST_BUCKET + _INFOBOX,
		name = "Bottomless compost bucket",
		description = "",
		section = infoboxes
	)
	default boolean bottomlessCompostBucketInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BOW_STRING_SPOOL + _INFOBOX,
		name = "Bow string spool",
		description = "",
		section = infoboxes
	)
	default boolean bowStringSpoolInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = COAL_BAG + _INFOBOX,
		name = "Coal bag",
		description = "",
		section = infoboxes
	)
	default boolean coalBagInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = COLOSSAL_POUCH + _INFOBOX,
		name = "Colossal pouch",
		description = "",
		section = infoboxes
	)
	default boolean colossalPouchInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_SAW + _INFOBOX,
		name = "Crystal saw",
		description = "",
		section = infoboxes
	)
	default boolean crystalSawInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ECTOPHIAL + _INFOBOX,
		name = "Ectophial",
		description = "",
		section = infoboxes
	)
	default boolean ectophialInfobox() {
		return false;
	}

	@ConfigItem(
		keyName = FISH_BARREL + _INFOBOX,
		name = "Fish barrel",
		description = "",
		section = infoboxes
	)
	default boolean fishBarrelInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FLAMTAER_BAG + _INFOBOX,
		name = "Flamtaer bag",
		description = "",
		section = infoboxes
	)
	default boolean flamtaerBagInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = FUNGICIDE_SPRAY + _INFOBOX,
		name = "Fungicide spray",
		description = "",
		section = infoboxes
	)
	default boolean fungicideSprayInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = GEM_BAG + _INFOBOX,
		name = "Gem bag",
		description = "",
		section = infoboxes
	)
	default boolean gemBagInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = GIANTSOUL_AMULET + _INFOBOX,
		name = "Giantsoul amulet",
		description = "",
		section = infoboxes
	)
	default boolean giantsoulAmuletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = GRICOLLERS_CAN + _INFOBOX,
		name = "Gricollers can",
		description = "",
		section = infoboxes
	)
	default boolean gricollersCanInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = HERB_SACK + _INFOBOX,
		name = "Herb sack",
		description = "",
		section = infoboxes
	)
	default boolean herbSackInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = JAR_GENERATOR + _INFOBOX,
		name = "Jar generator",
		description = "",
		section = infoboxes
	)
	default boolean jarGeneratorInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = LOG_BASKET + _INFOBOX,
		name = "Log basket",
		description = "",
		section = infoboxes
	)
	default boolean logBasketInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = OGRE_BELLOWS + _INFOBOX,
		name = "Ogre bellows",
		description = "",
		section = infoboxes
	)
	default boolean ogreBellowsInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = PLANK_SACK + _INFOBOX,
		name = "Plank sack",
		description = "",
		section = infoboxes
	)
	default boolean plankSackInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = QUETZAL_WHISTLE + _INFOBOX,
		name = "Quetzal whistle",
		description = "",
		section = infoboxes
	)
	default boolean quetzalWhistleInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SEED_BOX + _INFOBOX,
		name = "Seed box",
		description = "",
		section = infoboxes
	)
	default boolean seedBoxInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SKILLS_NECKLACE + _INFOBOX,
		name = "Skills necklace",
		description = "",
		section = infoboxes
	)
	default boolean skillsNecklaceInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SOUL_BEARER + _INFOBOX,
		name = "Soul bearer",
		description = "",
		section = infoboxes
	)
	default boolean soulBearerInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = STRANGE_OLD_LOCKPICK + _INFOBOX,
		name = "Strange old lockpick",
		description = "",
		section = infoboxes
	)
	default boolean strangeOldLockpickInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TACKLE_BOX + _INFOBOX,
		name = "Tackle box",
		description = "",
		section = infoboxes
	)
	default boolean tackleBoxInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TELEPORT_CRYSTAL + _INFOBOX,
		name = "Teleport crystal",
		description = "",
		section = infoboxes
	)
	default boolean teleportCrystalInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ETERNAL_TELEPORT_CRYSTAL + _INFOBOX,
		name = "Eternal teleport crystal",
		description = "",
		section = infoboxes
	)
	default boolean eternalTeleportCrystalInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = WATERSKIN + _INFOBOX,
		name = "Waterskin",
		description = "",
		section = infoboxes
	)
	default boolean waterskinInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ARCLIGHT + _INFOBOX,
		name = "Arclight",
		description = "",
		section = infoboxes
	)
	default boolean arclightInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BLAZING_BLOWPIPE + _INFOBOX,
		name = "Blazing blowpipe",
		description = "",
		section = infoboxes
	)
	default boolean blazingBlowpipeInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BRYOPHYTAS_STAFF + _INFOBOX,
		name = "Bryophytas staff",
		description = "",
		section = infoboxes
	)
	default boolean bryophytasStaffInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRAWS_BOW + _INFOBOX,
		name = "Craw's bow",
		description = "",
		section = infoboxes
	)
	default boolean crawsBowInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = BURNING_AMULET + _INFOBOX,
		name = "Burning amulet",
		description = "",
		section = infoboxes
	)
	default boolean burningAmuletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_BOW + _INFOBOX,
		name = "Crystal bow",
		description = "",
		section = infoboxes
	)
	default boolean crystalBowInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_HALBERD + _INFOBOX,
		name = "Crystal halberd",
		description = "",
		section = infoboxes
	)
	default boolean crystalHalberdInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = EFARITAYS_AID + _INFOBOX,
		name = "Efaritays aid",
		description = "",
		section = infoboxes
	)
	default boolean efaritaysAidInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ENCHANTED_LYRE + _INFOBOX,
		name = "Enchanted Lyre",
		description = "",
		section = infoboxes
	)
	default boolean enchantedLyreInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = IBANS_STAFF + _INFOBOX,
		name = "Iban's staff",
		description = "",
		section = infoboxes
	)
	default boolean ibansStaffInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = INFERNAL_AXE + _INFOBOX,
		name = "Infernal axe",
		description = "",
		section = infoboxes
	)
	default boolean infernalAxeInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = PHARAOHS_SCEPTRE + _INFOBOX,
		name = "Pharaoh's sceptre",
		description = "",
		section = infoboxes
	)
	default boolean pharaohsSceptreInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SANGUINESTI_STAFF + _INFOBOX,
		name = "Sanguinesti staff",
		description = "",
		section = infoboxes
	)
	default boolean sanguinestiStaffInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SCYTHE_OF_VITUR + _INFOBOX,
		name = "Scythe of Vitur",
		description = "",
		section = infoboxes
	)
	default boolean scytheOfViturInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SKULL_SCEPTRE + _INFOBOX,
		name = "Skull sceptre",
		description = "",
		section = infoboxes
	)
	default boolean skullSceptreInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = SLAYER_STAFF_E + _INFOBOX,
		name = "Slayer staff (e)",
		description = "",
		section = infoboxes
	)
	default boolean slayerStaffEInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TOXIC_BLOWPIPE + _INFOBOX,
		name = "Toxic blowpipe",
		description = "",
		section = infoboxes
	)
	default boolean toxicBlowpipeInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TOXIC_STAFF_OF_THE_DEAD + _INFOBOX,
		name = "Toxic staff of the dead",
		description = "",
		section = infoboxes
	)
	default boolean toxicStaffOfTheDeadInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SEAS + _INFOBOX,
		name = "Trident of the seas",
		description = "",
		section = infoboxes
	)
	default boolean tridentOfTheSeasInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SEAS_E + _INFOBOX,
		name = "Trident of the seas (e)",
		description = "",
		section = infoboxes
	)
	default boolean tridentOfTheSeasEInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SWAMP + _INFOBOX,
		name = "Trident of the swamp",
		description = "",
		section = infoboxes
	)
	default boolean tridentOfTheSwampInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SWAMP_E + _INFOBOX,
		name = "Trident of the swamp (e)",
		description = "",
		section = infoboxes
	)
	default boolean tridentOfTheSwampEInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = WARPED_SCEPTRE + _INFOBOX,
		name = "Warped sceptre",
		description = "",
		section = infoboxes
	)
	default boolean warpedSceptreInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = WEBWEAVER_BOW + _INFOBOX,
		name = "Webweaver bow",
		description = "",
		section = infoboxes
	)
	default boolean webweaverBowInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = ALCHEMISTS_AMULET + _INFOBOX,
		name = "Alchemist's amulet",
		description = "",
		section = infoboxes
	)
	default boolean alchemistsAmuletInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = AMULET_OF_BLOOD_FURY + _INFOBOX,
		name = "Amulet of blood fury",
		description = "",
		section = infoboxes
	)
	default boolean amuletOfBloodFuryInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = AMULET_OF_CHEMISTRY + _INFOBOX,
		name = "Amulet of chemistry",
		description = "",
		section = infoboxes
	)
	default boolean amuletOfChemistryInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = AMULET_OF_GLORY + _INFOBOX,
		name = "Amulet of glory",
		description = "",
		section = infoboxes
	)
	default boolean amuletOfGloryInfobox() {
		return true;
	}

	@ConfigItem(
		keyName = EYE_OF_AYAK + _INFOBOX,
		name = "Eye of Ayak",
		description = "",
		section = infoboxes
	)
	default boolean eyeOfAyakInfobox() {
		return true;
	}

	@ConfigSection(
		name = "Overlays",
		description = "Choose for which charged items number is shown next to it",
		position = 5,
		closedByDefault = true
	)
	String overlays = "overlays";

	@ConfigItem(
		keyName = BINDING_NECKLACE + _OVERLAY,
		name = "Binding necklace",
		description = "",
		section = overlays
	)
	default boolean bindingNecklaceOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = PENDANT_OF_ATES + _OVERLAY,
		name = "Pendant of ates",
		description = "",
		section = overlays
	)
	default boolean pendantOfAtesOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = DIGSITE_PENDANT + _OVERLAY,
		name = "Digsite pendant",
		description = "",
		section = overlays
	)
	default boolean digsitePendantOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TUMEKENS_SHADOW + _OVERLAY,
		name = "Tumeken's shadow",
		description = "",
		section = overlays
	)
	default boolean tumekensShadowOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = MASTER_SCROLL_BOOK + _OVERLAY,
		name = "Master scroll book",
		description = "",
		section = overlays
	)
	default boolean masterScrollBookOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = REAGENT_POUCH + _OVERLAY,
		name = "Reagent pouch",
		description = "",
		section = overlays
	)
	default boolean reagentPouchOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ROYAL_SEED_POD + _OVERLAY,
		name = "Royal seed pod",
		description = "",
		section = overlays
	)
	default boolean royalSeedPodOverlay() {
		return false;
	}

	@ConfigItem(
		keyName = RING_OF_DUELING + _OVERLAY,
		name = "Ring of dueling",
		description = "",
		section = overlays
	)
	default boolean ringOfDuelingOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_FORGING + _OVERLAY,
		name = "Ring of forging",
		description = "",
		section = overlays
	)
	default boolean ringOfForgingOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_PURSUIT + _OVERLAY,
		name = "Ring of pursuit",
		description = "",
		section = overlays
	)
	default boolean ringOfPursuitOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = HUNTSMANS_KIT + _OVERLAY,
		name = "Huntsman's kit",
		description = "",
		section = overlays
	)
	default boolean huntsmansKitOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = IMP_IN_A_BOX + _OVERLAY,
		name = "Imp in a box",
		description = "",
		section = overlays
	)
	default boolean impInABoxOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ARCLIGHT + _OVERLAY,
		name = "Arclight",
		description = "",
		section = overlays
	)
	default boolean arclightOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BLAZING_BLOWPIPE + _OVERLAY,
		name = "Blazing blowpipe",
		description = "",
		section = overlays
	)
	default boolean blazingBlowpipeOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ARDOUGNE_CLOAK + _OVERLAY,
		name = "Ardougne cloak",
		description = "",
		section = overlays
	)
	default boolean ardougneCloakOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ASH_SANCTIFIER + _OVERLAY,
		name = "Ash sanctifier",
		description = "",
		section = overlays
	)
	default boolean ashSanctifierOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BLOOD_ESSENCE + _OVERLAY,
		name = "Blood essence",
		description = "",
		section = overlays
	)
	default boolean bloodEssenceOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BARROWS_GEAR + _OVERLAY,
		name = "Barrows armor",
		description = "",
		section = overlays
	)
	default boolean barrowsOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = MOONS_GEAR + _OVERLAY,
		name = "Moons armor",
		description = "",
		section = overlays
	)
	default boolean moonsGearOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BONECRUSHER + _OVERLAY,
		name = "Bonecrusher",
		description = "",
		section = overlays
	)
	default boolean bonecrusherOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BOTTOMLESS_COMPOST_BUCKET + _OVERLAY,
		name = "Bottomless compost bucket",
		description = "",
		section = overlays
	)
	default boolean bottomlessCompostBucketOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BOW_STRING_SPOOL + _OVERLAY,
		name = "Bow string spool",
		description = "",
		section = overlays
	)
	default boolean bowStringSpoolOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BOW_OF_FAERDHINEN + _OVERLAY,
		name = "Bow of faerdhinen",
		description = "",
		section = overlays
	)
	default boolean bowOfFaerdhinenOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BRACELET_OF_CLAY + _OVERLAY,
		name = "Bracelet of clay",
		description = "",
		section = overlays
	)
	default boolean braceletOfClayOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = EXPEDITIOUS_BRACELET + _OVERLAY,
		name = "Expeditious bracelet",
		description = "",
		section = overlays
	)
	default boolean expeditiousBraceletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FLAMTAER_BRACELET + _OVERLAY,
		name = "Flamtaer bracelet",
		description = "",
		section = overlays
	)
	default boolean flamtaerBraceletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = GAMES_NECKLACE + _OVERLAY,
		name = "Games necklace",
		description = "",
		section = overlays
	)
	default boolean gamesNecklaceOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BRACELET_OF_SLAUGHTER + _OVERLAY,
		name = "Bracelet of slaughter",
		description = "",
		section = overlays
	)
	default boolean braceletOfSlaughterOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CAMULET + _OVERLAY,
		name = "Camulet",
		description = "",
		section = overlays
	)
	default boolean camuletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CASTLE_WARS_BRACELET + _OVERLAY,
		name = "Castle wars bracelet",
		description = "",
		section = overlays
	)
	default boolean castleWarsBraceletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CELESTIAL_RING + _OVERLAY,
		name = "Celestial ring",
		description = "",
		section = overlays
	)
	default boolean celestialRingOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = COMBAT_BRACELET + _OVERLAY,
		name = "Combat bracelet",
		description = "",
		section = overlays
	)
	default boolean combatBraceletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CHRONICLE + _OVERLAY,
		name = "Chronicle",
		description = "",
		section = overlays
	)
	default boolean chronicleOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CIRCLET_OF_WATER + _OVERLAY,
		name = "Circlet of water",
		description = "",
		section = overlays
	)
	default boolean circletOfWaterOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CHUGGING_BARREL + _OVERLAY,
		name = "Chugging barrel",
		description = "",
		section = overlays
	)
	default boolean chuggingBarrelOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = COAL_BAG + _OVERLAY,
		name = "Coal bag",
		description = "",
		section = overlays
	)
	default boolean coalBagOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = COLOSSAL_POUCH + _OVERLAY,
		name = "Colossal pouch",
		description = "",
		section = overlays
	)
	default boolean colossalPouchOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = COFFIN + _OVERLAY,
		name = "Coffin",
		description = "",
		section = overlays
	)
	default boolean coffinOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_BODY + _OVERLAY,
		name = "Crystal body",
		description = "",
		section = overlays
	)
	default boolean crystalBodyOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_HELM + _OVERLAY,
		name = "Crystal helm",
		description = "",
		section = overlays
	)
	default boolean crystalHelmOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_LEGS + _OVERLAY,
		name = "Crystal legs",
		description = "",
		section = overlays
	)
	default boolean crystalLegsOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_SAW + _OVERLAY,
		name = "Crystal saw",
		description = "",
		section = overlays
	)
	default boolean crystalSawOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ECTOPHIAL + _OVERLAY,
		name = "Ectophial",
		description = "",
		section = overlays
	)
	default boolean ectophialOverlay() {
		return false;
	}

	@ConfigItem(
		keyName = CRYSTAL_SHIELD + _OVERLAY,
		name = "Crystal shield",
		description = "",
		section = overlays
	)
	default boolean crystalShieldOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = DESERT_AMULET + _OVERLAY,
		name = "Desert amulet",
		description = "",
		section = overlays
	)
	default boolean desertAmuletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = DRAGONFIRE_SHIELD + _OVERLAY,
		name = "Dragonfire shield",
		description = "",
		section = overlays
	)
	default boolean dragonfireShieldOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FALADOR_SHIELD + _OVERLAY,
		name = "Falador shield",
		description = "",
		section = overlays
	)
	default boolean faladorShieldOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FUR_POUCH + _OVERLAY,
		name = "Fur pouch",
		description = "",
		section = overlays
	)
	default boolean furPouchOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ESCAPE_CRYSTAL + _OVERLAY,
		name = "Escape crystal",
		description = "",
		section = overlays
	)
	default boolean escapeCrystalOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = EXPLORERS_RING + _OVERLAY,
		name = "Explorer's ring",
		description = "",
		section = overlays
	)
	default boolean explorersRingOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = DODGY_NECKLACE + _OVERLAY,
		name = "Dodgy necklace",
		description = "",
		section = overlays
	)
	default boolean dodgyNecklaceOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FISH_BARREL + _OVERLAY,
		name = "Fish barrel",
		description = "",
		section = overlays
	)
	default boolean fishBarrelOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FLAMTAER_BAG + _OVERLAY,
		name = "Flamtaer bag",
		description = "",
		section = overlays
	)
	default boolean flamtaerBagOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FORESTRY_BASKET + _OVERLAY,
		name = "Forestry basket",
		description = "",
		section = overlays
	)
	default boolean forestryBasketOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FORESTRY_KIT + _OVERLAY,
		name = "Forestry kit",
		description = "",
		section = overlays
	)
	default boolean forestryKitOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FREMENNIK_SEA_BOOTS + _OVERLAY,
		name = "Fremennik sea boots",
		description = "",
		section = overlays
	)
	default boolean fremennikSeaBootsOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = FUNGICIDE_SPRAY + _OVERLAY,
		name = "Fungicide spray",
		description = "",
		section = overlays
	)
	default boolean fungicideSprayOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = GEM_BAG + _OVERLAY,
		name = "Gem bag",
		description = "",
		section = overlays
	)
	default boolean gemBagOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = GIANTSOUL_AMULET + _OVERLAY,
		name = "Giantsoul amulet",
		description = "",
		section = overlays
	)
	default boolean giantsoulAmuletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = GRICOLLERS_CAN + _OVERLAY,
		name = "Gricollers can",
		description = "",
		section = overlays
	)
	default boolean gricollersCanOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = HERB_SACK + _OVERLAY,
		name = "Herb sack",
		description = "",
		section = overlays
	)
	default boolean herbSackOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = JAR_GENERATOR + _OVERLAY,
		name = "Jar generator",
		description = "",
		section = overlays
	)
	default boolean jarGeneratorOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = KANDARIN_HEADGEAR + _OVERLAY,
		name = "Kandarin Headgear",
		description = "",
		section = overlays
	)
	default boolean kandarinHeadgearOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = KHAREDSTS_MEMOIRS + _OVERLAY,
		name = "Kharedst's memoirs",
		description = "",
		section = overlays
	)
	default boolean kharedstsMemoirsOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = KHAREDSTS_MEMOIRS + _OVERLAY,
		name = "Book of the dead",
		description = "",
		section = overlays
	)
	default boolean bookOfTheDeadOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = LOG_BASKET + _OVERLAY,
		name = "Log basket",
		description = "",
		section = overlays
	)
	default boolean logBasketOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = MAGIC_CAPE + _OVERLAY,
		name = "Magic cape",
		description = "",
		section = overlays
	)
	default boolean magicCapeOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = MEAT_POUCH + _OVERLAY,
		name = "Meat pouch",
		description = "",
		section = overlays
	)
	default boolean meatPouchOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = NECKLACE_OF_PASSAGE + _OVERLAY,
		name = "Necklace of passage",
		description = "",
		section = overlays
	)
	default boolean necklaceOfPassageOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = OGRE_BELLOWS + _OVERLAY,
		name = "Ogre bellows",
		description = "",
		section = overlays
	)
	default boolean ogreBellowsOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = PHOENIX_NECKLACE + _OVERLAY,
		name = "Phoenix necklace",
		description = "",
		section = overlays
	)
	default boolean phoenixNecklaceOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = PLANK_SACK + _OVERLAY,
		name = "Plank sack",
		description = "",
		section = overlays
	)
	default boolean plankSackOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_RECOIL + _OVERLAY,
		name = "Ring of recoil",
		description = "",
		section = overlays
	)
	default boolean ringOfRecoilOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_RETURNING + _OVERLAY,
		name = "Ring of returning",
		description = "",
		section = overlays
	)
	default boolean ringOfReturningOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_SHADOWS + _OVERLAY,
		name = "Ring of shadows",
		description = "",
		section = overlays
	)
	default boolean ringOfShadowsOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_SUFFERING + _OVERLAY,
		name = "Ring of suffering",
		description = "",
		section = overlays
	)
	default boolean ringOfSufferingOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_THE_ELEMENTS + _OVERLAY,
		name = "Ring of the elements",
		description = "",
		section = overlays
	)
	default boolean ringOfTheElementsOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_WEALTH + _OVERLAY,
		name = "Ring of wealth",
		description = "",
		section = overlays
	)
	default boolean ringOfWealthOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = RING_OF_ENDURANCE + _OVERLAY,
		name = "Ring of endurance",
		description = "",
		section = overlays
	)
	default boolean ringOfEnduranceOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SEED_BOX + _OVERLAY,
		name = "Seed box",
		description = "",
		section = overlays
	)
	default boolean seedBoxOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SKILLS_NECKLACE + _OVERLAY,
		name = "Skills necklace",
		description = "",
		section = overlays
	)
	default boolean skillsNecklaceOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SLAYER_RING + _OVERLAY,
		name = "Slayer ring",
		description = "",
		section = overlays
	)
	default boolean slayerRingOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SOUL_BEARER + _OVERLAY,
		name = "Soul bearer",
		description = "",
		section = overlays
	)
	default boolean soulBearerOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = STRANGE_OLD_LOCKPICK + _OVERLAY,
		name = "Strange old lockpick",
		description = "",
		section = overlays
	)
	default boolean strangeOldLockpickOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TACKLE_BOX + _OVERLAY,
		name = "Tackle box",
		description = "",
		section = overlays
	)
	default boolean tackleBoxOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TELEPORT_CRYSTAL + _OVERLAY,
		name = "Teleport crystal",
		description = "",
		section = overlays
	)
	default boolean teleportCrystalOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ETERNAL_TELEPORT_CRYSTAL + _OVERLAY,
		name = "Eternal teleport crystal",
		description = "",
		section = overlays
	)
	default boolean eternalTeleportCrystalOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TOME_OF_EARTH + _OVERLAY,
		name = "Tome of earth",
		description = "",
		section = overlays
	)
	default boolean tomeOfEarthOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TOME_OF_FIRE + _OVERLAY,
		name = "Tome of fire",
		description = "",
		section = overlays
	)
	default boolean tomeOfFireOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TOME_OF_WATER + _OVERLAY,
		name = "Tome of water",
		description = "",
		section = overlays
	)
	default boolean tomeOfWaterOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = VENATOR_BOW + _OVERLAY,
		name = "Venator bow",
		description = "",
		section = overlays
	)
	default boolean venatorBowOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = WATERSKIN + _OVERLAY,
		name = "Waterskin",
		description = "",
		section = overlays
	)
	default boolean waterskinOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = WESTERN_BANNER + _OVERLAY,
		name = "Western banner",
		description = "",
		section = overlays
	)
	default boolean westernBannerOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BRYOPHYTAS_STAFF + _OVERLAY,
		name = "Bryophytas staff",
		description = "",
		section = overlays
	)
	default boolean bryophytasStaffOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CRAWS_BOW + _OVERLAY,
		name = "Craw's bow",
		description = "",
		section = overlays
	)
	default boolean crawsBowOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = BURNING_AMULET + _OVERLAY,
		name = "Burning amulet",
		description = "",
		section = overlays
	)
	default boolean burningAmuletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_BOW + _OVERLAY,
		name = "Crystal bow",
		description = "",
		section = overlays
	)
	default boolean crystalBowOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = CRYSTAL_HALBERD + _OVERLAY,
		name = "Crystal halberd",
		description = "",
		section = overlays
	)
	default boolean crystalHalberdOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = EFARITAYS_AID + _OVERLAY,
		name = "Efaritays aid",
		description = "",
		section = overlays
	)
	default boolean efaritaysAidOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = ENCHANTED_LYRE + _OVERLAY,
		name = "Enchanted Lyre",
		description = "",
		section = overlays
	)
	default boolean enchantedLyreOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = IBANS_STAFF + _OVERLAY,
		name = "Iban's staff",
		description = "",
		section = overlays
	)
	default boolean ibansStaffOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = INFERNAL_AXE + _OVERLAY,
		name = "Infernal axe",
		description = "",
		section = overlays
	)
	default boolean infernalAxeOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = PHARAOHS_SCEPTRE + _OVERLAY,
		name = "Pharaoh's sceptre",
		description = "",
		section = overlays
	)
	default boolean pharaohsSceptreOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = QUETZAL_WHISTLE + _OVERLAY,
		name = "Quetzal whistle",
		description = "",
		section = overlays
	)
	default boolean quetzalWhistleOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SANGUINESTI_STAFF + _OVERLAY,
		name = "Sanguinesti staff",
		description = "",
		section = overlays
	)
	default boolean sanguinestiStaffOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SCYTHE_OF_VITUR + _OVERLAY,
		name = "Scythe of Vitur",
		description = "",
		section = overlays
	)
	default boolean scytheOfViturOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SKULL_SCEPTRE + _OVERLAY,
		name = "Skull sceptre",
		description = "",
		section = overlays
	)
	default boolean skullSceptreOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = SLAYER_STAFF_E + _OVERLAY,
		name = "Slayer staff (e)",
		description = "",
		section = overlays
	)
	default boolean slayerStaffEOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TOXIC_BLOWPIPE + _OVERLAY,
		name = "Toxic blowpipe",
		description = "",
		section = overlays
	)
	default boolean toxicBlowpipeOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TOXIC_STAFF_OF_THE_DEAD + _OVERLAY,
		name = "Toxic staff of the dead",
		description = "",
		section = overlays
	)
	default boolean toxicStaffOfTheDeadOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SEAS + _OVERLAY,
		name = "Trident of the seas",
		description = "",
		section = overlays
	)
	default boolean tridentOfTheSeasOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SEAS_E + _OVERLAY,
		name = "Trident of the seas (e)",
		description = "",
		section = overlays
	)
	default boolean tridentOfTheSeasEOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SWAMP + _OVERLAY,
		name = "Trident of the swamp",
		description = "",
		section = overlays
	)
	default boolean tridentOfTheSwampOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SWAMP_E + _OVERLAY,
		name = "Trident of the swamp (e)",
		description = "",
		section = overlays
	)
	default boolean tridentOfTheSwampEOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = WARPED_SCEPTRE + _OVERLAY,
		name = "Warped sceptre",
		description = "",
		section = overlays
	)
	default boolean warpedSceptreOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = WEBWEAVER_BOW + _OVERLAY,
		name = "Webweaver bow",
		description = "",
		section = overlays
	)
	default boolean webweaverBowOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = XERICS_TALISMAN + _OVERLAY,
		name = "Xeric's talisman",
		description = "",
		section = overlays
	)
	default boolean xericsTalismanOverlay() {
		return true;
	}

	@ConfigItem(
		keyName =  SAILORS_AMULET + _OVERLAY,
		name = "Sailors' Amulet",
		description = "",
		section = overlays
	) default boolean sailorsAmuletOverlay() { return true; }

	@ConfigItem(
		keyName = ALCHEMISTS_AMULET + _OVERLAY,
		name = "Alchemist's amulet",
		description = "",
		section = overlays
	)
	default boolean alchemistsAmuletOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = AMULET_OF_BLOOD_FURY + _OVERLAY,
		name = "Amulet of blood fury",
		description = "",
		section = overlays
	)
	default boolean amuletOfBloodFuryOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = AMULET_OF_CHEMISTRY + _OVERLAY,
		name = "Amulet of chemistry",
		description = "",
		section = overlays
	)
	default boolean amuletOfChemistryOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = AMULET_OF_GLORY + _OVERLAY,
		name = "Amulet of glory",
		description = "",
		section = overlays
	)
	default boolean amuletOfGloryOverlay() {
		return true;
	}

	@ConfigItem(
		keyName = EYE_OF_AYAK + _OVERLAY,
		name = "Eye of Ayak",
		description = "",
		section = overlays
	)
	default boolean eyeOfAyakOverlay() {
		return true;
	}

	@ConfigSection(
		name = "Debug",
		description = "Values of charges for all items under the hood",
		position = 99,
		closedByDefault = true
	)
	String debug = "debug";

	@ConfigItem(
		keyName = VERSION,
		name = VERSION,
		description = "Version of the plugin for update message",
		section = debug,
		position = -4
	)
	default String getVersion() {
		return "";
	}

	@ConfigItem(
		keyName = DATE,
		name = "Date",
		description = "Date to check for charges reset when logging in",
		section = debug,
		position = -3
	)
	default String getResetDate() {
		return "";
	}

	@ConfigItem(
		keyName = DEBUG_IDS,
		name = "Debug IDs",
		description = "Shows animation and graphics ids within in-game messages to add support for new items",
		section = debug,
		position = -2
	)
	default boolean showDebugIds() {
		return false;
	}

	@ConfigItem(
		keyName = STORAGE_BANK,
		name = STORAGE_BANK,
		description = "All player bank items to check for daily resets",
		section = debug,
		position = 1
	)
	default String getStorageBank() {
		return "";
	}

	@ConfigItem(
		keyName = BARROWS_GEAR + "_ahrims_hood",
		name = BARROWS_GEAR + "_ahrims_hood",
		description = BARROWS_GEAR + "_ahrims_hood",
		section = debug
	)
	default int ahrimsHoodCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = RING_OF_PURSUIT,
		name = RING_OF_PURSUIT,
		description = RING_OF_PURSUIT,
		section = debug
	)
	default int ringOfPursuitCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = ARCLIGHT,
		name = ARCLIGHT,
		description = ARCLIGHT,
		section = debug
	)
	default int getArclightCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = ASH_SANCTIFIER,
		name = ASH_SANCTIFIER,
		description = ASH_SANCTIFIER,
		section = debug
	)
	default int getAshSanctifierCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = ASH_SANCTIFIER_STATUS,
		name = ASH_SANCTIFIER_STATUS,
		description = ASH_SANCTIFIER_STATUS,
		section = debug
	)
	default ItemActivity getAshSanctifierStatus() {
		return ItemActivity.ACTIVATED;
	}

	@ConfigItem(
		keyName = BINDING_NECKLACE,
		name = BINDING_NECKLACE,
		description = BINDING_NECKLACE,
		section = debug
	)
	default int getBindingNecklaceCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = BONECRUSHER,
		name = BONECRUSHER,
		description = BONECRUSHER,
		section = debug
	)
	default int getBoneCrusherCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = EFARITAYS_AID,
		name = EFARITAYS_AID,
		description = EFARITAYS_AID,
		section = debug
	)
	default int getEfaritaysAidCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = BONECRUSHER_STATUS,
		name = BONECRUSHER_STATUS,
		description = BONECRUSHER_STATUS,
		section = debug
	)
	default ItemActivity getBoneCrusherStatus() {
		return ItemActivity.ACTIVATED;
	}

	@ConfigItem(
		keyName = KHAREDSTS_MEMOIRS,
		name = KHAREDSTS_MEMOIRS,
		description = KHAREDSTS_MEMOIRS,
		section = debug
	)
	default int getKharedstsMemoirsCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = BOTTOMLESS_COMPOST_BUCKET + _STORAGE,
		name = BOTTOMLESS_COMPOST_BUCKET + _STORAGE,
		description = BOTTOMLESS_COMPOST_BUCKET + _STORAGE,
		section = debug
	)
	default String getBottomlessCompostBucketStorage() {
		return "";
	}

	@ConfigItem(
		keyName = MASTER_SCROLL_BOOK + _STORAGE,
		name = MASTER_SCROLL_BOOK + _STORAGE,
		description = MASTER_SCROLL_BOOK + _STORAGE,
		section = debug
	)
	default String masterScrollBookStorage() {
		return "";
	}

	@ConfigItem(
		keyName = BRACELET_OF_SLAUGHTER,
		name = BRACELET_OF_SLAUGHTER,
		description = BRACELET_OF_SLAUGHTER,
		section = debug
	)
	default int getBraceletOfSlaughterCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = BRYOPHYTAS_STAFF,
		name = BRYOPHYTAS_STAFF,
		description = BRYOPHYTAS_STAFF,
		section = debug
	)
	default int getBryophytasStaffCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CELESTIAL_RING,
		name = CELESTIAL_RING,
		description = CELESTIAL_RING,
		section = debug
	)
	default int getCelestialRingCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CHRONICLE,
		name = CHRONICLE,
		description = CHRONICLE,
		section = debug
	)
	default int getChronicleCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CRYSTAL_SHIELD,
		name = CRYSTAL_SHIELD,
		description = CRYSTAL_SHIELD,
		section = debug
	)
	default int getCrystalShieldCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CRYSTAL_BOW,
		name = CRYSTAL_BOW,
		description = CRYSTAL_BOW,
		section = debug
	)
	default int getCrystalBowCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = EXPEDITIOUS_BRACELET,
		name = EXPEDITIOUS_BRACELET,
		description = EXPEDITIOUS_BRACELET,
		section = debug
	)
	default int getBraceletOfExpeditiousCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = FALADOR_SHIELD,
		name = FALADOR_SHIELD,
		description = FALADOR_SHIELD,
		section = debug
	)
	default int getFaladorShieldCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = FISH_BARREL + _STORAGE,
		name = FISH_BARREL + _STORAGE,
		description = FISH_BARREL + _STORAGE,
		section = debug
	)
	default String getFishBarrelStorage() {
		return "";
	}

	@ConfigItem(
		keyName = FLAMTAER_BAG + _STORAGE,
		name = FLAMTAER_BAG + _STORAGE,
		description = FLAMTAER_BAG + _STORAGE,
		section = debug
	)
	default String getFlamtaerBagStorage() {
		return "";
	}

	@ConfigItem(
		keyName = GRICOLLERS_CAN,
		name = GRICOLLERS_CAN,
		description = GRICOLLERS_CAN,
		section = debug
	)
	default int getGricollersCanCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = IBANS_STAFF,
		name = IBANS_STAFF,
		description = IBANS_STAFF,
		section = debug
	)
	default int getIbansStaffCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = MOONS_GEAR + "_eclipse_chestplate",
		name = MOONS_GEAR + "_eclipse_chestplate",
		description = MOONS_GEAR + "_eclipse_chestplate",
		section = debug
	)
	default int getEclipseMoonChestplateCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = PHARAOHS_SCEPTRE,
		name = PHARAOHS_SCEPTRE,
		description = PHARAOHS_SCEPTRE,
		section = debug
	)
	default int getPharaohsSceptreCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = REAGENT_POUCH + _STORAGE,
		name = REAGENT_POUCH + _STORAGE,
		description = REAGENT_POUCH + _STORAGE,
		section = debug
	)
	default String getReagentPouchStorage() {
		return "";
	}

	@ConfigItem(
		keyName = RING_OF_FORGING,
		name = RING_OF_FORGING,
		description = RING_OF_FORGING,
		section = debug
	)
	default int getRingOfForgingCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = RING_OF_SUFFERING,
		name = RING_OF_SUFFERING,
		description = RING_OF_SUFFERING,
		section = debug
	)
	default int getRingOfSufferingCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = RING_OF_SUFFERING_STATUS,
		name = RING_OF_SUFFERING_STATUS,
		description = RING_OF_SUFFERING_STATUS,
		section = debug
	)
	default ItemActivity getRingOfSufferingStatus() {
		return ItemActivity.ACTIVATED;
	}

	@ConfigItem(
		keyName = SANGUINESTI_STAFF,
		name = SANGUINESTI_STAFF,
		description = SANGUINESTI_STAFF,
		section = debug
	)
	default int getSanguinestiStaffCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = SCYTHE_OF_VITUR,
		name = SCYTHE_OF_VITUR,
		description = SCYTHE_OF_VITUR,
		section = debug
	)
	default int getScytheOfViturCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = SKULL_SCEPTRE,
		name = SKULL_SCEPTRE,
		description = SKULL_SCEPTRE,
		section = debug
	)
	default int getSkullSceptreCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = SOUL_BEARER,
		name = SOUL_BEARER,
		description = SOUL_BEARER,
		section = debug
	)
	default int getSoulBearerCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = TRIDENT_OF_THE_SEAS,
		name = TRIDENT_OF_THE_SEAS,
		description = TRIDENT_OF_THE_SEAS,
		section = debug
	)
	default int getTridentOfTheSeasCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = XERICS_TALISMAN,
		name = XERICS_TALISMAN,
		description = XERICS_TALISMAN,
		section = debug
	)
	default int getXericsTalismanCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = SAILORS_AMULET,
		name = SAILORS_AMULET,
		description = SAILORS_AMULET,
		section = debug
	) default int getSailorsAmuletCharges() { return ChargeId.UNKNOWN; }

	@ConfigItem(
		keyName = DRAGONFIRE_SHIELD,
		name = DRAGONFIRE_SHIELD,
		description = DRAGONFIRE_SHIELD,
		section = debug
	)
	default int getDragonfireShieldCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CAMULET,
		name = CAMULET,
		description = CAMULET,
		section = debug
	)
	default int getCamuletCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CIRCLET_OF_WATER,
		name = CIRCLET_OF_WATER,
		description = CIRCLET_OF_WATER,
		section = debug
	)
	default int getCircletOfWaterCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CHUGGING_BARREL + _STORAGE,
		name = CHUGGING_BARREL + _STORAGE,
		description = CHUGGING_BARREL + _STORAGE,
		section = debug
	)
	default String getChuggingBarrelStorage() {
		return "";
	}

	@ConfigItem(
		keyName = TELEPORT_CRYSTAL,
		name = TELEPORT_CRYSTAL,
		description = TELEPORT_CRYSTAL,
		section = debug
	)
	default int getTeleportCrystalCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = BRACELET_OF_CLAY,
		name = BRACELET_OF_CLAY,
		description = BRACELET_OF_CLAY,
		section = debug
	)
	default int getBraceletOfClayCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = COFFIN,
		name = COFFIN,
		description = COFFIN,
		section = debug
	)
	default int getCoffinCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = HUNTSMANS_KIT + _STORAGE,
		name = HUNTSMANS_KIT + _STORAGE,
		description = HUNTSMANS_KIT + _STORAGE,
		section = debug
	)
	default String getHuntsmansKitStorage() {
		return "";
	}

	@ConfigItem(
		keyName = LOG_BASKET + _STORAGE,
		name = LOG_BASKET + _STORAGE,
		description = LOG_BASKET + _STORAGE,
		section = debug
	)
	default String getLogBasketStorage() {
		return "";
	}

	@ConfigItem(
		keyName = FORESTRY_BASKET + _STORAGE,
		name = FORESTRY_BASKET + _STORAGE,
		description = FORESTRY_BASKET + _STORAGE,
		section = debug
	)
	default String getForestryBasketStorage() {
		return "";
	}

	@ConfigItem(
		keyName = FORESTRY_KIT + _STORAGE,
		name = FORESTRY_KIT + _STORAGE,
		description = FORESTRY_KIT + _STORAGE,
		section = debug
	)
	default String getForestryKitStorage() {
		return "";
	}

	@ConfigItem(
		keyName = ARDOUGNE_CLOAK,
		name = ARDOUGNE_CLOAK,
		description = ARDOUGNE_CLOAK,
		section = debug
	)
	default int getArdougneCloakCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = MAGIC_CAPE,
		name = MAGIC_CAPE,
		description = MAGIC_CAPE,
		section = debug
	)
	default int getMagicCapeCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = MEAT_POUCH + _STORAGE,
		name = MEAT_POUCH + _STORAGE,
		description = MEAT_POUCH + _STORAGE,
		section = debug
	)
	default String getMeatPouchStorageCharges() {
		return "";
	}

	@ConfigItem(
		keyName = GEM_BAG + _STORAGE,
		name = GEM_BAG + _STORAGE,
		description = GEM_BAG + _STORAGE,
		section = debug
	)
	default String getGemBagStorageCharges() {
		return "";
	}

	@ConfigItem(
		keyName = SEED_BOX,
		name = SEED_BOX,
		description = SEED_BOX,
		section = debug
	)
	default int getSeedBoxCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = SEED_BOX + _STORAGE,
		name = SEED_BOX + _STORAGE,
		description = SEED_BOX + _STORAGE,
		section = debug
	)
	default String getSeedBoxStorage() {
		return "";
	}

	@ConfigItem(
		keyName = CRYSTAL_HELM,
		name = CRYSTAL_HELM,
		description = CRYSTAL_HELM,
		section = debug
	)
	default int getCrystalHelmCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CRYSTAL_BODY,
		name = CRYSTAL_BODY,
		description = CRYSTAL_BODY,
		section = debug
	)
	default int getCrystalBodyCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CRYSTAL_LEGS,
		name = CRYSTAL_LEGS,
		description = CRYSTAL_LEGS,
		section = debug
	)
	default int getCrystalLegsCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CRYSTAL_HALBERD,
		name = CRYSTAL_HALBERD,
		description = CRYSTAL_HALBERD,
		section = debug
	)
	default int getCrystalHalberdCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = RING_OF_SHADOWS,
		name = RING_OF_SHADOWS,
		description = RING_OF_SHADOWS,
		section = debug
	)
	default int getRingOfShadowsCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = COAL_BAG,
		name = COAL_BAG,
		description = COAL_BAG,
		section = debug
	)
	default int getCoalBagCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = COLOSSAL_POUCH + _STORAGE,
		name = COLOSSAL_POUCH + _STORAGE,
		description = COLOSSAL_POUCH + _STORAGE,
		section = debug
	)
	default String getColossalPouchStorage() {
		return "";
	}

	@ConfigItem(
		keyName = COLOSSAL_POUCH_DECAY_COUNT,
		name = COLOSSAL_POUCH_DECAY_COUNT,
		description = "Colossal pouch decay count",
		section = debug
	)
	default int getColossalPouchDecayCount() {
		return 0;
	}

	@ConfigItem(
		keyName = HERB_SACK + _STORAGE,
		name = HERB_SACK + _STORAGE,
		description = HERB_SACK + _STORAGE,
		section = debug
	)
	default String getHerbSackStorage() {
		return "";
	}

	@ConfigItem(
		keyName = ESCAPE_CRYSTAL_STATUS,
		name = ESCAPE_CRYSTAL_STATUS,
		description = ESCAPE_CRYSTAL_STATUS,
		section = debug
	)
	default ItemActivity getEscapeCrystalStatus() {
		return ItemActivity.DEACTIVATED;
	}

	@ConfigItem(
		keyName = ESCAPE_CRYSTAL_INACTIVITY_PERIOD,
		name = ESCAPE_CRYSTAL_INACTIVITY_PERIOD,
		description = ESCAPE_CRYSTAL_INACTIVITY_PERIOD,
		section = debug
	)
	default int getEscapeCrystalInactivityPeriod() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = STRANGE_OLD_LOCKPICK,
		name = STRANGE_OLD_LOCKPICK,
		description = STRANGE_OLD_LOCKPICK,
		section = debug
	)
	default int getStrangeOldLockCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = DESERT_AMULET,
		name = DESERT_AMULET,
		description = DESERT_AMULET,
		section = debug
	)
	default int getDesertAmuletCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = TOME_OF_FIRE,
		name = TOME_OF_FIRE,
		description = TOME_OF_FIRE,
		section = debug
	)
	default int getTomeOfFireCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = DODGY_NECKLACE,
		name = DODGY_NECKLACE,
		description = DODGY_NECKLACE,
		section = debug
	)
	default int getDodgyNecklaceCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = KANDARIN_HEADGEAR,
		name = KANDARIN_HEADGEAR,
		description = KANDARIN_HEADGEAR,
		section = debug
	)
	default int getKandarinHeadgearCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = FREMENNIK_SEA_BOOTS,
		name = FREMENNIK_SEA_BOOTS,
		description = FREMENNIK_SEA_BOOTS,
		section = debug
	)
	default int getFremennikSeaBootsCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = FUR_POUCH + _STORAGE,
		name = FUR_POUCH + _STORAGE,
		description = FUR_POUCH + _STORAGE,
		section = debug
	)
	default String getFurPouchStorageCharges() {
		return "";
	}

	@ConfigItem(
		keyName = JAR_GENERATOR,
		name = JAR_GENERATOR,
		description = JAR_GENERATOR,
		section = debug
	)
	default int getJarGeneratorCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = EXPLORERS_RING + _STORAGE,
		name = EXPLORERS_RING + _STORAGE,
		description = EXPLORERS_RING + _STORAGE,
		section = debug
	)
	default String getExplorersRingCharges() {
		return "";
	}

	@ConfigItem(
		keyName = ENCHANTED_LYRE,
		name = ENCHANTED_LYRE,
		description = ENCHANTED_LYRE,
		section = debug
	)
	default int getEnchantedLyreCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = RING_OF_THE_ELEMENTS,
		name = RING_OF_THE_ELEMENTS,
		description = RING_OF_THE_ELEMENTS,
		section = debug
	)
	default int getRingOfElementsCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = RING_OF_ENDURANCE,
		name = RING_OF_ENDURANCE,
		description = RING_OF_ENDURANCE,
		section = debug
	)
	default int getRingOfEnduranceCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = PLANK_SACK,
		name = PLANK_SACK,
		description = PLANK_SACK,
		section = debug
	)
	default int getPlankSackCharges() {
		return ChargeId.UNKNOWN;
	}


	@ConfigItem(
		keyName = PLANK_SACK + _STORAGE,
		name = PLANK_SACK + _STORAGE,
		description = PLANK_SACK + _STORAGE,
		section = debug
	)
	default String getPlankSackStorage() {
		return "";
	}

	@ConfigItem(
		keyName = SLAYER_STAFF_E,
		name = SLAYER_STAFF_E,
		description = SLAYER_STAFF_E,
		section = debug
	)
	default int getSlayerStaffECharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = WARPED_SCEPTRE,
		name = WARPED_SCEPTRE,
		description = WARPED_SCEPTRE,
		section = debug
	)
	default int getWarpedSceptreCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = CRYSTAL_SAW,
		name = CRYSTAL_SAW,
		description = CRYSTAL_SAW,
		section = debug
	)
	default int getCrystalSawCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = QUETZAL_WHISTLE,
		name = QUETZAL_WHISTLE,
		description = QUETZAL_WHISTLE,
		section = debug
	)
	default int getQuetzalWhistleCharges() {
		return ChargeId.UNKNOWN;
	}

	@ConfigItem(
		keyName = TACKLE_BOX + _STORAGE,
		name = TACKLE_BOX + _STORAGE,
		description = TACKLE_BOX + _STORAGE,
		section = debug
	)
	default String getTackleBoxStorage() {
		return "";
	}

	@ConfigItem(
		keyName = AMULET_OF_CHEMISTRY,
		name = AMULET_OF_CHEMISTRY,
		description = AMULET_OF_CHEMISTRY,
		section = debug
	)
	default int getAmuletOfChemistryCharges() {
		return ChargeId.UNKNOWN;
	}
}
