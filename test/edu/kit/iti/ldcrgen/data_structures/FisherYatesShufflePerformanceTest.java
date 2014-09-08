package edu.kit.iti.ldcrgen.data_structures;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.Sequences;
import edu.kit.iti.ldcrgen.util.Timer;

public class FisherYatesShufflePerformanceTest
{

	@Ignore
	@Test
	public void testPerformanceBuildUpSizes() throws FileNotFoundException
	{
		final int numRuns = 3;
		final int maxSize = (int) 1e7;
		final int stepsPerDecade = 8;
		final List<Integer> sizes = Sequences.decadeSequence((int) Math.log10(maxSize),
				stepsPerDecade);
		final PrintStream writer = new PrintStream(
				"./test/measurements/fy_shuffle_build_up_sizes.data");

		writer.println("# First column: size of shuffle");
		writer.println("# Second column: time in milliseconds");
		writer.println("size" + " " + "ms");
		for (final int size : sizes)
		{
			long startTime = System.currentTimeMillis();
			for (int r = 0; r < numRuns; ++r)
			{
				final FisherYatesShuffle shuffle = new FisherYatesShuffle(2 * size);
				for (int i = 0; i < size; ++i)
				{
					shuffle.select();
				}
			}
			long endTime = System.currentTimeMillis();
			writer.println(size + " " + (endTime - startTime) / (double) numRuns);

		}
		writer.close();
	}

	@Ignore
	@Test
	public void testPerformanceQueries() throws FileNotFoundException
	{
		final Random random = new Random();
		final int numRuns = 3;
		final int maxSize = (int) 1e7;
		final int numQueries = (int) 1e7;
		final int stepsPerDecade = 8;
		final List<Integer> sizes = Sequences.decadeSequence((int) Math.log10(maxSize),
				stepsPerDecade);
		final PrintStream writer = new PrintStream(
				"./test/measurements/fy_shuffle_queries.data");

		writer.println("# First column: size of shuffle");
		writer.println("# Second column: time in milliseconds per query");
		writer.println("# Number of queries: " + (float) numQueries);
		writer.println("size" + " " + "ms");
		for (final int size : sizes)
		{
			long startTime = System.currentTimeMillis();
			for (int r = 0; r < numRuns; ++r)
			{
				final FisherYatesShuffle shuffle = new FisherYatesShuffle(2 * size);
				for (int i = 0; i < numQueries; ++i)
				{
					final double probabilityOfDeletion = shuffle.getSelectionCount()
						/ (double) shuffle.getMaxNum();

					if (random.nextDouble() < probabilityOfDeletion)
					{
						shuffle.delete();
					}
					else
					{
						shuffle.select();
					}
				}
			}
			long endTime = System.currentTimeMillis();
			writer.println(size + " " + (endTime - startTime) / (double) numRuns);

		}
		writer.close();
	}

	@Ignore
	@Test
	public void testPerformanceBuildUpPercentage() throws FileNotFoundException
	{
		final int numRuns = 3;
		int size = (int) 1e7;
		final List<Integer> queries = Sequences.sequence(10, 101, 10);
		final PrintStream writer = new PrintStream(
				"./test/measurements/fy_shuffle_build_up_percentage.data");

		writer.println("# First column: % filled");
		writer.println("# Second column: time in milliseconds");
		writer.println("# Shuffle size: " + (float) size);
		writer.println("filled" + " " + "ms");
		for (final int q : queries)
		{
			long startTime = System.currentTimeMillis();

			for (int r = 0; r < numRuns; ++r)
			{
				final FisherYatesShuffle shuffle = new FisherYatesShuffle(size);
				for (int i = 0; i < q * size / 100; ++i)
				{
					shuffle.select();
				}
			}
			long endTime = System.currentTimeMillis();
			writer.println(q + " " + (endTime - startTime) / (double) numRuns);

		}
	}

	/**
	 * Tests in how far the inefficiency of the
	 * {@link FisherYatesShuffle#resize(long)}
	 * matters.
	 */
	@Ignore
	@Test
	public void testResize()
	{
		final Timer timer = new Timer();
		final int nodeCount = 40000;
		final long maxSize = nodeCount * (nodeCount - 1L) / 2;
		final int numElements = (int) 1e4;
		final int numRuns = 100;
		for (int r = 0; r < numRuns; ++r)
		{
			final FisherYatesShuffle shuffle = new FisherYatesShuffle(maxSize);

			for (int i = 0; i < numElements; ++i)
			{
				shuffle.select();
			}
			Assert.assertEquals(numElements, shuffle.getSelectionCount());

			timer.start();
			shuffle.resize(maxSize - (nodeCount - 1)); //corresponds to removing one node
			timer.stop();
		}

		System.out.println(timer.elapsed()/(double)numRuns + "ms");
	}
}
