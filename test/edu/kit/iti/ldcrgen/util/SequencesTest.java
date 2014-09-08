package edu.kit.iti.ldcrgen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Sequences;

public class SequencesTest
{
	@Test
	public void testSequence()
	{
		final List<Integer> list1 = Sequences.sequence(0, 0, 1);
		Assert.assertTrue(list1.isEmpty());

		final List<Integer> listN1 = Sequences.sequence(0, 0, -1);
		Assert.assertTrue(listN1.isEmpty());

		final List<Integer> list2 = Sequences.sequence(0, 1, 1);
		Assert.assertEquals(1, list2.size());
		Assert.assertTrue(list2.contains(0));

		final List<Integer> listN2 = Sequences.sequence(0, 1, -1);
		Assert.assertEquals(1, listN2.size());
		Assert.assertTrue(listN2.contains(1));

		final List<Integer> list3 = Sequences.sequence(-13, -11, 1);
		Assert.assertEquals(2, list3.size());
		Assert.assertTrue(list3.contains(-13));
		Assert.assertTrue(list3.contains(-12));

		final List<Integer> list4 = Sequences.sequence(-13, -11, 2);
		Assert.assertEquals(1, list4.size());
		Assert.assertTrue(list4.contains(-13));

		final List<Integer> list5 = Sequences.sequence(-1001, 20023, 1);
		Assert.assertTrue(ListUtils.isSortedAscending(list5));

		final List<Integer> list6 = Sequences.sequence(-1001, 20023, -1);
		Assert.assertTrue(ListUtils.isSortedDescending(list6));
	}

	@Test
	public void testDecadeSequence()
	{
		final List<Integer> list1 = Sequences.decadeSequence(2, 2);
		Assert.assertTrue(ListUtils.isSortedAscending(list1));
		final List<Integer> list2 = Sequences.decadeSequence(3, 8);
		Assert.assertTrue(ListUtils.isSortedAscending(list2));
		final List<Integer> list3 = Sequences.decadeSequence(5, 10);
		Assert.assertTrue(ListUtils.isSortedAscending(list3));

		final List<Integer> listInComment = Sequences.decadeSequence(2, 4);
		Assert.assertEquals(listInComment, Arrays.asList(2, 5, 7, 10, 25, 50, 75, 100));

		final List<Integer> listForTenVeryFine = Sequences.decadeSequence(1, 10);
		Assert.assertEquals(10, new HashSet<Integer>(listForTenVeryFine).size());

		final List<Integer> listRoundDown = Sequences.decadeSequence(1, 11);
		Assert.assertEquals(11, new HashSet<Integer>(listRoundDown).size());

		final List<Integer> listForTenOverlyFine = Sequences.decadeSequence(1, 100);
		Assert.assertEquals(11, new HashSet<Integer>(listForTenOverlyFine).size());

		final List<Integer> tooSmallStepCount = Sequences.decadeSequence(10, 0);
		Assert.assertTrue(tooSmallStepCount.isEmpty());

		final List<Integer> tooSmallMaxPow = Sequences.decadeSequence(0, 1);
		Assert.assertTrue(tooSmallMaxPow.isEmpty());
	}

	@Test
	public void testBinomialSequenceEmpty()
	{
		final List<Long> tooSmallList =
			Sequences.binomialSequence(-0.01, 20);
		Assert.assertTrue(tooSmallList.isEmpty());

		final List<Long> tooLargeProbList =
			Sequences.binomialSequence(1.01, 20);
		Assert.assertTrue(tooLargeProbList.isEmpty());

		final List<Long> negativeMaxList =
			Sequences.binomialSequence(0.5, -10);
		Assert.assertTrue(negativeMaxList.isEmpty());
	}

	@Test
	public void testBinomialSequence()
	{
		final int numRuns = (int) 1e5;

		final double[] testCases =
		{
			// 100, 0.5, //
			// 250, 0.1, //
			500, 0.05, //
		// 100, 0.05, //
		};

		for (int test = 0; test < testCases.length / 2; ++test)
		{
			int size = (int) testCases[2 * test];
			double prob = testCases[2 * test + 1];
			final List<Integer> sizes = new ArrayList<Integer>();

			// System.out.println("# --------------------------------------");
			// System.out.println("#" + size + " " + prob);
			for (int i = 0; i < numRuns; ++i)
			{
				final List<Long> list =
						Sequences.binomialSequence(prob, size);
				sizes.add(list.size());
				for (final long elem : list)
				{
					Assert.assertTrue(0 <= elem && elem < size);
				}
			}

			final int[] histogram = new int[size];

			for (final Integer val : sizes)
			{
				++histogram[val];
			}

			// int ctr = 0;
			// for (final int h : histogram)
			// {
			// System.out.println(++ctr + " " + h);
			// }
		}
	}
}
