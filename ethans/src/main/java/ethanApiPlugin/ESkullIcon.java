package ethanApiPlugin;

import net.runelite.api.Prayer;

public enum ESkullIcon {

	/**
	 * White skull from PVP world or wilderness
	 */
	SKULL(0),
	/**
	 * Red skull from Tzhaar Fight Pits
	 */
	SKULL_FIGHT_PIT(1),
	/**
	 * White skull used on High Risk worlds
	 */
	SKULL_HIGH_RISK(2),
	/**
	 * Forinthry surge buff in the Revenant caves
	 */
	FORINTHRY_SURGE(3),
	/**
	 * Grey skull used on Deadman worlds
	 */
	SKULL_DEADMAN(7),
	/**
	 * PK skull carrying one loot key
	 */
	LOOT_KEYS_ONE(8),
	/**
	 * PK skull carrying two loot keys
	 */
	LOOT_KEYS_TWO(9),
	/**
	 * PK skull carrying two loot keys
	 */
	LOOT_KEYS_THREE(10),
	/**
	 * PK skull carrying two loot keys
	 */
	LOOT_KEYS_FOUR(11),
	/**
	 * PK skull carrying two loot keys
	 */
	LOOT_KEYS_FIVE(12),
	/**
	 * Forinthry surge skull used on Deadman worlds
	 */
	FORINTHRY_SURGE_DEADMAN(14),
	/**
	 * Forinthry surge while carrying one loot key
	 */
	FORINTHRY_SURGE_KEYS_ONE(15),
	/**
	 * Forinthry surge while carrying two loot keys
	 */
	FORINTHRY_SURGE_KEYS_TWO(16),
	/**
	 * Forinthry surge while carrying three loot keys
	 */
	FORINTHRY_SURGE_KEYS_THREE(17),
	/**
	 * Forinthry surge while carrying four loot keys
	 */
	FORINTHRY_SURGE_KEYS_FOUR(18),
	/**
	 * Forinthry surge while carrying five loot keys
	 */
	FORINTHRY_SURGE_KEYS_FIVE(19),

	/**
	 * The player does not have a skull.
	 */
	NONE(-1);

	public final int skullId;
	ESkullIcon(int skullId)
	{
		this.skullId = skullId;
	}

	public static ESkullIcon fromId(int id)
	{
		for (final ESkullIcon type : ESkullIcon.values())
		{
			if (type.skullId == id)
			{
				return type;
			}
		}

		return null;
	}
}
