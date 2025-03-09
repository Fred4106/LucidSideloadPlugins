package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
import com.fredplugins.kroovy.api.Ingredient;
import com.fredplugins.kroovy.api.Product;
import com.fredplugins.kroovy.managers.InventoryManager;
import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import static net.runelite.api.ItemID.*;

public class UseItemOnItemDetector extends ActionDetector
{

	private static final Ingredient PESTLE_AND_MORTAR = new Ingredient(ItemID.PESTLE_AND_MORTAR, 1, false);
	private static final Ingredient CHISEL = new Ingredient(ItemID.CHISEL, 1, false);
	private static final String BREAK_DOWN = "Break-down";

	private static final Product[] PRODUCTS = {
			new Product(ActionEnum.GRIND, 				GROUND_ASHES, 					new Ingredient[]{ new Ingredient(ASHES)}, 														PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				CRUSHED_NEST, 					new Ingredient[]{ new Ingredient(BIRD_NEST_5075)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				DRAGON_SCALE_DUST, 				new Ingredient[]{ new Ingredient(BLUE_DRAGON_SCALE)}, 											PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_CHARCOAL, 				new Ingredient[]{ new Ingredient(CHARCOAL)}, 													PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				CHOCOLATE_DUST, 				new Ingredient[]{ new Ingredient(CHOCOLATE_BAR)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_CRAB_MEAT, 				new Ingredient[]{ new Ingredient(CRAB_MEAT)}, 													PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GOAT_HORN_DUST, 				new Ingredient[]{ new Ingredient(DESERT_GOAT_HORN)}, 											PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_THISTLE, 				new Ingredient[]{ new Ingredient(DRIED_THISTLE)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GORAK_CLAW_POWDER, 				new Ingredient[]{ new Ingredient(GORAK_CLAWS)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_GUAM, 					new Ingredient[]{ new Ingredient(GUAM_LEAF)}, 													PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				KEBBIT_TEETH_DUST, 				new Ingredient[]{ new Ingredient(KEBBIT_TEETH)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_KELP, 					new Ingredient[]{ new Ingredient(KELP)}, 														PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				LAVA_SCALE_SHARD, 				new Ingredient[]{ new Ingredient(LAVA_SCALE)}, 													PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_MUD_RUNES,				new Ingredient[]{ new Ingredient(MUD_RUNE)}, 													PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				MYSTERIOUS_CRUSHED_MEAT, 		new Ingredient[]{ new Ingredient(MYSTERIOUS_MEAT)}, 											PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				NIHIL_DUST, 					new Ingredient[]{ new Ingredient(NIHIL_SHARD)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				KARAMBWAN_PASTE, 				new Ingredient[]{ new Ingredient(POISON_KARAMBWAN)}, 											PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_COD, 					new Ingredient[]{ new Ingredient(RAW_COD)}, 													PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				RUNE_DUST, 						new Ingredient[]{ new Ingredient(RUNE_SHARDS)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_SEAWEED, 				new Ingredient[]{ new Ingredient(SEAWEED)}, 													PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				GROUND_TOOTH, 					new Ingredient[]{ new Ingredient(SUQAH_TOOTH)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				UNICORN_HORN_DUST, 				new Ingredient[]{ new Ingredient(UNICORN_HORN)}, 												PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND, 				CRUSHED_SUPERIOR_DRAGON_BONES, 	new Ingredient[]{ new Ingredient(SUPERIOR_DRAGON_BONES)}, 										PESTLE_AND_MORTAR),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONES)}, 												CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BAT_BONES)}, 											CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BIG_BONES)}, 											CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_ZOGRE_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BABYWYRM_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BABYDRAGON_BONES)}, 									CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_WYRM_BONES)}, 											CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(SUNKISSED_BONES)}, 											CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_WYVERN_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_DRAGON_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_DRAKE_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_FAYRG_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_LAVA_DRAGON_BONES)}, 									CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_RAURG_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_HYDRA_BONES)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(DAGANNOTH_BONES_29376)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_OURG_BONES)}, 											CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_SUPERIOR_DRAGON_BONES)}, 								CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONE_STATUETTE)}, 										CHISEL),
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONE_STATUETTE_29340)}, 								CHISEL), //Might not be required. Not sure what the difference is
			new Product(ActionEnum.GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONE_STATUETTE_29342)}, 								CHISEL), //Might not be required. Not sure what the difference is
			new Product(ActionEnum.GRIND_DARK_ESSENCE, DARK_ESSENCE_FRAGMENTS, 		new Ingredient[]{ new Ingredient(DARK_ESSENCE_BLOCK)},				 							CHISEL),
			new Product(ActionEnum.SUNFIRE_WINE, 		JUG_OF_SUNFIRE_WINE,			new Ingredient[]{ new Ingredient(JUG_OF_WINE), new Ingredient(SUNFIRE_SPLINTERS, 2)}, 	PESTLE_AND_MORTAR)
	};

	@Inject private InventoryManager inventoryManager;

	@Inject private Client client;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		if(evt.getMenuOption().equals(BREAK_DOWN)){
			for (Product product : PRODUCTS) {
				if(product.IngredientsIsIncludedIn(evt.getMenuTarget(), client)){
					int amount = product.getMakeProductCount(this.inventoryManager);
					if (amount > 0) {
						this.actionManager.setAction(product.getAction(), amount, product.getProductId());
					}
				}
			}
		}
		if (evt.getMenuAction() != MenuAction.WIDGET_TARGET_ON_WIDGET) {
			return;
		}
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		Widget widget = this.client.getSelectedWidget();
		if (inventory == null|| widget == null) {
			return;
		}

		//Given evt.getMenuTarget() is in the following format <col=ff9040>Bird nest</col><col=ffffff> -> <col=ff9040>Bird nest</col>
		//The code below will check if the source and the target are the same, and return if it is the case
		Pattern r = Pattern.compile("<.*>(.*)</.*><.*>(.*)</.*>");
		Matcher m = r.matcher(evt.getMenuTarget());
		if (m.find() && Objects.equals(m.group(1), m.group(2))){
			return;
		}

		Item[] items = IntStream.of(widget.getId(), evt.getParam0())
								.mapToObj(inventory::getItem)
								.filter(n -> n!= null)
								.toArray(Item[]::new);

		for (Product product : PRODUCTS) {
			if (product.isMadeWith(items)) {
				int amount = product.getMakeProductCount(this.inventoryManager);
				if (amount > 0) {
					this.actionManager.setAction(product.getAction(), amount, product.getProductId());
				}
			}
		}
	}

	@Override
	public void setup() {

	}

	@Override
	public void shutDown() {

	}
}
