package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import edu.kit.iti.ldcrgen.util.ListUtils;

public class FisherYatesShuffleTest
{

	@Test
	public void testClear()
	{
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(10);
		for (int i = 0; i < 10; ++i)
		{
			shuffle.select();
		}
		shuffle.clear();

		Assert.assertTrue(shuffle.isEmpty());
		Assert.assertTrue(shuffle.getSelectedElements().isEmpty());
		Assert.assertEquals(0, shuffle.getSelectionCount());

		Assert.assertEquals(10, shuffle.getUnselectedElements().size());
	}

	@Test(
			expected = IllegalArgumentException.class)
	public void testIllegalConstruction()
	{
		new FisherYatesShuffle(-1);
	}

	@Test
	public void testMaximumSize()
	{
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(Long.MAX_VALUE);
		shuffle.select(Long.MAX_VALUE - 1);
		Assert.assertEquals(1, shuffle.getSelectionCount());
	}

	@Test(
			expected = IllegalStateException.class)
	public void testInitialSetup()
	{
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(0);

		shuffle.select();
	}

	@Test(
			expected = IllegalArgumentException.class)
	public void testIllegalSelection()
	{
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(0);
		shuffle.select(10);
	}

	@Test(
			expected = IllegalArgumentException.class)
	public void testIllegalSelectionII()
	{
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(1);
		shuffle.delete(0);
	}

	@Test
	public void testSingleElement()
	{
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(1);

		long element = shuffle.select();

		Assert.assertEquals(1, shuffle.getSelectionCount());
		Assert.assertEquals(1, shuffle.getSelectedElements().size());
		Assert.assertTrue(shuffle.getSelectedElements().contains(element));
		Assert.assertFalse(shuffle.isEmpty());

		Assert.assertEquals(0, shuffle.getUnselectedElements().size());
		Assert.assertTrue(shuffle.isFull());

		long deletedElement = shuffle.delete();

		Assert.assertEquals(deletedElement, element);
		Assert.assertEquals(0, shuffle.getSelectionCount());
		Assert.assertTrue(shuffle.getSelectedElements().isEmpty());
		Assert.assertEquals(1, shuffle.getUnselectedElements().size());
	}

	@Test(
			expected = IllegalStateException.class)
	public void testOverflow()
	{
		final int maxNum = 10;
		FisherYatesShuffle shuffle = new FisherYatesShuffle(maxNum);

		for (int i = 0; i < maxNum; ++i)
		{
			shuffle.select();
		}
		shuffle.select();
	}

	/**
	 * White box test for the different cases which can occur.
	 */
	@Test
	public void testCasesForSelection()
	{
		int maxCount = 20;
		FisherYatesShuffle shuffle = new FisherYatesShuffle(maxCount);
		List<Long> expectedContents = new ArrayList<Long>();

		// Special case 1: i = j
		shuffle.select(0);
		expectedContents.add(0L);

		Assert.assertEquals(expectedContents.size(), shuffle.getSelectionCount());
		Assert.assertEquals(expectedContents.size(), shuffle.getSelectedElements().size());
		Assert.assertTrue(shuffle.getSelectedElements().containsAll(expectedContents));

		// Special case 2: i = j and replace(i) != null
		shuffle.select(2); // state afterwards: 0 2 | 1 3...
		shuffle.select(1); // state afterwards: 0 1 2 | 3...
		expectedContents.add(1L);
		expectedContents.add(2L);

		Assert.assertEquals(expectedContents.size(), shuffle.getSelectionCount());
		Assert.assertEquals(expectedContents.size(), shuffle.getSelectedElements().size());
		Assert.assertTrue(shuffle.getSelectedElements().containsAll(expectedContents));
		Assert.assertTrue(ListUtils.isSortedAscending(shuffle.getSelectedElements()));

		// Case 1: replace(i) = replace(j) = null
		shuffle.select(7); // state afterwards: 0 1 2 7 | 4 5 6 3 ...
		shuffle.select(8); // state afterwards: 0 1 2 7 8 | 5 6 3 4...
		shuffle.select(9); // state afterwards: 0 1 2 7 8 9 | 6 3 4 5...
		shuffle.select(10); // state afterwards: 0 1 2 7 8 9 10|3 4 5 6...
		expectedContents.add(7L);
		expectedContents.add(8L);
		expectedContents.add(9L);
		expectedContents.add(10L);
		Assert.assertTrue(shuffle.getSelectedElements().containsAll(expectedContents));
		Assert.assertEquals(expectedContents.size(), shuffle.getSelectedElements().size());

		// Case 4: replace(i) != null AND replace(j) != null
		shuffle.select(4); // state afterwards: 0 1 2 8 4 9 10 7|3 5 6...
		shuffle.select(5); // state afterwards: 0 1 2 9 4 5 10 7 8|3 6...
		shuffle.select(6); // state afterwards: 0 1 2 10 4 5 6 7 8 9|3...
		expectedContents.add(4L);
		expectedContents.add(5L);
		expectedContents.add(6L);
		Assert.assertTrue(shuffle.getSelectedElements().containsAll(expectedContents));
		Assert.assertEquals(expectedContents.size(), shuffle.getSelectedElements().size());

		while (!expectedContents.isEmpty())
		{
			final long last = expectedContents.remove(expectedContents.size() - 1);
			shuffle.delete(last);
			Assert.assertTrue(shuffle.getSelectedElements().containsAll(expectedContents));
			Assert.assertEquals(expectedContents.size(), shuffle.getSelectedElements().size());
		}

		Assert.assertTrue(shuffle.isEmpty());
		Assert.assertTrue(ListUtils.isSortedAscending(shuffle.getSelectedElements()));
		Assert.assertTrue(ListUtils.isSortedAscending(shuffle.getUnselectedElements()));
	}

