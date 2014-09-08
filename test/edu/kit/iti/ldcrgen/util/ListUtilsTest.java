package edu.kit.iti.ldcrgen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ListUtilsTest
{
	private static final double EPS = 1e-7;

	@Test
	public void testAllEquals()
	{
		final List<Object> emptyList = new LinkedList<Object>();
		Assert.assertTrue(emptyList.isEmpty());
		Assert.assertTrue(ListUtils.allEqual(emptyList));
		Assert.assertTrue(ListUtils.allEqual(emptyList, 5));

		final List<String> oneElementList = Arrays.asList("Test String");
		Assert.assertTrue(ListUtils.allEqual(oneElementList));
		Assert.assertTrue(ListUtils.allEqual(oneElementList, oneElementList.get(0)));

		final List<Integer> intList = Arrays.asList(1024, 1024, 1024);
		Assert.assertTrue(ListUtils.allEqual(intList, 1024));
		Assert.assertTrue(ListUtils.allEqual(intList, intList.get(2)));

		final List<Float> longList = new ArrayList<Float>((int) 1e3);
		Collections.fill(longList, new Float(0.231e-15));
		Assert.assertTrue(ListUtils.allEqual(longList));
		Assert.assertTrue(ListUtils.allEqual(longList, new Float(0.23e-15)));

		Assert.assertFalse(ListUtils.allEqual(Arrays.asList(1.0), 2.0));
		Assert.assertFalse(ListUtils.allEqual(Arrays.asList(2.0,1.0), 2.0));
		Assert.assertFalse(ListUtils.allEqual(Arrays.asList(2.0,2.0,1.0), 2.0));
	}

	@Test
	public void testIsSortedAsc()
	{
		final List<Integer> emptyList = new ArrayList<Integer>();
		Assert.assertTrue(ListUtils.isSortedAscending(emptyList));
		Assert.assertTrue(ListUtils.isSortedDescending(emptyList));

		Assert.assertTrue(ListUtils.isSortedAscending(Arrays.asList(0)));
		Assert.assertTrue(ListUtils.isSortedDescending(Arrays.asList(0)));
		Assert.assertTrue(ListUtils.isSortedAscending(Arrays.asList(0, 0, 0, 0, 0, 0, 0)));
		Assert.assertTrue(ListUtils.isSortedDescending(Arrays.asList(0, 0, 0, 0, 0, 0, 0)));

		Assert.assertFalse(ListUtils.isSortedAscending(Arrays.asList(2, 1)));
		Assert.assertTrue(ListUtils.isSortedDescending(Arrays.asList(2, 1)));

		Assert.assertFalse(ListUtils.isSortedAscending(Arrays.asList(1023, 231, 102, 57, 17)));
		Assert.assertTrue(ListUtils.isSortedDescending(Arrays.asList(1023, 231, 102, 57, 17)));
	}

	@Test
	public void testInclusivePrefixSum()
	{
		final List<Integer> emptyList = new ArrayList<Integer>();
		final List<Integer> emptyPrefixSum = ListUtils.inclusivePrefixSum(emptyList);
		Assert.assertEquals(emptyList, emptyPrefixSum);

		final List<Integer> oneElementList = Arrays.asList(1);
		final List<Integer> oneElementPrefixSum = ListUtils.inclusivePrefixSum(oneElementList);
		Assert.assertEquals(oneElementList, oneElementPrefixSum);

		final List<Integer> list1 = Arrays.asList(9, 2, 7);
		final List<Integer> prefixSum1 = ListUtils.inclusivePrefixSum(list1);
		Assert.assertEquals(Arrays.asList(9, 11, 18), prefixSum1);
	}

	@Test
	public void testArithmeticMeanAndVariance()
	{
		Assert.assertEquals(12.5, ListUtils.arithmeticMean(Arrays.asList(12.5)), 1e-7);

		Assert.assertEquals(1.0,
				ListUtils.arithmeticMean(Collections.nCopies(100, new Double(1.0))), 1e-7);
		Assert.assertEquals(0.0, ListUtils.variance(Collections.nCopies(100, new Double(1.0))),
				1e-7);

		final List<Double> list1 = Arrays.asList(-1.5, -0.5, 0.0, 1.0, 3.5);
		Assert.assertEquals(2.5 / 5.0, ListUtils.arithmeticMean(list1), 1e-7);

		// [(-2)^2 + (-1)^2 + (-0.5)^2 + (0.5)^2 + (3.0)^2 ] / 5 = 14.5/5 =
		// 2.9
		Assert.assertEquals(14.5 / 5, ListUtils.variance(list1), 1e-7);

	}

	@Test(expected=UnsupportedOperationException.class)
	public void testMapType()
	{
		final List<Double> list1 = Arrays.asList(-1.5, -0.5, 0.0, 1.0, 3.5,
				(double) Byte.MAX_VALUE, (double) Short.MAX_VALUE, (double) Integer.MAX_VALUE,
				(double) Long.MAX_VALUE, (double)Float.MAX_VALUE, Double.MAX_VALUE);
		ListUtils.mapType(list1, Double.class);

		ListUtils.mapType(new ArrayList<Double>(), Integer.class);

		ListUtils.mapType(list1, Float.class);
	}

	@Test
	public void testSplit()
	{
		final Pair<List<Integer>> split1 = ListUtils.split(new ArrayList<Integer>(), 0);
		Assert.assertTrue(split1.getFirst().isEmpty());
		Assert.assertTrue(split1.getSecond().isEmpty());

		final Pair<List<Integer>> split2 = ListUtils.split(Arrays.asList(1), 1);
		Assert.assertEquals(Arrays.asList(1),split2.getFirst());
		Assert.assertTrue(split2.getSecond().isEmpty());

		final Pair<List<Integer>> split3 = ListUtils.split(Arrays.asList(1), 0);
		Assert.assertTrue(split3.getFirst().isEmpty());
		Assert.assertEquals(Arrays.asList(1),split3.getSecond());

		final Pair<List<Integer>> split4 = ListUtils.split(Arrays.asList(1,2,3,4,5,6,7), 3);
		Assert.assertEquals(Arrays.asList(1,2,3), split4.getFirst());
		Assert.assertEquals(Arrays.asList(4,5,6,7),split4.getSecond());
	}

	@Test
	public void sumUp()
	{
		Assert.assertEquals(0, ListUtils.sumUp(new ArrayList<Double>()), EPS);
		Assert.assertEquals(1, ListUtils.sumUp(Arrays.asList(1.0)), EPS);
		Assert.assertEquals(0, ListUtils.sumUp(Arrays.asList(-1.0,0.0,1.0)), EPS);
	}
}
