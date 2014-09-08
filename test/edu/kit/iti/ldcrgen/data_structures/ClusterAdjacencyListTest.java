package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.BooleanUtils;
import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Pair;

/**
 * Test cases for {@link ClusterAdjacencyList}.
 *
 * @author roland
 */
public class ClusterAdjacencyListTest
{

	@Test
	public void testInitialization()
	{
		final OrdinaryCluster cl = new OrdinaryCluster(DCRGraphTest.emptyGraph() , 0.5);
		cl.setGroundTruthIndex(0);
		new ClusterAdjacencyList(cl);
	}

	@Test
	public void testOnlyNodes()
	{
		final OrdinaryCluster cl = new OrdinaryCluster(DCRGraphTest.emptyGraph(), 0.5);
		cl.setGroundTruthIndex(0);

		Assert.assertTrue(cl.isInGroundTruth());
		Assert.assertFalse(cl.isInReferenceClustering());
		Assert.assertTrue(BooleanUtils.implies(cl.isInReferenceClustering(), false));

		final ClusterAdjacencyList list = new ClusterAdjacencyList(cl);
		Assert.assertSame(cl, list.getParent());

		final List<Node> expectedNodes = new ArrayList<Node>(100);
		for (int i = 0; i < 100; ++i)
		{
			final Node node = new Node();
			list.addNode(node);
			expectedNodes.add(node);
		}

		Assert.assertEquals(expectedNodes.size(), list.getNodeCount());

		for (int i = 0; i < 100; ++i)
		{
			final Node expectedNode = ListUtils.last(expectedNodes);
			Assert.assertTrue(list.contains(expectedNode));

			list.removeNode(expectedNode);

			Assert.assertFalse(list.contains(expectedNode));
			expectedNodes.remove(expectedNode);

			for (final Node unchanged : expectedNodes)
			{
				Assert.assertTrue(list.contains(unchanged));
			}

			Assert.assertEquals(expectedNodes.size(), list.getNodeCount());
		}

	}

	@Test
	public void testCompleteGraph()
	{
		final DCRGraph graph = new DCRGraph(0.01, 0.3, PInSampler.MEAN);
		final List<Node> nodes = new ArrayList<Node>(100);
		final List<Edge> expectedEdges = new ArrayList<Edge>(100 * 100);
		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.5);
		fstCl.setGroundTruthIndex(0);
		final ClusterAdjacencyList list = new ClusterAdjacencyList(fstCl);
		for (int i = 0; i < 100; ++i)
		{
			final Node node = new Node();
			node.setGtCluster(fstCl);
			list.addNode(node);
			nodes.add(node);
		}

