package com.fredplugins.kroovy;

import lombok.Getter;

import java.util.function.Function;

enum ActionEnum
{
	COLLECT_SAND("Collecting", FredsActionProgressConfig::collectSand, ActionIcon.SPRITE_BUCKET, 1, 2),
	COOKING_UNKNOWN("Cooking ???", FredsActionProgressConfig::cookingCooking, ActionIcon.SPRITE_COOKING, 2, 3, 4),
	COOKING_134("Cooking 134", FredsActionProgressConfig::cookingCooking, ActionIcon.SPRITE_COOKING, 1, 3, 4),
	COOKING_14("Cooking 14", FredsActionProgressConfig::cookingCooking, ActionIcon.SPRITE_COOKING, 1, 4),
	COOKING_3("Cooking 3", FredsActionProgressConfig::cookingCooking, ActionIcon.SPRITE_COOKING, 3),
	COOKING_24("Cooking 24", FredsActionProgressConfig::cookingCooking, ActionIcon.SPRITE_COOKING, 2,4),
	COOKING_CUT_FRUIT("Cutting", FredsActionProgressConfig::cookingCutFruit, ActionIcon.SPRITE_COOKING, 2),
	COOKING_MIX_DOUGH("Combining", FredsActionProgressConfig::cookingMixPastry, ActionIcon.SPRITE_COOKING, 1, 2),
	COOKING_MIX_GRAPES("Combining", FredsActionProgressConfig::cookingMixWines, ActionIcon.SPRITE_COOKING, 3, 2),
	COOKING_TOP_PIZZA("Combining", FredsActionProgressConfig::cookingTopPizza, ActionIcon.SPRITE_COOKING, 2),
	CRAFT_BATTLESTAVES("Combining", FredsActionProgressConfig::craftBattlestaves, ActionIcon.SPRITE_CRAFTING, 3, 2),
	CRAFT_BLOW_GLASS("Glassblowing", FredsActionProgressConfig::craftGlassblowing, ActionIcon.SPRITE_CRAFTING, 3),
	CRAFT_CAST_GOLD_AND_SILVER("Casting", FredsActionProgressConfig::craftCastGoldAndSilver, ActionIcon.SPRITE_CRAFTING, 3),
	CRAFT_CUT_GEMS("Cutting", FredsActionProgressConfig::craftCutGems, ActionIcon.SPRITE_CRAFTING, 2),
	CRAFT_HARD_LEATHER("Leather-working", FredsActionProgressConfig::craftLeatherWorking, ActionIcon.SPRITE_CRAFTING, 2),
	CRAFT_LEATHER("Leather-working", FredsActionProgressConfig::craftLeatherWorking, ActionIcon.SPRITE_CRAFTING, 3),
	CRAFT_MOLTEN_GLASS("Creating", FredsActionProgressConfig::craftMakeMoltenGlass, ActionIcon.SPRITE_CRAFTING, 2),
	CRAFT_STRING_JEWELLERY("Stringing", FredsActionProgressConfig::craftStringJewellery, ActionIcon.SPRITE_CRAFTING, 2),
	CRAFT_LOOM("Weaving,", FredsActionProgressConfig::craftWeaving,ActionIcon.SPRITE_CRAFTING, 4, 3),
	CRAFT_LOOM_DRIFT_NET("Weaving,", FredsActionProgressConfig::craftWeaving,ActionIcon.SPRITE_CRAFTING, 3),
	CRAFT_SHIELD("Crafting", FredsActionProgressConfig::craftShields, ActionIcon.SPRITE_CRAFTING, 5),
	CRAFT_AMETHYST_HEADS_AND_TIPS("Crafting", FredsActionProgressConfig::craftHeadsAndTips,ActionIcon.SPRITE_CRAFTING,2),
	FLETCH_ATTACH("Attaching", FredsActionProgressConfig::fletchArrowsAndBolts, ActionIcon.SPRITE_FLETCHING, 2),
	FLETCH_ATTACH_3T("Attaching", FredsActionProgressConfig::fletchArrowsAndBolts, ActionIcon.SPRITE_FLETCHING, 3),
	FLETCH_CUT_ARROW_SHAFT("Cutting", FredsActionProgressConfig::fletchArrowsAndBolts, ActionIcon.SPRITE_FLETCHING, 3),
	FLETCH_CUT_BOW("Cutting", FredsActionProgressConfig::fletchBows, ActionIcon.SPRITE_FLETCHING, 3),
	FLETCH_CUT_TIPS("Cutting", FredsActionProgressConfig::fletchArrowsAndBolts, ActionIcon.SPRITE_FLETCHING, 5),
	FLETCH_ATTACH_TIPS("Attaching", FredsActionProgressConfig::fletchArrowsAndBolts, ActionIcon.SPRITE_FLETCHING, 2),
	FLETCH_CUT_TIPS_AMETHYST("Cutting", FredsActionProgressConfig::fletchArrowsAndBolts, ActionIcon.SPRITE_FLETCHING, 2),
	FLETCH_STRING_BOW("Stringing", FredsActionProgressConfig::fletchBows, ActionIcon.SPRITE_FLETCHING, 2),
	FLETCH_SHIELD("Cutting", FredsActionProgressConfig::fletchShields, ActionIcon.SPRITE_FLETCHING, 7),
	FLETCH_CUT_CROSSBOW("Cutting", FredsActionProgressConfig::fletchCrossbows, ActionIcon.SPRITE_FLETCHING, 2, 3),
	FLETCH_ATTACH_CROSSBOW("Attaching", FredsActionProgressConfig::fletchCrossbows, ActionIcon.SPRITE_FLETCHING, 2),
	FLETCH_STRING_CROSSBOW("Stringing", FredsActionProgressConfig::fletchCrossbows, ActionIcon.SPRITE_FLETCHING, 2),
	FLETCH_SPINNING("Spinning", FredsActionProgressConfig::fletchSpinning, ActionIcon.SPRITE_FLETCHING, 3),
	FLETCH_JAVELIN("Attaching", FredsActionProgressConfig::fletchJavelin, ActionIcon.SPRITE_FLETCHING, 2),
	FLETCH_DART("Attaching", FredsActionProgressConfig::fletchArrowsAndBolts, ActionIcon.SPRITE_FLETCHING, 2),
	GRIND("Grinding", FredsActionProgressConfig::grinding, ActionIcon.SPRITE_TOTAL, 0, 2, 3),
	GRIND_BONE_SHARDS("Grinding", FredsActionProgressConfig::grinding, ActionIcon.SPRITE_TOTAL, 1, 4),
	GRIND_DARK_ESSENCE("Grinding", FredsActionProgressConfig::grinding, ActionIcon.SPRITE_TOTAL, 1, 3, 4),
	SUNFIRE_WINE("Grinding", FredsActionProgressConfig::grinding, ActionIcon.SPRITE_TOTAL, 1, 4),
	HERB_CLEAN("Cleaning", FredsActionProgressConfig::herbCleaning, ActionIcon.SPRITE_HERBLORE, 0, 2),
	HERB_MIX_POTIONS("Mixing", FredsActionProgressConfig::herbPotions, ActionIcon.SPRITE_HERBLORE, 2),
	HERB_MIX_POTIONS_3T("Mixing", FredsActionProgressConfig::herbPotions, ActionIcon.SPRITE_HERBLORE, 0, 3),
	HERB_MIX_TAR("Mixing", FredsActionProgressConfig::herbTar, ActionIcon.SPRITE_HERBLORE, 2, 3),
	HERB_MIX_UNFINISHED("Mixing", FredsActionProgressConfig::herbPotions, ActionIcon.SPRITE_HERBLORE, 2, 1),
	MAGIC_CREATE_TABLET("Enchanting", FredsActionProgressConfig::magicTablets, ActionIcon.SPRITE_MAGIC, 2, 4),
	MAGIC_ENCHANT_BOLTS("Enchanting", FredsActionProgressConfig::magicEnchantBolts, ActionIcon.SPRITE_MAGIC, 1, 3),
	MAGIC_ENCHANT_JEWELLERY("Enchanting", FredsActionProgressConfig::magicEnchantJewellery, ActionIcon.SPRITE_MAGIC, 0, 7),
	MAGIC_CHARGE_ORB("Charging", FredsActionProgressConfig::magicChargeOrbs, ActionIcon.SPRITE_MAGIC, 3, 6),
	MAGIC_STRING_JEWELLERY("Stringing jewellery", FredsActionProgressConfig::magicStringJewellery, ActionIcon.SPRITE_MAGIC, 3),
	MAGIC_PLANK_MAKE("Making plank", FredsActionProgressConfig::magicPlankMake, ActionIcon.SPRITE_MAGIC, 3, 6),
	MAKING_FORESTERS_RATION("Making rations", FredsActionProgressConfig::forestersRation, ActionIcon.SPRITE_WOODCUTTING, 3),
	SMELTING("Smelting", FredsActionProgressConfig::smithSmelting, ActionIcon.SPRITE_SMITHING, 5),
	SMELTING_CANNONBALLS("Casting", FredsActionProgressConfig::smithCannonballs, ActionIcon.SPRITE_SMITHING, 7, 10),
	SMITHING("Forging", FredsActionProgressConfig::smithSmithing, ActionIcon.SPRITE_SMITHING, 5),
	SMITHING_WITH_SMITH_OUTFIT("Forging", FredsActionProgressConfig::smithSmithing, ActionIcon.SPRITE_SMITHING, 5),
	TEMPOROSS_COOKING("Cooking", FredsActionProgressConfig::temporossCooking, ActionIcon.SPRITE_COOKING, 4, 3),
	TEMPOROSS_FILL_CRATE("Filling", FredsActionProgressConfig::temporossFiring, ActionIcon.SPRITE_FISHING, 2),
	TEMPOROSS_REWARD_POOL("Fishing", FredsActionProgressConfig::temporossRewardPool, ActionIcon.SPRITE_FISHING, 1, 3),
	WINTERTODT_WOODCUTTING("Chopping", FredsActionProgressConfig::wintertodtWoodcutting, ActionIcon.SPRITE_WOODCUTTING, 2, 3),
	WINTERTODT_FIREMAKING("Lighting", FredsActionProgressConfig::wintertodtLighting, ActionIcon.SPRITE_FIREMAKING, 4, 3),
	WINTERTODT_FLETCHING("Cutting", FredsActionProgressConfig::wintertodtFletching, ActionIcon.SPRITE_FLETCHING, 4, 4),
	FARM_ULTRA_COMPOST("Mixing", FredsActionProgressConfig::farmUltraCompost, ActionIcon.SPRITE_FARMING,2),
	FIREMAKING_CAMPFIRE("Tending", FredsActionProgressConfig::campfire, ActionIcon.SPRITE_FIREMAKING,5,4),
	GUARDIAN_OF_THE_RIFT_CRAFTING("Crafting", FredsActionProgressConfig::guardianOfTheRiftCrafting, ActionIcon.SPRITE_CRAFTING, 1),
	//Timing might be off. Not on the wiki. Will need to confirm when having more rewards points
	GUARDIAN_OF_THE_RIFT_REWARD_POOL("Searching", FredsActionProgressConfig::guardianOfTheRiftRewardPool, ActionIcon.SPRITE_GUARDIAN_OF_THE_RIFT_REWARD, 3),
	CHURNING_CREAM("Churning", FredsActionProgressConfig::cookingChurning, ActionIcon.SPRITE_COOKING,10),
	CHURNING_BUTTER_WITH_MILK("Churning", FredsActionProgressConfig::cookingChurning, ActionIcon.SPRITE_COOKING,19),
	CHURNING_BUTTER_WITH_CREAM("Churning", FredsActionProgressConfig::cookingChurning, ActionIcon.SPRITE_COOKING,10),
	CHURNING_CHEESE_WITH_MILK("Churning", FredsActionProgressConfig::cookingChurning, ActionIcon.SPRITE_COOKING,26),
	CHURNING_CHEESE_WITH_CREAM("Churning", FredsActionProgressConfig::cookingChurning, ActionIcon.SPRITE_COOKING,19),
	CHURNING_CHEESE_WITH_BUTTER("Churning", FredsActionProgressConfig::cookingChurning, ActionIcon.SPRITE_COOKING,10),
	CHURNING_CHEESE_WITH_GARLIC("Churning", FredsActionProgressConfig::cookingChurning, ActionIcon.SPRITE_COOKING,10);

