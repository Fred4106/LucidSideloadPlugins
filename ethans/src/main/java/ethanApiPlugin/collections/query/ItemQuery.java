package ethanApiPlugin.collections.query;

import ethanApiPlugin.EthanApiPlugin;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemQuery {
	private List<Widget> items;
	static Client client = RuneLite.getInjector().getInstance(Client.class);
	static ItemManager itemManager = RuneLite.getInjector().getInstance(ItemManager.class);

	public ItemQuery(List<Widget> items) {
		this.items = new ArrayList(items);
	}

	public ItemQuery filter(Predicate<? super Widget> predicate) {
		items = items.stream().filter(predicate).collect(Collectors.toList());
		return this;
	}

	public ItemQuery withAction(String action) {
		items = items.stream().filter(item -> Arrays.asList(item.getActions()).contains(action)).collect(Collectors.toList());
		return this;
	}

	public ItemQuery tradeAble() {
		return filterItem(ItemComposition::isTradeable);
	}

	public ItemQuery differenceInValueLessThan(int difference) {
//		items = items.stream().filter(item -> Math.abs(itemManager.getItemComposition(item.getItemId()).getHaPrice() - itemManager.getItemPriceWithSource(item.getItemId(), true)) < difference).collect(Collectors.toList());
		return filterItem(ic -> ic.getHaPrice() - itemManager.getItemPriceWithSource(ic.getId(), true) < difference);//.getPrice()
//		return this;
	}

	public ItemQuery priceOver(int price) {
//		items = items.stream().filter(item -> itemManager.getItemComposition(item.getItemId()).getHaPrice() >= price).collect(Collectors.toList());
//		return this;
		return filterItem(ic-> ic.getPrice() >= price);
	}

	public ItemQuery withSet(Set<Integer> ids) {
		items = items.stream()
			.filter(item -> ids.contains(item.getItemId()))
			.collect(Collectors.toList());
		return this;
	}

//	public ItemQuery withId(int id) {
//		items = items.stream().filter(item -> item.getItemId() == id).collect(Collectors.toList());
//		return this;
//	}

	public ItemQuery withId(int ... ids) {
		items = items.stream().filter(item -> ArrayUtils.contains(ids, item.getItemId())).collect(Collectors.toList());
		return this;
	}

	public ItemQuery withIdFilter(IntPredicate idPredicate) {
		items = items.stream().filter(item -> idPredicate.test(item.getItemId())).collect(Collectors.toList());
		return this;
	}

	public ItemQuery withName(String name) {
		items = items.stream().filter(item -> Text.removeTags(item.getName()).equals(Text.removeTags(name))).collect(Collectors.toList());
		return this;
	}

	public ItemQuery quantityGreaterThan(int quanity) {
		items = items.stream().filter(item -> item.getItemQuantity() > quanity).collect(Collectors.toList());
		return this;
	}

	public ItemQuery nameContains(String name) {
		items = items.stream().filter(item -> item.getName().contains(name)).collect(Collectors.toList());
		return this;
	}

	public ItemQuery nonPlaceHolder() {
		return quantityGreaterThan(0);
	}

	public ItemQuery idInList(List<Integer> ids) {
		items = items.stream().filter(item -> ids.contains(item.getItemId())).collect(Collectors.toList());
		return this;
	}

	public ItemQuery nameInList(List<String> names) {
		items = items.stream()
			.filter(item -> names.stream()
				.map(String::toLowerCase)
				.anyMatch(name -> Text.removeTags(item.getName()).equalsIgnoreCase(name)))
			.collect(Collectors.toList());
		return this;
	}

	public ItemQuery indexIs(int index) {
		items = items.stream().filter(item -> item.getIndex() == index).collect(Collectors.toList());
		return this;
	}

	public ItemQuery matchesWildCardNoCase(String input) {
		items =
			items.stream().
				filter(item -> WildcardMatcher.matches(input.toLowerCase(), Text.removeTags(item.getName().toLowerCase()))).
				collect(Collectors.toList());
		return this;
	}

	public ItemQuery filterItem(Predicate<ItemComposition> predicate) {
		Function<ItemComposition, Boolean> predFunc = predicate::test;
		return filter(predFunc
			.compose(EthanApiPlugin.itemDefs::getUnchecked)
			.compose(Widget::getItemId)
			::apply
		);
	}

	private static final Predicate<ItemComposition> stackable = ItemComposition::isStackable;
	private static final Predicate<ItemComposition> noted = (ItemComposition i) -> i.getNote() != 1;

	public ItemQuery onlyStackable() {
		return filterItem(stackable);
	}
	
	public ItemQuery onlyNoted() {
		return filterItem(noted);
	}
	
	public ItemQuery onlyUnstackable() {
		return filterItem(stackable.negate());
	}

	public ItemQuery onlyUnnoted() {
		return filterItem(noted.negate());
	}

	public boolean empty() {
		return items.size() == 0;
	}

	public ItemQuery filterUnique() {
		items = items.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Widget::getItemId))), ArrayList::new));
		return this;
	}

	public List<Widget> result() {
		return items;
	}

	public Optional<Widget> first() {
		Widget returnWidget = null;
		if (items.size() == 0) {
			return Optional.ofNullable(null);
		}
		return Optional.ofNullable(items.get(0));
	}
}