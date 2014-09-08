package edu.kit.iti.ldcrgen.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class Sequences
{
	private Sequences()
	{
		// utility class -> private constructor
	}

	/**
	 * Returns a sequence the elements of which divide each decade from
	 * 10^1 to 10^maxPow into the given number of steps.
	 *
	 * The border of each step is rounded down so that maxPow=1,steps=20
	 * would include
	 * a 0 which corresponds to 10^1/20=0.5.
	 * For example, the parameters maxPow=2 and steps=4 would yield the
	 * sequence
	 * (2, 5, 7, 10, 25, 50, 75, 100).
	 *
	 * For maxPow < 1 or steps < 1 the list will be empty.
	 *
	 * The list will never contain duplicates!
	 *
	 * @param maxPow
	 *            the maximum value in the sequence. This value will always
	 *            appear at the
	 *            end of the sequence
	 * @param stepsPerDecade
	 *            the stepping within one decade.
	 * @return the list as described above with each element being unique
	 */
	public static List<Integer> decadeSequence(final int maxPow, final int stepsPerDecade)
	{
		final Set<Integer> tmpSet = new HashSet<Integer>(maxPow * stepsPerDecade);
		if (stepsPerDecade >= 1)
		{
			int currentPower = 10;
			for (int d = 0; d < maxPow; ++d)
			{
				for (int s = 1; s <= stepsPerDecade; ++s)
				{
					tmpSet.add(currentPower * s / stepsPerDecade);
				}

				currentPower *= 10;
			}
		}
		final List<Integer> result = new ArrayList<Integer>(tmpSet);
		Collections.sort(result);
		return result;
	}

	public static List<Integer> sequence(final int first, final int last, final int step)
	{
		final List<Integer> result = new ArrayList<Integer>(Math.abs(first - last));

		final int max = Math.max(first, last);
		final int min = Math.min(first, last);

		if (step > 0)
		{
			for (int i = min; i < max; i += step)
			{
				result.add(i);
			}
		}
		else
		{
			for (int i = max; i > min; i += step)
			{
				result.add(i);
			}
		}
		return result;
	}

	public static List<Long> binomialSequence(final double prob, final long max)
	{
		final List<Long> result = new ArrayList<Long>();
		if (prob > 0 && prob < 1.01 && max >= 0)
		{
			final Random random = new Random();
			final double logQ = Math.log(1 - prob);
			long current = -1;

			do
			{
				final double rnd = random.nextDouble();
				current += 1 + Math.floor(Math.log(1 - rnd) / logQ);
				if (current < max)
				{
					result.add(current);
				}
			}
			while (current < max);
		}
		return result;
	}
}