	@Getter
	private final String description;
	@Getter
	private final IconSource iconSource;
	private final int[] tickTimes;
	@Getter
	private final Function<FredsActionProgressConfig, Boolean> enabledFunction;

	public int[] getTickTimes() {
		return tickTimes.clone();
	}

	ActionEnum(
			String description,
			Function<FredsActionProgressConfig, Boolean> enabledFunction,
			IconSource iconSource,
			int tickTime0)
	{
		this(description, enabledFunction, iconSource, new int[] {tickTime0});
	}

	ActionEnum(
			String description,
			Function<FredsActionProgressConfig, Boolean> enabledFunction,
			IconSource iconSource,
			int tickTime0,
			int tickTime1,
			int tickTime2)
	{
		this(description, enabledFunction, iconSource, new int[] {tickTime0, tickTime1, tickTime2});
	}

	ActionEnum(
			String description,
			Function<FredsActionProgressConfig, Boolean> enabledFunction,
			IconSource iconSource,
			int tickTime0,
			int tickTime1)
	{
		this(description, enabledFunction, iconSource, new int[] {tickTime0, tickTime1});
	}

	ActionEnum(
			String description,
			Function<FredsActionProgressConfig, Boolean> enabledFunction,
			IconSource iconSource,
			int[] tickTimes)
	{
		this.description = description;
		this.enabledFunction = enabledFunction;
		this.iconSource = iconSource;
		// I did it this way so that we never accidentally create an action with zero tick times,
		// while preserving the convenience of var-args
		this.tickTimes = tickTimes;
	}
}