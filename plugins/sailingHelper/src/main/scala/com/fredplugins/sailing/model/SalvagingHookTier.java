package com.fredplugins.sailing.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ObjectID;

@RequiredArgsConstructor
@Getter
public enum SalvagingHookTier
{

	BRONZE(
		new int[]{
			ObjectID.SALVAGING_HOOK_RAFT_BRONZE,
			ObjectID.SALVAGING_HOOK_BRONZE,
			ObjectID.SALVAGING_HOOK_LARGE_BRONZE,
			ObjectID.SALVAGING_HOOK_LARGE_BRONZE_B,
		}
	),
	IRON(
		new int[]{
			ObjectID.SALVAGING_HOOK_RAFT_IRON,
			ObjectID.SALVAGING_HOOK_IRON,
			ObjectID.SALVAGING_HOOK_LARGE_IRON,
			ObjectID.SALVAGING_HOOK_LARGE_IRON_B,
		}
	),
	STEEL(
		new int[]{
			ObjectID.SALVAGING_HOOK_RAFT_STEEL,
			ObjectID.SALVAGING_HOOK_STEEL,
			ObjectID.SALVAGING_HOOK_LARGE_STEEL,
			ObjectID.SALVAGING_HOOK_LARGE_STEEL_B,
		}
	),
	MITHRIL(
		new int[]{
			ObjectID.SALVAGING_HOOK_RAFT_MITHRIL,
			ObjectID.SALVAGING_HOOK_MITHRIL,
			ObjectID.SALVAGING_HOOK_LARGE_MITHRIL,
			ObjectID.SALVAGING_HOOK_LARGE_MITHRIL_B,
		}
	),
	ADAMANT(
		new int[]{
			ObjectID.SALVAGING_HOOK_RAFT_ADAMANT,
			ObjectID.SALVAGING_HOOK_ADAMANT,
			ObjectID.SALVAGING_HOOK_LARGE_ADAMANT,
			ObjectID.SALVAGING_HOOK_LARGE_ADAMANT_B,
		}
	),
	RUNE(
		new int[]{
			ObjectID.SALVAGING_HOOK_RAFT_RUNE,
			ObjectID.SALVAGING_HOOK_RUNE,
			ObjectID.SALVAGING_HOOK_LARGE_RUNE,
			ObjectID.SALVAGING_HOOK_LARGE_RUNE_B,
		}
	),
	DRAGON(
		new int[]{
			ObjectID.SALVAGING_HOOK_RAFT_DRAGON,
			ObjectID.SALVAGING_HOOK_DRAGON,
			ObjectID.SALVAGING_HOOK_LARGE_DRAGON,
			ObjectID.SALVAGING_HOOK_LARGE_DRAGON_B,
		}
	),
	;

	private final int[] gameObjectIds;

	public static SalvagingHookTier fromGameObjectId(int id)
	{
		for (SalvagingHookTier tier : values())
		{
			for (int objectId : tier.getGameObjectIds())
			{
				if (objectId == id)
				{
					return tier;
				}
			}
		}

		return null;
	}

}