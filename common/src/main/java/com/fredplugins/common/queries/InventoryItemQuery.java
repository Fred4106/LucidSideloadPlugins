/*
 * Copyright (c) 2016-2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.fredplugins.common.queries;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import com.fredplugins.common.QueryResults;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

public class InventoryItemQuery extends WidgetItemQuery
{
	@Override
	public QueryResults<WidgetItem> result(Client client)
	{
		Collection<WidgetItem> widgetItems = getInventoryItems(client);
		return new QueryResults<>(widgetItems.stream()
			.filter(Objects::nonNull)
			.filter(predicate)
			.collect(Collectors.toList()));
	}

	private Collection<WidgetItem> getInventoryItems(Client client)
	{
		Collection<WidgetItem> widgetItems = new ArrayList<>();
		Widget inv = client.getWidget(WidgetInfo.INVENTORY);
		if (inv != null && !inv.isHidden())
		{
			Widget[] children = inv.getDynamicChildren();
			for (int i = 0; i < children.length; i++)
			{
				Widget child = children[i];
				if (child.getItemId() == 6512 || child.isSelfHidden())
				{
					continue;
				}
				// set bounds to same size as default inventory
				Rectangle bounds = child.getBounds();
				bounds.setBounds(bounds.x - 1, bounds.y - 1, 32, 32);
				// Index is set to 0 because the widget's index does not correlate to the order in the bank
				widgetItems.add(new WidgetItem(child.getItemId(), child.getItemQuantity(), bounds, child, null));
			}
		}
		return widgetItems;
	}
}
