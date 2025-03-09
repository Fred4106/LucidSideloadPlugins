package com.fredplugins.kroovy;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Item;

import java.util.Arrays;
import java.util.stream.Stream;

@Getter
public class Product extends Recipe
{

	private final ActionEnum action;

	public Product(ActionEnum action, int productId, Ingredient... requirements)
	{
		super(productId, requirements);
		this.action = action;
	}

	public Product(ActionEnum action, int productId, Ingredient[] requirements, Ingredient tool)
	{
		super(productId, requirements, tool);
		this.action = action;
	}

	public Product(ActionEnum action, int productId, Boolean isSelectingIngredientAsProduct, Ingredient... requirements)
	{
		super(productId, isSelectingIngredientAsProduct, requirements);
		this.action = action;
	}

	public boolean isMadeWith(Item... items)
	{
		return Stream.of(items)
					 .mapToInt(Item::getId)
					 .allMatch(id -> Arrays.stream(this.getRequirements())
										   .mapToInt(Ingredient::getItemId)
										   .anyMatch(i -> i == id));
	}
	public boolean IngredientsIsIncludedIn(String ingredientString, Client client){
		return Stream.of(getRequirements())
					 .anyMatch(ing -> ingredientString.toLowerCase().contains(
							 client.getItemDefinition(ing.getItemId()).getName().toLowerCase())
					 );
	}
}