package edu.kit.iti.ldcrgen.util;

/**
 * Provides different logical operators which are not predefined in Java.
 *
 * @author Roland Kluge
 */
public final class BooleanUtils
{
	private BooleanUtils()
	{
		// utilitiy class -> hidden constructor
	}

	/**
	 * Returns the logic XOR of its arguments.
	 *
	 * @param a
	 *            first value
	 * @param b
	 *            second value
	 * @return a XOR b
	 */
	public static boolean xor(final boolean a, final boolean b)
	{
		return (a || b) && !(a && b);
	}

	/**
	 * Returns the logic implication of its arguments.
	 *
	 * @param a
	 *            first value
	 * @param b
	 *            second value
	 * @return a implies b,i.e., (NOT a) OR b
	 */
	public static boolean implies(final boolean a, final boolean b)
	{
		return (!a) || b;
	}

	/**
	 * Returns the logic equivalence of its arguments.
	 *
	 * @param a
	 *            first value
	 * @param b
	 *            second value
	 * @return a EQUIV b,i.e., (a AND b) OR (NOT a AND NOT b)
	 */
	public static boolean equivalent(final boolean a, final boolean b)
	{
		return a == b;
	}
}