	@Test
	public void testResize()
	{
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(100);
		shuffle.select(10);
		shuffle.select(1);
		shuffle.select(0);
		shuffle.select(13);
		shuffle.select(shuffle.getMaxNum() - 1);
		Assert.assertEquals(5, shuffle.getSelectionCount());
		shuffle.resize(shuffle.getMaxNum());
		Assert.assertEquals(5, shuffle.getSelectionCount());
		shuffle.resize(shuffle.getMaxNum() - 1);
		Assert.assertEquals(4, shuffle.getSelectionCount());
		shuffle.resize(shuffle.getMaxNum() + 1);
		Assert.assertEquals(4, shuffle.getSelectionCount());

		shuffle.clear();
		Assert.assertEquals(0, shuffle.getSelectionCount());
		Assert.assertEquals(100, shuffle.getMaxNum());

		this.fill(shuffle);

		// keep the size of the shuffle
		shuffle.resize(shuffle.getMaxNum());

		final long origMaxNum = shuffle.getMaxNum();
		for (long i = 0; i < shuffle.getMaxNum(); ++i)
		{
			final long newSize = origMaxNum - i - 1;
			shuffle.resize(newSize);
			Assert.assertEquals(newSize, shuffle.getMaxNum());
			Assert.assertEquals(newSize, shuffle.getSelectionCount());
		}
	}

	@Test
	public void testLargeRandom()
	{
		final int numTestOperations = (int) 1e3;
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(numTestOperations);
		final Random random = new Random();
		final List<Long> expectedElements = new LinkedList<Long>();

		for (int i = 0; i < numTestOperations; ++i)
		{
			final long weightForDelete = shuffle.getSelectionCount();
			final int rand = random.nextInt(numTestOperations);
			if (rand < weightForDelete) // Do delete operation!
			{
				final long deletedElement = shuffle.delete();
				expectedElements.remove(new Long(deletedElement));
				Assert.assertEquals(expectedElements.size(), shuffle.getSelectionCount());
			}
			else
			// Do insert operation!
			{
				final long selectedElement = shuffle.select();
				expectedElements.add(selectedElement);
				Assert.assertEquals(expectedElements.size(), shuffle.getSelectionCount());
			}

		}
	}

	@Test
	public void testRandomOperationsAfterProposition()
	{
		final int numTestOperations = (int) 1e3;
		final FisherYatesShuffle shuffle = new FisherYatesShuffle(numTestOperations);
		final Random random = new Random();
		final List<Long> expectedElements = new LinkedList<Long>();

		for (int i = 0; i < numTestOperations; ++i)
		{
			final long weightForDelete = shuffle.getSelectionCount();
			final int rand = random.nextInt(numTestOperations);
			if (rand < weightForDelete)
			// Do delete operation!
			{
				final long deletedElement = shuffle.proposeForDeletion();
				shuffle.delete(deletedElement);
				expectedElements.remove(new Long(deletedElement));
				Assert.assertEquals(expectedElements.size(), shuffle.getSelectionCount());
			}
			else
			// Do insert operation!
			{
				final long selectedElement = shuffle.proposeForSelection();
				shuffle.select(selectedElement);
				expectedElements.add(selectedElement);
				Assert.assertEquals(expectedElements.size(), shuffle.getSelectionCount());
			}

		}
	}

	private void fill(final FisherYatesShuffle shuffle)
	{
		for (long i = 0; i < shuffle.getMaxNum(); ++i)
		{
			if (shuffle.contains(i))
			{
				shuffle.delete(i);
			}

			shuffle.select(i);
		}
		Assert.assertTrue(shuffle.isFull());
	}


}
