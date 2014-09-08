package edu.kit.iti.ldcrgen.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link MathUtils}.
 *
 * @author Roland Kluge
 *
 */
public class MathUtilsTest
{
	private static final double EPS = 1e-7;

	@Test
	public void testRound()
	{
		Assert.assertEquals(1.0, MathUtils.roundToDecimalPlaces(1.222, 0), 0.0);
		Assert.assertEquals(1.2, MathUtils.roundToDecimalPlaces(1.222, 1), EPS);
		Assert.assertEquals(1.22, MathUtils.roundToDecimalPlaces(1.222, 2), EPS);
		Assert.assertEquals(1.222, MathUtils.roundToDecimalPlaces(1.222, 3), EPS);
		Assert.assertEquals(1.222, MathUtils.roundToDecimalPlaces(1.222, 4), EPS);
	}
}
