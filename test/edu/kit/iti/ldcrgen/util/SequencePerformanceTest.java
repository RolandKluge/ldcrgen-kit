package edu.kit.iti.ldcrgen.util;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.junit.Ignore;
import org.junit.Test;

public class SequencePerformanceTest
{
	@Ignore
	@Test
	public void testBinomialSequence() throws FileNotFoundException
	{
		final PrintStream writer = new PrintStream(
				"./test/measurements/binomial_sequence.data");
		final int numRuns = 5;

		final double[] testCases =
		{
				1e9, 5e-3, // expected count: 5e6
				5e8, 1e-2, //
				1e8, 5e-2, //
				5e7, 1e-1, //
				1e7, 0.5, //
				5e6, 1.0, //
				1e9, 1e-2, // expected count: 1e7
				5e8, 2e-2, //
				1e8, 1e-1, //
				5e7, 2e-1, //
				2.5e7, 0.4, //
				2e7, 0.5, //
				1e7, 1.0, //
				1e9, 2.5e-2, // expected count: 2.5e7
				5e8, 5e-2, //
				1e8, 2.5e-1, //
				5e7, 0.5, //
				2.5e7, 1, //
//				1e9, 5e-2, // expected count: 5e7
//				5e8, 1e-1, //
		};

		writer.println("# Verification that generating binomial sequences\n"
				+ "# with expected 'size = prob * maxSize'\n"
				+ "# elements takes only O(size) time");
		writer.println("size ms prob maxSize");
		for (int test = 0; test < testCases.length / 2; ++test)
		{
			final long startTime = System.currentTimeMillis();

			int size = (int) testCases[2 * test];
			double prob = testCases[2 * test + 1];

			for (int i = 0; i < numRuns; ++i)
			{
				Sequences.binomialSequence(prob, size);
			}
			final long endTime = System.currentTimeMillis();
			writer.println(//
			size * prob + " " + //
					(endTime - startTime) / (double) numRuns + " " + //
					prob + " " + //
					(double) size);
		}
		writer.close();
	}
}
