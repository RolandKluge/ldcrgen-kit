package edu.kit.iti.ldcrgen.util;

/**
 * Provides useful utility methods for mathemtics.
 *
 * @author Roland Kluge
 *
 */
public final class MathUtils
{
	private MathUtils()
	{ /* intentionally left empty */
	}

	/**
	 * Returns the given value rounded to the given precision.
	 *
	 * @param val the value to be rounded
	 * @param places the number of places to round to
	 * @return the rounded value
	 */
	public static double roundToDecimalPlaces(final double val, final int places)
	{
		final long power = (long) Math.pow(10, places);
		return ((long)(val * power + 0.5))/(double)power;
	}
}
