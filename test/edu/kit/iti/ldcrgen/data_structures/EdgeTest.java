package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.BooleanUtils;
import edu.kit.iti.ldcrgen.util.Pair;

public class EdgeTest
{

	@Test
	public void testEqualsAndHashCode()
	{
		final List<Node> nodes = new ArrayList<Node>();
		final DCRGraph graph = new DCRGraph();
		final Random random = new Random();
		for (int i = 0; i < 10; ++i)
		{
			final Node node = new Node();
			node.setGtCluster(new OrdinaryCluster(graph, random.nextDouble()));
			node.setGtClIndex(random.nextInt());
			node.setPsClIndex(random.nextInt());
			node.setOperationIndex(random.nextInt());
			nodes.add(node);
		}

		final List<Edge> edges = new ArrayList<Edge>();
		for (int i = 0; i < nodes.size(); ++i)
		{
			for (int j = i + 1; j < nodes.size(); j++)
			{
				edges.add(Edge.createEdge(new Pair<Node>(nodes.get(i), nodes.get(j))));
			}
		}

		edges.add(null);
		edges.add(null);

		for (int i = 0; i < edges.size(); ++i)
		{
			// reflexivity
			final Edge fst = edges.get(i);
			if (null != fst)
			{
				Assert.assertEquals(fst, fst);
				Assert.assertFalse(fst.equals(null));

				for (int j = 0; j < edges.size(); ++j)
				{
					final Edge snd = edges.get(j);

					if (null != snd)
					{
						// symmetry
						Assert
							.assertTrue(BooleanUtils.equivalent(fst.equals(snd), snd.equals(fst)));

						//
						Assert.assertTrue(BooleanUtils.implies(fst.equals(snd),
							fst.hashCode() == snd.hashCode()));

						for (int k = 0; k < edges.size(); ++k)
						{
							final Edge thd = edges.get(k);

							// transitivity
							Assert.assertTrue(BooleanUtils.implies(
								fst.equals(snd) && snd.equals(thd),
								fst.equals(thd)));
						}
					}
					else
					{
						Assert.assertFalse(fst.equals(snd));
					}
				}
			}
		}

		for (int i = 0; i < edges.size(); ++i)
		{
			final Edge edge = edges.get(i);
			if (edge != null)
			{
				Assert.assertFalse(edge.equals(new Object()));
				Assert.assertFalse(edge.equals("Hallo Test!"));
			}
		}
	}

	@Test
	public void testEdgeIndex()
	{
		final Pair<Integer> nodeIDs1 = new Pair<Integer>(46325, 46342);
		final long edgeIndex1 = Edge.edgeIndex(nodeIDs1.getFirst(), nodeIDs1.getSecond());
		final Pair<Integer> nodeIDsCalculated1 = Edge.nodeIndices(edgeIndex1);

		Assert.assertEquals(nodeIDs1, nodeIDsCalculated1);

		// cannot be distinguished
		// final Pair<Integer> nodeIDs2 = new Pair<Integer>(0, 1);
		// final long edgeIndex2 = Edge.edgeIndex(nodeIDs2.getFirst(),
		// nodeIDs2.getSecond());
		// final Pair<Integer> nodeIDsCalculated2 =
		// Edge.nodeIndices(edgeIndex2);
		// Assert.assertEquals(nodeIDs2, nodeIDsCalculated2);
	}
}