		for (int i = 0; i < 100; ++i)
		{
			for (int j = 0; j < 100; ++j)
			{
				final Pair<Node> nodePair = new Pair<Node>(nodes.get(i), nodes.get(j));
				if (j < i)
				{
					Assert.assertTrue(list.isConnected(nodePair));
				}
				else if (j > i)
				{
					final Edge edge = Edge.createEdge(nodePair);
					Assert.assertTrue(list.isUnconnected(nodePair));
					list.addEdge(edge);
					expectedEdges.add(edge);
					Assert.assertTrue(list.isConnected(nodePair));
					Assert.assertTrue(list.contains(ListUtils.last(expectedEdges)));
				}
			}
		}
	}

	@Test
	public void testClearEdges()
	{
		final DCRGraph graph = new DCRGraph();
		final OrdinaryCluster parent = new OrdinaryCluster(graph, 1.0);
		parent.setGroundTruthIndex(0);
		final ClusterAdjacencyList list = new ClusterAdjacencyList(parent);

		final Node[] nodes = { new Node(), new Node(), new Node(), new Node(), new Node() };
		for (final Node node : nodes)
		{
			list.addNode(node);
		}
		final int[] edges =
		{
			0, 1,//
			1, 2,//
			1, 3,//
			1, 4,//
			2, 3,//
			3, 4,//
		};
		for (int e = 0; e < edges.length - 1; e += 2)
		{
			Assert.assertTrue(edges[e] < nodes.length && edges[e + 1] < nodes.length);
			list.addEdge(Edge.createEdge(new Pair<Node>(nodes[edges[e]], nodes[edges[e + 1]])));
			Assert.assertEquals((e + 2) / 2, list.getEdgeCount());
		}
		list.clearEdges();
		Assert.assertEquals(0, list.getEdgeCount());
		for (int e = 0; e < edges.length - 1; e += 2)
		{
			Assert.assertTrue(edges[e] < nodes.length && edges[e + 1] < nodes.length);
			list.addEdge(Edge.createEdge(new Pair<Node>(nodes[edges[e]], nodes[edges[e + 1]])));
			Assert.assertEquals((e + 2) / 2, list.getEdgeCount());
		}

	}

	@Test(
		expected = UnsupportedOperationException.class)
	public void testNodeIterator()
	{
		final ClusterAdjacencyList list = createRandomList(1000);

		final Random random = new Random();
		final Iterator<Node> iter = list.nodeIterator();
		final int steps = random.nextInt(list.getNodeCount());
		for (int i = 0; i < steps; ++i)
		{
			if (steps < list.getNodeCount() - 1)
				Assert.assertTrue(iter.hasNext());
			else
				Assert.assertFalse(iter.hasNext());
			final Node node = iter.next();
			Assert.assertTrue(list.contains(node));
		}

		iter.remove();
	}

	@Test(
		expected = UnsupportedOperationException.class)
	public void testNodeSpecificIterator()
	{
		final ClusterAdjacencyList list = createRandomList(1000);
		final Iterator<Edge> iter = list.edgeIterator(list.getLastNode());
		final Random random = new Random();
		final int degOut = list.getInterClusterAdjacencies(list.getLastNode()).size();
		if (degOut > 0)
		{
			final int steps = random.nextInt(degOut);
			for (int i = 0; i < steps; ++i)
			{
				if (steps < degOut - 1)
					Assert.assertTrue(iter.hasNext());
				else
					Assert.assertFalse(iter.hasNext());
				final Edge edge = iter.next();
				Assert.assertTrue(list.contains(edge));
			}
		}

		iter.remove();
	}

	@Test(
		expected = UnsupportedOperationException.class)
	public void testEdgeIterator()
	{
		final ClusterAdjacencyList list = createRandomList(1000);

		final Random random = new Random();
		final Iterator<Edge> iter = list.edgeIterator();
		final int steps = random.nextInt(list.getEdgeCount());
		for (int i = 0; i < steps; ++i)
		{
			if (steps < list.getEdgeCount() - 1)
				Assert.assertTrue(iter.hasNext());
			else
				Assert.assertFalse(iter.hasNext());
			final Edge edge = iter.next();
			Assert.assertTrue(list.contains(edge));
		}

		iter.remove();
	}

	@Test(
		expected = UnsupportedOperationException.class)
	public void testInterClusterEdgeIterator()
	{
		final ClusterAdjacencyList list = createRandomList(1000);

		final Iterator<Edge> iter = list.interClusterEdgeIterator();
		iter.remove();
	}

	@Test(
		expected = UnsupportedOperationException.class)
	public void testIntraClusterEdgeIterator()
	{
		final ClusterAdjacencyList list = createRandomList(1000);

		final Random random = new Random();
		final Iterator<Edge> iter = list.intraClusterEdgeIterator();
		final int steps = random.nextInt(list.getIntraClusterEdgeCount());
		for (int i = 0; i < steps; ++i)
		{
			if (steps < list.getIntraClusterEdgeCount() - 1)
				Assert.assertTrue(iter.hasNext());
			else
				Assert.assertFalse(iter.hasNext());
			final Edge edge = iter.next();
			Assert.assertTrue(list.contains(edge));
			Assert.assertTrue(edge.isIntraClusterEdge());
		}

		iter.remove();
	}

	private ClusterAdjacencyList createRandomList(final int nodeCount)
	{
		final List<Node> nodes = new ArrayList<Node>(nodeCount);
		final Random random = new Random();
		final DCRGraph graph = new DCRGraph();

		final OrdinaryCluster cluster = new OrdinaryCluster(graph, random.nextDouble());
		cluster.setGroundTruthIndex(0);
		final ClusterAdjacencyList list = new ClusterAdjacencyList(cluster);

		for (int i = 0; i < nodeCount; ++i)
		{
			final Node node = new Node();
			list.addNode(node);
			nodes.add(node);
			node.setGtCluster(cluster);
		}

		// final int numEdges =
		// random.nextInt(Edge.maxEdgeCount(nodes.size()));
		final int numEdges = random.nextInt(Math.min(10 * nodeCount,
			(int) Edge.maxEdgeCount(nodes.size())));

		for (int i = 0; i < numEdges / 2; ++i)
		{
			Node fst;
			Node snd;
			do
			{
				fst = list.getNode(random.nextInt(nodes.size()));
				snd = list.getNode(random.nextInt(nodes.size()));
			}
			while (fst.equals(snd) || list.isConnected(new Pair<Node>(fst, snd)));
			list.addEdge(Edge.createEdge(new Pair<Node>(fst, snd)));
		}
		return list;
	}
}
