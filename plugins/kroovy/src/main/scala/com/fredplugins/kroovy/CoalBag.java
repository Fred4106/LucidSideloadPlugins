package com.fredplugins.kroovy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoalBag
{
	private static final int UNKNOWN_AMOUNT = 0;
	private static final int EMPTY_AMOUNT = 0;

	private static final Pattern BAG_EMPTY_MESSAGE = Pattern.compile("^The coal bag is (?:now\\s)?empty\\.");
	private static final Pattern BAG_ONE_OR_MANY_MESSAGE = Pattern.compile("^The coal bag (?:still\\s)?contains ([\\d]+|one) pieces? of coal\\.");

	private static int storedAmount;

	private static void setAmount(int amount)
	{
		storedAmount = amount;
	}

	private static void setEmptyAmount()
	{
		storedAmount = EMPTY_AMOUNT;
	}

	public static void setUnknownAmount()
	{
		storedAmount = UNKNOWN_AMOUNT;
	}

	public static int getAmount()
	{
		return storedAmount;
	}

	public static void updateAmount(String message)
	{
		final Matcher emptyMatcher = BAG_EMPTY_MESSAGE.matcher(message);
		if (emptyMatcher.matches())
		{
			setEmptyAmount();
		}
		else
		{
			final Matcher oneOrManyMatcher = BAG_ONE_OR_MANY_MESSAGE.matcher(message);
			if (oneOrManyMatcher.matches())
			{
				final String match = oneOrManyMatcher.group(1);
				if (match.equals("one"))
				{
					setAmount(1);
				} else {
					setAmount(Integer.parseInt(match));
				}
			}
		}
	}

	public static boolean isUnknown()
	{
		return storedAmount == UNKNOWN_AMOUNT;
	}

	public static boolean isEmpty()
	{
		return storedAmount == EMPTY_AMOUNT;
	}
}