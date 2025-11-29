package com.fredplugins.sailing.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ObjectID;

@RequiredArgsConstructor
@Getter
public enum HullTier
{

	WOOD(
		new int[]{
			ObjectID.SAILING_BOAT_HULL_KANDARIN_1X3_WOOD,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_2X5_WOOD,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_3X8_WOOD,
		},
		new int[]{192, 192, 192},
		new int[]{320, 384, 448}
	),
	OAK(
		new int[]{
			ObjectID.SAILING_BOAT_HULL_KANDARIN_1X3_OAK,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_2X5_OAK,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_3X8_OAK,
		},
		new int[]{192, 192, 192},
		new int[]{320, 384, 448}
	),
	TEAK(
		new int[]{
			ObjectID.SAILING_BOAT_HULL_KANDARIN_1X3_TEAK,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_2X5_TEAK,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_3X8_TEAK,
		},
		new int[]{256, 256, 256},
		new int[]{320, 384, 448}
	),
	MAHOGANY(
		new int[]{
			ObjectID.SAILING_BOAT_HULL_KANDARIN_1X3_MAHOGANY,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_2X5_MAHOGANY,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_3X8_MAHOGANY,
		},
		new int[]{256, 256, 256},
		new int[]{320, 384, 448}
	),
	CAMPHOR(
		new int[]{
			ObjectID.SAILING_BOAT_HULL_KANDARIN_1X3_CAMPHOR,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_2X5_CAMPHOR,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_3X8_CAMPHOR,
		},
		new int[]{320, 320, 320},
		new int[]{384, 384, 448}
	),
	IRONWOOD(
		new int[]{
			ObjectID.SAILING_BOAT_HULL_KANDARIN_1X3_IRONWOOD,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_2X5_IRONWOOD,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_3X8_IRONWOOD,
		},
		new int[]{320, 320, 320},
		new int[]{384, 384, 448}
	),
	ROSEWOOD(
		new int[]{
			ObjectID.SAILING_BOAT_HULL_KANDARIN_1X3_ROSEWOOD,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_2X5_ROSEWOOD,
			ObjectID.SAILING_BOAT_HULL_KANDARIN_3X8_ROSEWOOD,
		},
		new int[]{384, 384, 384},
		new int[]{448, 448, 448}
	),
	;

	private final int[] gameObjectIds;
	private final int[] baseSpeeds;
	private final int[] maxSpeeds;

	public int getBaseSpeed(SizeClass sizeClass)
	{
		return baseSpeeds[sizeClass.ordinal()];
	}

	public int getMaxSpeed(SizeClass sizeClass)
	{
		return maxSpeeds[sizeClass.ordinal()];
	}

	public static HullTier fromGameObjectId(int id)
	{
		for (HullTier tier : values())
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