package edu.kit.iti.ldcrgen.data_structures.binary_selection_tree;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.BinarySelectionTree;
import edu.kit.iti.ldcrgen.util.Sequences;

public class BinarySelectionTreePerformanceTest
{
	@Ignore
	@Test
	public void testPerformanceOfSelect() throws IOException
	{
		final Random random = new Random();
		final int numRuns = 3;
		final int numQueries = 50000;
		final List<Integer> sizes = Sequences.decadeSequence(7, 1);
		final PrintStream writer = new PrintStream(
				"./test/measurements/binary_search_tree_select.data");

		writer.println("# Number of 'select' queries: " + numQueries);
		writer.println("size" + "\t" + "ms");
		for (final int size : sizes)
		{
			final BinarySelectionTree tree = new BinarySelectionTree();

			for (int i = 0; i < size; ++i)
			{
				tree.insert(new WeightDummy(random.nextDouble() * 1000));
			}

			long startTime = System.currentTimeMillis();
			for (int r = 0; r < numRuns; ++r)
			{
				for (int i = 0; i < numQueries; ++i)
				{
					tree.select();
				}
			}
			long endTime = System.currentTimeMillis();
			writer.println(size + "\t" + (endTime - startTime) / numRuns);

		}
		writer.close();
	}
}
