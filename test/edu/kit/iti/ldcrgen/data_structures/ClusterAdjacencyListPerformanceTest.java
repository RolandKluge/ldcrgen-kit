package edu.kit.iti.ldcrgen.data_structures;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import edu.kit.iti.ldcrgen.util.Pair;
import edu.kit.iti.ldcrgen.util.Sequences;

public class ClusterAdjacencyListPerformanceTest
{

	@Test(expected=OutOfMemoryError.class)
	public void testMaximumSize() throws FileNotFoundException
	{
		final OrdinaryCluster cl = new OrdinaryCluster(null, 0.5);
		cl.setGroundTruthIndex(0);
		final int maxSize = (int) 1e5;
		final int stepsPerDecade = 10;
		final int numRuns = 1;
		final List<Integer> sizes = Sequences.decadeSequence((int) Math.log10(maxSize),
				stepsPerDecade);
		final PrintStream writer = new PrintStream("./test/measurements/adj_list_max_size.data");

		writer.println("# Complete graphs have been created for each size");
		writer.println("# First column: size of adjacency list (node count)");
		writer.println("# Second column: time in milliseconds");
		writer.println("size" + " " + "ms");
		for (final int size : sizes)
		{
			long startTime = 0;
			long endTime = 0;
			for (int r = 0; r < numRuns; ++r)
			{
				startTime += System.currentTimeMillis();
				final ClusterAdjacencyList list = new ClusterAdjacencyList(cl);
				for (int i = 0; i < size; ++i)
				{
					list.addNode(new Node());
				}

				for (int i = 0; i < size; ++i)
				{
					for (int j = i + 1; j < size; ++j)
					{
						final Pair<Node> nodePair = new Pair<Node>(list.getNode(i), list.getNode(j));
						final Edge edge = Edge.createEdge(nodePair);
						list.addEdge(edge);
					}
				}
				endTime += System.currentTimeMillis();
			}
			writer.println(size + " " + (endTime - startTime) / (double) numRuns);
		}
	}

	@Test
	public void testPerformanceEdgeCount() throws FileNotFoundException
	{
		final OrdinaryCluster cl = new OrdinaryCluster(null, 0.5);
		cl.setGroundTruthIndex(0);
		final Random random = new Random();
		final int numRuns = 1;
		final int numQueries = 1000000;
		final int maxSize = (int) 1e4;
		final int stepsPerDecade = 5;
		final List<Integer> sizes = Sequences.decadeSequence((int) Math.log10(maxSize),
				stepsPerDecade);
		final PrintStream writer = new PrintStream(
				"./test/measurements/adj_list_count_queries.data");

		writer.println("# Runs a certain amount of count queries upon a fixed-sized list");
		writer.println("# First column: size of adjacency list (edge count)");
		writer.println("# Second column: time in milliseconds");
		writer.println("# Number of queries: " + numQueries);
		writer.println("size" + " " + "ms");
		for (final int size : sizes)
		{
			long edgeCount = 0;
			long startTime = 0;
			long endTime = 0;
			for (int r = 0; r < numRuns; ++r)
			{
				final ClusterAdjacencyList list = new ClusterAdjacencyList(cl);
				for (int i = 0; i < size; ++i)
				{
					list.addNode(new Node());
				}
				for (int i = 0; i < size; ++i)
				{
					final int first = random.nextInt(size);
					final int second = random.nextInt(size);
					final Pair<Node> pair = new Pair<Node>(list.getNode(first), list.getNode(second));

					if (list.isUnconnected(pair))
					{
						list.addEdge(Edge.createEdge(pair));
					}

				}
				startTime += System.currentTimeMillis();

				edgeCount += list.getEdgeCount();
				for (int i = 0; i < numQueries; ++i)
				{
					list.getEdgeCount();
				}
				endTime += System.currentTimeMillis();
			}

			writer.println(edgeCount / numRuns + " " + (endTime - startTime) / (double) numRuns
					/ numQueries);

		}
		writer.close();
	}
}
