package edu.kit.iti.ldcrgen.data_structures;

import java.util.Random;

import edu.kit.iti.ldcrgen.util.Pair;

public class JavaUtilRandomProvider implements RandomProvider
{
	private Random randomGenerator;

	public JavaUtilRandomProvider()
	{
		this(System.currentTimeMillis());
	}

	public JavaUtilRandomProvider(final long seed)
	{
		this.randomGenerator = new Random(seed);
	}

	@Override
	public void setSeed(final long seed)
	{
		this.randomGenerator.setSeed(seed);
	}

	@Override
	public int nextInt()
	{
		return this.randomGenerator.nextInt();
	}

	@Override
	public double nextDouble()
	{
		return this.randomGenerator.nextDouble();
	}

	@Override
	public double nextGaussian()
	{
		return this.randomGenerator.nextGaussian();
	}

	@Override
	public int nextInt(final int maxValue)
	{
		return this.randomGenerator.nextInt(maxValue);
	}

	@Override
	public long nextLong()
	{
		return this.randomGenerator.nextLong();
	}

	/**
	 * Returns a pair of integers in the range of 0(inclusive) to
	 * max(exclusive)
	 * using the given Random object.
	 *
	 * @param random
	 *            the random number generator
	 * @param max
	 *            the exclusive maximum of the returned values. Must be
	 *            &gt; 1.
	 * @return a pair of distinct integers
	 *
	 * @throws IllegalArgumentException
	 *             if maxValue is smaller than 2
	 */
	@Override
	public Pair<Integer> nextUnequalInts(final int maxValue)
	{
		if (maxValue < 2)
		{
			throw new IllegalArgumentException("Given maximum Value is too small: " + maxValue);
		}

		final int first = this.nextInt(maxValue);
		int second;
		do
		{
			second = this.nextInt(maxValue);
		}
		while (first == second);

		final Pair<Integer> result = new Pair<Integer>(first, second);
		return result;
	}
}
