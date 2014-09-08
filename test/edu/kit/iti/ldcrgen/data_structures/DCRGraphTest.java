package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.BooleanUtils;
import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Pair;

public class DCRGraphTest
{

	private static final double EPS = 1e-7;

	@Test
	public void testInitialization()
	{
		final double pOut = 0.2321;
		final double theta = 0.11;
		final PInSampler sampler = PInSampler.MEAN;
		final DCRGraph graph = new DCRGraph(pOut, theta, sampler);

		Assert.assertEquals(0, graph.getEdgeCount());
		Assert.assertEquals(0, graph.getNonEdgeCount());
		Assert.assertEquals(0, graph.getInterClusterEdgeCount());
		Assert.assertEquals(0, graph.getIntraClusterEdgeCount());
		Assert.assertEquals(0, graph.getNodeCount());
		Assert.assertEquals(0, graph.getClusterCount());
		Assert.assertEquals(0, graph.getCurrentTime());

		Assert.assertNotNull(graph.getClusteringJournal());
		Assert.assertNotNull(graph.getGraphJournal());

		final GroundTruth gt = graph.getGroundTruth();
		final ReferenceClustering refCl = graph.getReferenceClustering();

		Assert.assertEquals(0, gt.getClusterCount());
		Assert.assertTrue(gt.getPIn().isEmpty());

		Assert.assertEquals(0, refCl.getClusterCount());
		Assert.assertTrue(refCl.getPIn().isEmpty());

		Assert.assertEquals(theta, graph.getClusterOpThreshold(), EPS);
		Assert.assertEquals(pOut, graph.getPOut(), EPS);
		Assert.assertSame(sampler, graph.getSamplerForNewPIn());
	}

	@Test
	public void testTimeSteps()
	{
		final DCRGraph graph = new DCRGraph();
		Assert.assertEquals(0, graph.getCurrentTime());
		graph.nextTimeStep();
		Assert.assertEquals(1, graph.getCurrentTime());
	}

	@Test
	public void testNodeDynamic()
	{
		final DCRGraph graph = new DCRGraph(0.1, 0.5, PInSampler.MEAN);

		final OrdinaryCluster cluster = new OrdinaryCluster(graph, 0.5);
		graph.addCluster(cluster);
		final List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < 10; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, cluster);
		}

		final int[] edges =
		{
			0, 1,//
			0, 2,//
			0, 3,//
			0, 4,//
		};

		for (int i = 0; i < edges.length; i += 2)
		{
			graph.addEdge(new Pair<Node>(nodes.get(edges[i]), nodes.get(edges[i + 1])));
			Assert.assertEquals(i / 2 + 1, cluster.getEdgeCount());
		}

		// only one cluster -> no inter-cluster edges possible!
		Assert.assertEquals(0, graph.getInterClusterNonEdgeCount());
		Assert.assertEquals(0, graph.getInterClusterEdgeCount());

		final Node firstNode = nodes.get(0);

		Assert.assertEquals(4, firstNode.getDegree());
		Assert.assertEquals(4, graph.removeAllEdges(firstNode));
		Assert.assertEquals(0, firstNode.getDegree());

		graph.removeNode(firstNode);
		Assert.assertFalse(graph.contains(firstNode));


	}

	@Test
	public void testNodeDeletionSpecial()
	{
		final DCRGraph graph = new DCRGraph(0.1, 0.8, PInSampler.GAUSSIAN);
		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.4);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.6);
		final OrdinaryCluster thdCl = new OrdinaryCluster(graph, 0.6);
		final OrdinaryCluster fothCl = new OrdinaryCluster(graph, 0.99);
		final OrdinaryCluster ffthCl = new OrdinaryCluster(graph, 1.0);

		graph.addCluster(fstCl);
		graph.addCluster(sndCl);
		graph.addCluster(thdCl);
		graph.addCluster(fothCl);
		graph.addCluster(ffthCl);

		graph.addNode(new Node(), fstCl);
		graph.addNode(new Node(), sndCl);
		graph.addNode(new Node(), thdCl);
		graph.addNode(new Node(), fothCl);
		graph.addNode(new Node(), ffthCl);

		// cannot remove a node because this would leave one cluster empty
		Assert.assertEquals(graph.getClusterCount(), graph.getNodeCount());
		graph.removeNode();
		Assert.assertEquals(graph.getClusterCount(), graph.getNodeCount());

		graph.addNode(new Node(), ffthCl);

		Assert.assertEquals(graph.getClusterCount() + 1, graph.getNodeCount());
		graph.removeNode();
		Assert.assertEquals(graph.getClusterCount(), graph.getNodeCount());

		// cannot remove a node because this would leave one cluster empty
		Assert.assertEquals(graph.getClusterCount(), graph.getNodeCount());
		graph.removeNode();
		Assert.assertEquals(graph.getClusterCount(), graph.getNodeCount());
	}

	@Test
	public void testAddAndConnect()
	{
		final DCRGraph graph = new DCRGraph(1.0, 0.5, PInSampler.MEAN);

		final OrdinaryCluster cluster = new OrdinaryCluster(graph, 1.0);
		graph.addCluster(cluster);

		final List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < 100; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, cluster);
		}

		Assert.assertEquals(0, graph.getEdgeCount());
		graph.addAndConnectNode();

		// due to pin=pout=1 the new node is connected to every other node
		Assert.assertEquals(graph.getNodeCount() - 1, graph.getEdgeCount());

		graph.addAndConnectNode();

		final int expectedEdgeCount = (graph.getNodeCount() - 1) + (graph.getNodeCount() - 2);
		Assert.assertEquals(expectedEdgeCount, graph.getEdgeCount());

	}

	@Test
	public void testDegree()
	{
		final double pOut = 0.01;
		final DCRGraph graph = new DCRGraph(pOut,0.5, PInSampler.MEAN);

		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.3);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.3);
		final OrdinaryCluster thdCl = new OrdinaryCluster(graph, 0.3);
		graph.addCluster(fstCl);
		graph.addCluster(sndCl);
		graph.addCluster(thdCl);

		final List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < 10; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, fstCl);
		}

		for (int i = 10; i < 20; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, sndCl);
		}

		for (int i = 20; i < 30; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, sndCl);
		}

		final int[] edges =
		{
			// edges in first cluster
			0, 1,//
			1, 2,//
			1, 3,//
			2, 3,//
			2, 4,//
			2, 5,//
			3, 11,//
			3, 21,//
			// edges in second cluster
			10, 11,//
			10, 12,//
			10, 13,//
			10, 14,//
			10, 15,//
			10, 16,//
			10, 17,//
			10, 18,//
			10, 19,//
			// edges in third cluster
			20, 29,//
			20, 27,//
			20, 25,//
			20, 23,//
			20, 21,//
		};

		for (int e = 0; e < edges.length - 1; e += 2)
		{
			graph.addEdge(new Pair<Node>(nodes.get(edges[e]), nodes.get(edges[e + 1])));
			Assert.assertEquals((e + 2) / 2, graph.getEdgeCount());
		}

		Assert.assertEquals(30, graph.getNodeCount());
		Assert.assertEquals(edges.length / 2, graph.getEdgeCount());
		Assert.assertEquals(3, graph.getClusterCount());

		// first cluster
		Assert.assertEquals(1, nodes.get(0).getDegreeIntra());

		Assert.assertEquals(2 + 1, nodes.get(1).getDegreeIntra());

		Assert.assertEquals(3 + 1, nodes.get(2).getDegreeIntra());

		// second cluster
		Assert.assertEquals(9, nodes.get(10).getDegreeIntra());

		// third cluster
		Assert.assertEquals(5, nodes.get(20).getDegreeIntra());

		// inter-cluster edges
		Assert.assertEquals(2, nodes.get(3).getDegreeIntra());
		Assert.assertEquals(2, nodes.get(3).getDegreeInter());

	}

	@Test
	public void testIterators()
	{
		final double pOut = 0.01;
		final DCRGraph graph = new DCRGraph(pOut, 0.5, PInSampler.GAUSSIAN);

		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.3);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.3);
		final OrdinaryCluster thdCl = new OrdinaryCluster(graph, 0.3);
		graph.addCluster(fstCl);
		graph.addCluster(sndCl);
		graph.addCluster(thdCl);
		final List<Node> nodes = new ArrayList<Node>();

		for (int i = 0; i < 10; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, fstCl);
		}

		for (int i = 10; i < 20; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, sndCl);
		}

		for (int i = 20; i < 30; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, thdCl);
		}

		Assert.assertEquals(10, fstCl.getNodeCount());
		Assert.assertEquals(10, sndCl.getNodeCount());
		Assert.assertEquals(10, thdCl.getNodeCount());

		Iterator<Edge> intraIter;
		Iterator<Edge> interIter;
		int counter;

		/*
		 * ****************************************************
		 * fully connect fstCl within itself
		 * ****************************************************
		 */
		for (int i = 0; i < 10; ++i)
		{
			for (int j = i + 1; j < 10; ++j)
			{
				graph.addEdge(new Pair<Node>(nodes.get(i), nodes.get(j)));
			}
		}

		intraIter = fstCl.intraClusterEdgeIterator();
		counter = 0;
		while (intraIter.hasNext())
		{
			intraIter.next();
			++counter;
		}
		Assert.assertEquals(2 * Edge.maxEdgeCount(fstCl.getNodeCount()), counter);

		interIter = fstCl.interClusterEdgeIterator();
		counter = 0;
		while (interIter.hasNext())
		{
			interIter.next();
			++counter;
		}
		interIter = fstCl.interClusterEdgeIterator();
		while (interIter.hasNext())
		{
			interIter.next();
			++counter;
		}
		Assert.assertEquals(0, counter / 2);

		/*
		 * ****************************************************
		 * fully connect fstCl to the rest of the graph
		 * ****************************************************
		 */
		for (int i = 0; i < 10; ++i)
		{
			for (int j = 10; j < 30; ++j)
			{
				graph.addEdge(new Pair<Node>(nodes.get(i), nodes.get(j)));
			}
		}

		// test whether repeated execution changes something
		interIter = fstCl.interClusterEdgeIterator();
		counter = 0;
		while (interIter.hasNext())
		{
			interIter.next();
			++counter;
		}
		interIter = fstCl.interClusterEdgeIterator();
		while (interIter.hasNext())
		{
			interIter.next();
			++counter;
		}

		Assert.assertEquals(10 * (10 + 10), counter / 2);
		Assert.assertEquals(10 * (10 + 10), fstCl.getInterClusterEdgeCount());

		interIter = fstCl.interClusterEdgeIterator(sndCl);
		counter = 0;
		while (interIter.hasNext())
		{
			interIter.next();
			++counter;
		}

		Assert.assertEquals(10 * 10, counter);
		Assert.assertEquals(10 * 10, fstCl.getInterClusterEdgeCount(sndCl));
		Assert.assertEquals(10 * 10, sndCl.getInterClusterEdgeCount(fstCl));

		interIter = fstCl.interClusterEdgeIterator(thdCl);
		counter = 0;
		while (interIter.hasNext())
		{
			interIter.next();
			++counter;
		}

		Assert.assertEquals(10 * 10, counter);
		Assert.assertEquals(10 * 10, fstCl.getInterClusterEdgeCount(thdCl));

		/*
		 * ****************************************************
		 * fully connect sndCl within itself
		 * ****************************************************
		 */
		for (int i = 10; i < 20; ++i)
		{
			for (int j = i + 1; j < 20; ++j)
			{
				graph.addEdge(new Pair<Node>(nodes.get(i), nodes.get(j)));
			}
		}

		// fully connect sndCl to the rest of the graph
		for (int i = 10; i < 20; ++i)
		{
			for (int j = 20; j < 30; ++j)
			{
				graph.addEdge(new Pair<Node>(nodes.get(i), nodes.get(j)));
			}
		}

		interIter = sndCl.interClusterEdgeIterator(thdCl);
		counter = 0;
		while (interIter.hasNext())
		{
			interIter.next();
			++counter;
		}

		Assert.assertEquals(10 * 10, counter);
		Assert.assertEquals(10 * 10, sndCl.getInterClusterEdgeCount(thdCl));

		// fully connect thdCl within itself
		for (int i = 20; i < 30; ++i)
		{
			for (int j = i + 1; j < 30; ++j)
			{
				graph.addEdge(new Pair<Node>(nodes.get(i), nodes.get(j)));
			}
		}

		intraIter = thdCl.intraClusterEdgeIterator();
		counter = 0;
		while (intraIter.hasNext())
		{
			intraIter.next();
			++counter;
		}
		Assert.assertEquals(2 * Edge.maxEdgeCount(thdCl.getNodeCount()), counter);
	}

	@Test
	public void testDeterministicGraph()
	{
		final double pOut = 0.1;
		final DCRGraph graph = new DCRGraph(pOut,0.5, PInSampler.MEAN);
		final GroundTruth groundTruth = graph.getGroundTruth();
		final ReferenceClustering refCl = graph.getReferenceClustering();

		final double pIn = 0.5;
		final OrdinaryCluster fstCluster = new OrdinaryCluster(graph, pIn);
		graph.addCluster(fstCluster);
		Assert.assertEquals(fstCluster, refCl.getCluster(0));

		final OrdinaryCluster sndCluster = new OrdinaryCluster(graph, pIn);
		graph.addCluster(sndCluster);
		Assert.assertEquals(sndCluster, refCl.getCluster(1));

		Assert.assertEquals(2, graph.getClusterCount());
		Assert.assertEquals(2, groundTruth.getClusterCount());
		Assert.assertEquals(2, refCl.getClusterCount());

		final List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < 7; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, fstCluster);
		}

		for (int i = 7; i < 20; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, sndCluster);
		}

		Assert.assertEquals(20, graph.getNodeCount());
		Assert.assertEquals(7, fstCluster.getNodeCount());
		Assert.assertEquals(13, sndCluster.getNodeCount());
		Assert.assertEquals(20 * 19 / 2, graph.getNonEdgeCount());
		Assert.assertEquals(7 * 6 / 2, fstCluster.getIntraClusterNonEdgeCount());
		Assert.assertEquals(13 * 12 / 2, sndCluster.getIntraClusterNonEdgeCount());
		Assert.assertEquals(190 - 21 - 78, graph.getInterClusterNonEdgeCount());

		/*
		 * No edges exists, so edge deletion must be impossible
		 */
		Assert.assertTrue(graph.shallDoEdgeInsertion());
		Assert.assertEquals(0.0, fstCluster.getDeletionWeight().getWeight(), EPS);
		Assert.assertEquals(0.0, sndCluster.getDeletionWeight().getWeight(), EPS);
		Assert.assertEquals(0.0, graph.getPseudoCluster().getDeletionWeight().getWeight(), EPS);

		// insert several inter-cluster edges
		graph.addEdge(new Pair<Node>(nodes.get(0), nodes.get(7)));
		Assert.assertEquals(1, graph.getInterClusterEdgeCount());
		Assert.assertEquals(0, graph.getIntraClusterEdgeCount());
		Assert.assertEquals(1, graph.getEdgeCount());

		graph.addEdge(new Pair<Node>(nodes.get(1), nodes.get(8)));
		graph.addEdge(new Pair<Node>(nodes.get(2), nodes.get(9)));
		graph.addEdge(new Pair<Node>(nodes.get(3), nodes.get(10)));
		Assert.assertEquals(4, graph.getInterClusterEdgeCount());
		Assert.assertEquals(0, graph.getIntraClusterEdgeCount());
		Assert.assertEquals(4, graph.getEdgeCount());
		/*
		 * 4 edges exist - all inter-cluster edges
		 */
		final long expPClNonEdgeCount = graph.getNonEdgeCount()
				- fstCluster.getIntraClusterNonEdgeCount()
				- sndCluster.getIntraClusterNonEdgeCount();
		Assert.assertEquals(expPClNonEdgeCount, graph.getInterClusterNonEdgeCount());

		// some intra-cluster edges
		graph.addEdge(new Pair<Node>(nodes.get(0), nodes.get(1)));
		graph.addEdge(new Pair<Node>(nodes.get(1), nodes.get(2)));
		graph.addEdge(new Pair<Node>(nodes.get(2), nodes.get(3)));
		graph.addEdge(new Pair<Node>(nodes.get(3), nodes.get(4)));

		graph.addEdge(new Pair<Node>(nodes.get(7), nodes.get(8)));
		graph.addEdge(new Pair<Node>(nodes.get(8), nodes.get(9)));
		graph.addEdge(new Pair<Node>(nodes.get(10), nodes.get(11)));
		graph.addEdge(new Pair<Node>(nodes.get(11), nodes.get(12)));

		Assert.assertEquals(4, graph.getInterClusterEdgeCount());
		Assert.assertEquals(8, graph.getIntraClusterEdgeCount());
		Assert.assertEquals(4 + 8, graph.getEdgeCount());

	}

	@Test
	public void testEdgeOperationBoundaryCases()
	{
		DCRGraph graph = new DCRGraph(0.1, 0.5, PInSampler.GAUSSIAN);
		graph.addCluster(new OrdinaryCluster(graph, 0.5));
		for (int i = 0; i < 10; ++i)
		{
			graph.addNode();
		}

		Assert.assertEquals(0, graph.getEdgeCount());
		for (int i = 0; i < Edge.maxEdgeCount(10); ++i)
		{
			graph.addEdge();
			Assert.assertEquals(i + 1, graph.getEdgeCount());
		}



		// graph already complete
		Assert.assertFalse(graph.shallDoEdgeInsertion());
		graph.addEdge();
		Assert.assertEquals(Edge.maxEdgeCount(10), graph.getEdgeCount());

		for (int i = 0; i < Edge.maxEdgeCount(10); ++i)
		{
			graph.removeEdge();
			Assert.assertEquals(Edge.maxEdgeCount(10) - i - 1, graph.getEdgeCount());
		}

		// no edges left
		graph.removeEdge();
		Assert.assertEquals(0, graph.getEdgeCount());
	}

	@Test
	public void testForceRemovalOfInterClEdge()
	{
		final DCRGraph graph = new DCRGraph(0.1, 0.8, PInSampler.MEAN);
		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.5);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.8);
		graph.addCluster(fstCl);
		graph.addCluster(sndCl);

		/*
		 * Nodes of first cluster are stored at even positions and
		 * those of the second one at odd positions.
		 */
		final List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < 10; ++i)
		{
			final Node node1 = new Node();
			graph.addNode(node1, fstCl);
			final Node node2 = new Node();
			graph.addNode(node2, sndCl);
			nodes.addAll(Arrays.asList(node1, node2));
		}

		for (int i = 0; i < nodes.size(); i += 2)
		{
			for (int j = 1; j < nodes.size(); j += 2)
			{
				graph.addEdge(new Pair<Node>(nodes.get(i), nodes.get(j)));
			}
		}

		Assert.assertEquals(fstCl.getNodeCount() * sndCl.getNodeCount(),
			graph.getInterClusterEdgeCount());
		Assert.assertEquals(graph.getEdgeCount(), graph.getInterClusterEdgeCount());

		Assert.assertEquals(0, graph.getInterClusterNonEdgeCount());
		Assert.assertEquals(0, graph.getIntraClusterEdgeCount());

		for (int i = 0; i < graph.getInterClusterEdgeCount(); ++i)
		{
			graph.removeEdge();
			Assert.assertEquals(fstCl.getNodeCount() * sndCl.getNodeCount() - i - 1,
				graph.getInterClusterEdgeCount());
		}

		Assert.assertEquals(graph.getEdgeCount(), graph.getInterClusterEdgeCount());

		// no edges left
		graph.removeEdge();
	}

	@Test
	public void testRemoveAllEdges()
	{
		final DCRGraph graph = new DCRGraph(0.1, 0.8, PInSampler.MEAN);
		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.5);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.5);
		final OrdinaryCluster thdCl = new OrdinaryCluster(graph, 0.5);
		graph.addCluster(fstCl);
		graph.addCluster(sndCl);
		graph.addCluster(thdCl);

		final List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < 40; ++i)
		{
			final Node node1 = new Node();
			graph.addNode(node1, fstCl);
			final Node node2 = new Node();
			graph.addNode(node2, sndCl);
			final Node node3 = new Node();
			graph.addNode(node3, thdCl);
			nodes.addAll(Arrays.asList(node1, node2, node3));
		}
		for (int i = 0; i < Edge.maxEdgeCount(graph.getNodeCount()); ++i)
		{
			graph.addEdge();
		}

		final Random random = new Random(1);
		for (int i = 0; i < graph.getNodeCount() / 10; ++i)
		{
			final Node node = nodes.get(random.nextInt(nodes.size()));
			final OrdinaryCluster cluster = node.getGtCluster();

			final int edgesBefore = graph.getEdgeCount();
			final int intraClEdgesBefore = cluster.getEdgeCount();
			final int nodesBefore = graph.getNodeCount();
			final int degreeOutBefore = node.getDegreeInter();
			final int degreeInBefore = node.getDegreeIntra();
			graph.removeAllEdges(node);

			Assert.assertEquals(0, node.getDegree());
			Assert.assertEquals(0, node.getDegreeIntra());
			Assert.assertEquals(0, node.getDegreeInter());

			Assert.assertEquals(nodesBefore, graph.getNodeCount());

			Assert.assertEquals(edgesBefore - degreeInBefore - degreeOutBefore,
				graph.getEdgeCount());
			Assert.assertEquals(intraClEdgesBefore - degreeInBefore, cluster.getEdgeCount());
		}
	}

	@Test
	public void testFindEdge()
	{
		final DCRGraph graph = new DCRGraph(0.005, 0.6, PInSampler.MEAN);
		final OrdinaryCluster cl = new OrdinaryCluster(graph, 1.0);
		graph.addCluster(cl);

		final List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < 5; ++i)
		{
			final Node node = new Node();
			nodes.add(node);
			graph.addNode(node, cl);
		}
		@SuppressWarnings("unchecked")
		final List<Pair<Integer>> edges =
			Arrays.asList(
				new Pair<Integer>(0, 1),//
				new Pair<Integer>(1, 2),//
				new Pair<Integer>(1, 3),//
				new Pair<Integer>(1, 4),//
				new Pair<Integer>(2, 3),//
				new Pair<Integer>(3, 4)//
				);

		final List<Edge> edgeList = new ArrayList<Edge>();
		for (final Pair<Integer> edge : edges)
		{
			final Pair<Node> pair = new Pair<Node>(nodes.get(edge.getFirst()), nodes.get(edge
				.getSecond()));
			edgeList.add(Edge.createEdge(pair));
			graph.addEdge(pair);
		}

		for (final Edge edge : edgeList)
		{
			Assert.assertTrue(graph.contains(edge));
			Assert.assertTrue(edge.isIntraClusterEdge());
		}

		final List<Edge> remainingEdges = new ArrayList<Edge>(edgeList);
		while (!remainingEdges.isEmpty())
		{
			final Edge edge = cl.findEdge();
			Assert.assertTrue(remainingEdges.contains(edge)
				|| remainingEdges.contains(edge.getReverseEdge()));
			graph.removeEdge(edge);
			remainingEdges.remove(edge);
			remainingEdges.remove(edge.getReverseEdge());
		}

	}

	/**
	 * This test will
	 * - build a complete graph with one cluster
	 * - issue a split operation
	 * - remove all inter-cluster edges and
	 * - check whether the operation is considered 'complete'
	 *
	 */
	@Test
	public void testSplitOperation()
	{
		final DCRGraph graph = new DCRGraph(0.1, 0.8, PInSampler.MEAN);
		graph.addCluster(new OrdinaryCluster(graph, 0.5));

		for (int i = 0; i < 10; ++i)
		{
			graph.addNode();
		}
		for (int i = 0; i < Edge.maxEdgeCount(graph.getNodeCount()); ++i)
		{
			graph.addEdge();
		}

		Assert.assertEquals(10, graph.getNodeCount());
		Assert.assertEquals(0, graph.getInterClusterEdgeCount());
		Assert.assertEquals(Edge.maxEdgeCount(10), graph.getIntraClusterEdgeCount());

		graph.split();

		final List<AbstractClusterOperation> ops = graph.getListOfOperations();
		Assert.assertEquals(1, ops.size());
		final SplitOperation op = (SplitOperation) ops.get(0);

		final GroundTruth gt = graph.getGroundTruth();
		Assert.assertEquals(2, gt.getClusterCount());
		Assert.assertEquals(1, graph.getReferenceClustering().getClusterCount());

		final OrdinaryCluster firstCl = gt.getCluster(0);
		final int firstClSize = firstCl.getNodeCount();
		final OrdinaryCluster sndCl = gt.getCluster(1);
		final int sndClSize = sndCl.getNodeCount();

		Assert.assertEquals(new Pair<OrdinaryCluster>(firstCl, sndCl), op.getResultingClusters());
		Assert.assertEquals(graph.getReferenceClustering().getCluster(0), op.getInitialCluster());

		Assert.assertTrue(firstCl.isLocked());
		Assert.assertTrue(sndCl.isLocked());
		Assert.assertTrue(graph.getReferenceClustering().getCluster(0).isLocked());


		Assert.assertEquals(Edge.maxEdgeCount(firstClSize), firstCl.getIntraClusterEdgeCount());
		Assert.assertEquals(Edge.maxEdgeCount(sndClSize), sndCl.getIntraClusterEdgeCount());


		Assert.assertEquals(firstClSize * sndClSize, op.getEdgeCountBetweenClusters());
		Assert.assertEquals(firstClSize * sndClSize, firstCl.getInterClusterEdgeCount());
		Assert.assertEquals(firstClSize * sndClSize, sndCl.getInterClusterEdgeCount());

		Assert.assertEquals(firstCl.getInterClusterEdgeCount(),
			firstCl.getInterClusterEdgeCount(sndCl));
		Assert.assertEquals(sndCl.getInterClusterEdgeCount(),
			sndCl.getInterClusterEdgeCount(firstCl));

		Assert.assertEquals(
			Edge.maxEdgeCount(10) - Edge.maxEdgeCount(sndClSize) - Edge.maxEdgeCount(firstClSize),
			firstCl.getInterClusterEdgeCount());


		final List<Edge> edges = ListUtils.toList(firstCl.interClusterEdgeIterator());

		int deletedEdgeCtr = 0;
		for (final Edge edge : edges)
		{
			Assert.assertTrue(edge.isInterClusterEdge());

			graph.removeEdge(edge);
			++deletedEdgeCtr;

			Assert.assertEquals(firstClSize * sndClSize - deletedEdgeCtr,
				firstCl.getInterClusterEdgeCount(sndCl));
			Assert.assertEquals(firstClSize * sndClSize - deletedEdgeCtr,
				op.getEdgeCountBetweenClusters());

		}

		/*
		 * Still now, no clusters should be available for an operation!
		 */
		graph.merge();
		Assert.assertEquals(2, graph.getGroundTruth().getClusterCount());
		Assert.assertEquals(1, graph.getReferenceClustering().getClusterCount());
		Assert.assertEquals(1, graph.getListOfOperations().size());
		graph.split();
		Assert.assertEquals(2, graph.getGroundTruth().getClusterCount());
		Assert.assertEquals(1, graph.getReferenceClustering().getClusterCount());
		Assert.assertEquals(1, graph.getListOfOperations().size());

		Assert.assertTrue(graph.getListOfOperations().get(0).isComplete());

		Assert.assertEquals(0, firstCl.getInterClusterEdgeCount(sndCl));

		graph.checkClusterOperationsForCompleteness();


		Assert.assertTrue(!firstCl.isLocked());
		Assert.assertTrue(!sndCl.isLocked());

		Assert.assertEquals(2, graph.getReferenceClustering().getClusterCount());
		Assert.assertTrue(!graph.getReferenceClustering().getCluster(0).isLocked());
		Assert.assertTrue(!graph.getReferenceClustering().getCluster(1).isLocked());

		Assert.assertTrue(graph.getListOfOperations().isEmpty());

		Assert.assertEquals(graph.getGroundTruth(), graph.getReferenceClustering());
	}

	@Test
	public void testSplitOperationGeneral()
	{
		final double theta = 0.8;
		final DCRGraph graph = new DCRGraph(0.1, theta, PInSampler.GAUSSIAN);
		final ReferenceClustering refCl = graph.getReferenceClustering();
		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.4);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.6);
		final OrdinaryCluster thdCl = new OrdinaryCluster(graph, 0.6);
		final OrdinaryCluster fothCl = new OrdinaryCluster(graph, 0.99);
		final OrdinaryCluster ffthCl = new OrdinaryCluster(graph, 1.0);

		graph.addCluster(fstCl);
		graph.addCluster(sndCl);
		graph.addCluster(thdCl);
		graph.addCluster(fothCl);
		graph.addCluster(ffthCl);

		for (int i = 0; i < graph.getClusterCount() * 15; ++i)
		{
			graph.addNode();
		}
		for (int i = 0; i < Edge.maxEdgeCount(graph.getNodeCount()); ++i)
		{
			graph.addEdge();
		}

		graph.checkClusterOperationsForCompleteness();
		graph.split();
		Assert.assertEquals(1, graph.getListOfOperations().size());
		Assert.assertEquals(6, graph.getClusterCount());

		OrdinaryCluster initialCluster = null;
		for (int c = 0; c < refCl.getClusterCount(); ++c)
		{
			final OrdinaryCluster current = refCl.getCluster(c);
			// exactly one cluster is locked
			Assert.assertTrue(BooleanUtils.implies(initialCluster != null, !current.isLocked()));
			if (current.isLocked())
			{
				Assert.assertFalse(current.isInGroundTruth());
				initialCluster = current;
			}
		}
		Assert.assertNotNull(initialCluster);



		final SplitOperation splitOp = (SplitOperation) graph.getListOfOperations().get(0);
		final Pair<OrdinaryCluster> resultingCls = splitOp.getResultingClusters();

		final OrdinaryCluster fstResCl = resultingCls.getFirst();
		Assert.assertTrue(fstResCl.isLocked());
		Assert.assertTrue(fstResCl.isInGroundTruth());

		final OrdinaryCluster sndResCl = resultingCls.getSecond();
		Assert.assertTrue(sndResCl.isLocked());
		Assert.assertTrue(sndResCl.isInGroundTruth());

		final List<Edge> edges = ListUtils.toList(fstResCl.interClusterEdgeIterator(sndResCl));
		Assert.assertEquals(fstResCl.getNodeCount() * sndResCl.getNodeCount(), edges.size());

		graph.checkClusterOperationsForCompleteness();
		/*
		 * Simulate what we expect the split operation to do.
		 * It should be complete if m(C_1,C_2) < theta * intraCnt +
		 * (1-theta) * interCnt
		 */
		final double expectedIntraCount = fstResCl.getNodeCount() * sndResCl.getNodeCount()
			* initialCluster.getPIn();
		final double expectedInterCount = fstResCl.getNodeCount() * sndResCl.getNodeCount()
			* graph.getPOut();

		for (final Edge edge : edges)
		{

			final int countBefore = splitOp.getEdgeCountBetweenClusters();
			Assert.assertTrue(edge.isInterClusterEdge(fstResCl, sndResCl));
			graph.removeEdge(edge);
			if (splitOp.isRunning())
				Assert.assertEquals(countBefore - 1, splitOp.getEdgeCountBetweenClusters());

			graph.checkClusterOperationsForCompleteness();

			final double threshold = theta * expectedIntraCount + (1 - theta) * expectedInterCount;
			if (splitOp.getEdgeCountBetweenClusters() <= threshold)
			{
				Assert.assertTrue(splitOp.isComplete());
				Assert.assertEquals(0, graph.getListOfOperations().size());
				Assert.assertFalse(splitOp.isRunning());
			}
		}

	}

	@Test
	public void testMergeOperation()
	{
		final DCRGraph graph = new DCRGraph(0.1, 0.5, PInSampler.MEAN);
		final OrdinaryCluster fstOrig = new OrdinaryCluster(graph, 0.5);
		final OrdinaryCluster sndOrig = new OrdinaryCluster(graph, 0.8);
		graph.addCluster(fstOrig);
		graph.addCluster(sndOrig);

		graph.addNode(new Node(), fstOrig);
		graph.addNode(new Node(), sndOrig);
		for (int i = 2; i < 20; ++i)
		{
			graph.addNode();
		}

		for (int i = 0; i < fstOrig.getNodeCount(); ++i)
		{
			for (int j = i + 1; j < fstOrig.getNodeCount(); ++j)
			{
				graph.addEdge(new Pair<Node>(fstOrig.getNode(i), fstOrig.getNode(j)));
			}
		}

		for (int i = 0; i < sndOrig.getNodeCount(); ++i)
		{
			for (int j = i + 1; j < sndOrig.getNodeCount(); ++j)
			{
				graph.addEdge(new Pair<Node>(sndOrig.getNode(i), sndOrig.getNode(j)));
			}
		}

		Assert.assertEquals(2, graph.getGroundTruth().getClusterCount());
		Assert.assertEquals(2, graph.getReferenceClustering().getClusterCount());
		Assert.assertEquals(0, graph.getListOfOperations().size());
		graph.merge();
		Assert.assertEquals(1, graph.getGroundTruth().getClusterCount());
		Assert.assertEquals(2, graph.getReferenceClustering().getClusterCount());
		Assert.assertEquals(1, graph.getListOfOperations().size());
		graph.split();
		Assert.assertEquals(1, graph.getGroundTruth().getClusterCount());
		Assert.assertEquals(2, graph.getReferenceClustering().getClusterCount());
		Assert.assertEquals(1, graph.getListOfOperations().size());


		// graph contains no edges

		graph.merge();

		Assert.assertEquals(1, graph.getGroundTruth().getClusterCount());
		Assert.assertEquals(2, graph.getReferenceClustering().getClusterCount());
		Assert.assertEquals(1, graph.getListOfOperations().size());
		final MergeOperation mergeOp = (MergeOperation) graph.getListOfOperations().get(0);

		// let the number of inter-cluster edges grow

		final Iterator<Node> fstNodeIter = fstOrig.nodeIterator();
		int interClCounter = 0;
		while (fstNodeIter.hasNext())
		{
			final Node fstNode = fstNodeIter.next();
			final Iterator<Node> sndNodeIter = sndOrig.nodeIterator();
			while (sndNodeIter.hasNext())
			{
				graph.addEdge(new Pair<Node>(fstNode, sndNodeIter.next()));
				Assert.assertEquals(++interClCounter, mergeOp.getEdgeCountBetweenClusters());
			}
		}

		Assert.assertTrue(mergeOp.isComplete());
		graph.checkClusterOperationsForCompleteness();
		Assert.assertTrue(graph.getListOfOperations().isEmpty());

		Assert.assertTrue(!fstOrig.isLocked());
		Assert.assertTrue(!sndOrig.isLocked());

		Assert.assertEquals(1, graph.getReferenceClustering().getClusterCount());
		Assert.assertTrue(!graph.getReferenceClustering().getCluster(0).isLocked());

		Assert.assertEquals(graph.getGroundTruth(), graph.getReferenceClustering());

	}

	@Test
	public void testMergeOperationGeneral()
	{
		final double theta = 0.8;
		final DCRGraph graph = new DCRGraph(0.1, theta, PInSampler.GAUSSIAN);
		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.4);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.6);
		final OrdinaryCluster thdCl = new OrdinaryCluster(graph, 0.6);
		final OrdinaryCluster fothCl = new OrdinaryCluster(graph, 0.99);
		final OrdinaryCluster ffthCl = new OrdinaryCluster(graph, 1.0);
		graph.addCluster(fstCl);
		graph.addCluster(sndCl);
		graph.addCluster(thdCl);
		graph.addCluster(fothCl);
		graph.addCluster(ffthCl);

		// create complete graph
		for (int i = 0; i < graph.getClusterCount() * 15; ++i)
		{
			graph.addNode();
		}
		for (int i = 0; i < Edge.maxEdgeCount(graph.getNodeCount()); ++i)
		{
			graph.addEdge();
		}

		// choose pair of clusters to be split
		final Pair<OrdinaryCluster> clusters = graph.getGroundTruth().proposeClustersForMerge();

		final OrdinaryCluster fst = clusters.getFirst();
		final OrdinaryCluster snd = clusters.getSecond();

		final List<Edge> edgesToDelete = ListUtils.toList(//
			fst.interClusterEdgeIterator(snd));
		final Pair<List<Edge>> splitList = ListUtils.split(edgesToDelete, edgesToDelete.size() / 2);
		for (final Edge edge : splitList.getFirst())
		{
			graph.removeEdge(edge);
		}

		Assert.assertEquals(splitList.getSecond().size(), //
			ListUtils.toList(fst.interClusterEdgeIterator(snd)).size());

		graph.checkClusterOperationsForCompleteness();

		// start merge operation
		graph.merge(clusters);
		Assert.assertEquals(1, graph.getListOfOperations().size());
		Assert.assertEquals(4, graph.getClusterCount());

		final MergeOperation mergeOp = (MergeOperation) graph.getListOfOperations().get(0);
		final OrdinaryCluster resultingCl = mergeOp.getResultingCluster();

		Assert.assertTrue(mergeOp.getInitialClusters().equals(clusters)
			|| mergeOp.getInitialClusters().equals(clusters.getReversedPair()));

		Assert.assertTrue(fst.isLocked());
		Assert.assertTrue(snd.isLocked());

		Assert.assertTrue(fst.isInReferenceClustering());
		Assert.assertTrue(snd.isInReferenceClustering());
		Assert.assertTrue(resultingCl.isInGroundTruth());

		Assert.assertFalse(fst.isInGroundTruth());
		Assert.assertFalse(snd.isInGroundTruth());
		Assert.assertFalse(resultingCl.isInReferenceClustering());

		// remove remaining edges
		for (final Edge edge : splitList.getSecond())
		{
			graph.removeEdge(edge);
		}

		Assert.assertFalse(mergeOp.isComplete());
		Assert.assertEquals(0, mergeOp.getEdgeCountBetweenClusters());
		graph.checkClusterOperationsForCompleteness();
		Assert.assertEquals(1, graph.getListOfOperations().size());

		/*
		 * Simulate what we expect the split operation to do.
		 * It should be complete if m(C_1,C_2) < theta * intraCnt +
		 * (1-theta) * interCnt
		 */
		final double expectedIntraCount = fst.getNodeCount() * snd.getNodeCount()
			* resultingCl.getPIn();
		final double expectedInterCount = fst.getNodeCount() * snd.getNodeCount()
			* graph.getPOut();

		for (int i = 0; i < resultingCl.getNodeCount(); ++i)
		{
			for (int j = i + 1; j < resultingCl.getNodeCount(); ++j)
			{
				final Pair<Node> nodes = new Pair<Node>(resultingCl.getNode(i),
					resultingCl.getNode(j));

				if (nodes.getFirst().getRefClCluster().equals(clusters.getFirst())
					&& nodes.getSecond().getRefClCluster().equals(clusters.getSecond()))
				{
					final int countBefore = mergeOp.getEdgeCountBetweenClusters();
					graph.addEdge(nodes);
					if (mergeOp.isRunning())
						Assert.assertEquals(countBefore + 1, mergeOp.getEdgeCountBetweenClusters());

					graph.checkClusterOperationsForCompleteness();

					final double threshold = theta * expectedIntraCount + (1 - theta)
						* expectedInterCount;
					if (mergeOp.getEdgeCountBetweenClusters() >= threshold)
					{
						Assert.assertTrue(mergeOp.isComplete());
						Assert.assertEquals(0, graph.getListOfOperations().size());
						Assert.assertFalse(mergeOp.isRunning());
					}

				}
			}
		}
	}

	@Ignore
	@Test(
		expected = AssertionError.class)
	public void testErdosRenyiInconsistent()
	{
		final List<Integer> sizes = Arrays.asList(10, 5, 5);
		final List<Double> pIn = Arrays.asList(0.6, 0.8);
		DCRGraphTest.generateErdosRenyi(sizes, pIn, 1, PInSampler.MEAN, 0.5);
		// DCRGraph.generateErdosRenyi(20, sizes, pIn, 1, null, 0.5);
		// DCRGraph.generateErdosRenyi(20, null, pIn, 1, PInSampler.MEAN,
		// 0.5);
	}

	@Test
	public void testErdosRenyiSingle()
	{
		final List<Integer> sizes = Arrays.asList(20);
		final List<Double> pIn = Arrays.asList(0.6);
		final double pOut = 0.1;
		final DCRGraph graph = DCRGraphTest.generateErdosRenyi(sizes, pIn, pOut, PInSampler.MEAN, 0.5);

		Assert.assertEquals(0, graph.getListOfOperations().size());
		graph.merge();
		Assert.assertEquals(0, graph.getListOfOperations().size());

		Assert.assertEquals(20, graph.getNodeCount());
		Assert.assertEquals(0, graph.getInterClusterEdgeCount());

	}

	@Test
	public void testErdosRenyiTriple()
	{
		final List<Integer> sizes = Arrays.asList(10, 5, 5);
		final List<Double> pIn = Arrays.asList(0.6, 0.8, 0.8);
		final double pOut = 0.1;
		final DCRGraph graph = DCRGraphTest.generateErdosRenyi(sizes, pIn, pOut, PInSampler.MEAN, 0.5);

		Assert.assertEquals(20, graph.getNodeCount());
		Assert.assertEquals(3, graph.getClusterCount());
		Assert.assertEquals(3, graph.getGroundTruth().getClusterCount());

		int nodesum = 0;
		int intraClEdges = 0;
		int interClEdges = 0;
		for (int i = 0; i < graph.getClusterCount(); ++i)
		{
			final OrdinaryCluster cluster = graph.getGroundTruth().getCluster(i);
			Assert.assertTrue(cluster.getNodeCount() > 0);
			nodesum += cluster.getNodeCount();
			intraClEdges += cluster.getIntraClusterEdgeCount();
			interClEdges += cluster.getInterClusterEdgeCount();
		}

		interClEdges /= 2; // each edges is counted in both incident
							// clusters
		Assert.assertEquals(interClEdges, graph.getInterClusterEdgeCount());
		Assert.assertEquals(intraClEdges, graph.getIntraClusterEdgeCount());

		Assert.assertEquals(intraClEdges + interClEdges, graph.getEdgeCount());

		Assert.assertEquals(20, nodesum);
	}

	/**
	 * @see DCRGraphTest.randomGraph
	 */
	@Test
	public void testErdosRenyiRandomGraphMethod()
	{
		int numRuns = 10;
		for (int i = 0; i < numRuns; ++i)
		{
			DCRGraphTest.randomGraph();
		}
	}

	@Test
	public void testErdosRenyi0001()
	{
		final List<Integer> sizes = Arrays.asList(20, 15, 5);
		final List<Double> pIn = Arrays.asList(0.6, 0.65, 0.7);
		final DCRGraph graph = DCRGraphTest.generateErdosRenyi(sizes, pIn, 0.1, PInSampler.MEAN, 0.5);

		Assert.assertEquals(20 + 15 + 5, graph.getNodeCount());
		final int edgeCount = graph.getEdgeCount();
		int countedEdges = 0;
		final GroundTruth gt = graph.getGroundTruth();
		final ReferenceClustering refcl = graph.getReferenceClustering();

		final PseudoCluster pcl = graph.getPseudoCluster();

		Assert.assertEquals(gt, refcl);

		for (int c = 0; c < gt.getClusterCount(); ++c)
		{
			final OrdinaryCluster cl = gt.getCluster(c);
			for (int v = 0; v < cl.getNodeCount(); ++v)
			{
				final Node node = cl.getNode(v);
				final Iterator<Edge> iter = cl.edgeIterator(node);
				while (iter.hasNext())
				{
					final Edge edge = iter.next();
					++countedEdges;
					Assert.assertTrue("e: " + edge,
						BooleanUtils.equivalent(edge.isInterClusterEdge(),
							edge.getInterClusterEdgeListID() !=
							Edge.INVALID_ID));

					Assert.assertTrue("e: " + edge, pcl.contains(edge));

				}
			}
		}

		Assert.assertEquals(2 * edgeCount, countedEdges);

		graph.removeNode();
	}

	@Ignore
	@Test
	public void testRandomNodeOperationsOnly()
	{
		final DCRGraph graph = new DCRGraph(0.5, 0.5, PInSampler.MEAN);
		final OrdinaryCluster cl = new OrdinaryCluster(graph, 0.9);
		graph.addCluster(cl);

		final int numRuns = (int) 1e4;
		final double nodeInsertProb = 0.6;
		final Random random = new Random(0);

		Assert.assertEquals(0, graph.getEdgeCount());
		for (int r = 0; r < numRuns; ++r)
		{
			final int nodeCountBefore = graph.getNodeCount();
			if (random.nextDouble() < nodeInsertProb
				|| graph.getNodeCount() <= graph.getClusterCount())
			{
				graph.addNode();
				Assert.assertEquals(nodeCountBefore + 1, graph.getNodeCount());
			}
			else
			{
				graph.removeNode();
				Assert.assertEquals(nodeCountBefore - 1, graph.getNodeCount());
			}

			Assert.assertEquals(0, graph.getEdgeCount());
		}

	}

	@Ignore
	@Test
	public void testRandomNodeAndEdgeOperationsOnly()
	{
		final DCRGraph graph = new DCRGraph(0.5, 0.5, PInSampler.MEAN);
		final OrdinaryCluster cl = new OrdinaryCluster(graph, 0.9);
		graph.addCluster(cl);

		final int numRuns = (int) 1e4;
		final double edgeOpProb = 0.1;
		final double nodeInsertProb = 0.6;
		final double edgeInsertProb = 0.6;
		final Random random = new Random();

		Assert.assertEquals(0, graph.getEdgeCount());
		for (int r = 0; r < numRuns; ++r)
		{
			if (graph.getNodeCount() >= 2 && random.nextDouble() < edgeOpProb)
			{
				final int edgeCountBefore = graph.getEdgeCount();
				if ((graph.getEdgeCount() < Edge.maxEdgeCount(graph.getNodeCount()) && random
					.nextDouble() < edgeInsertProb)
					|| graph.getEdgeCount() == 0)
				{
					graph.addEdge();
					Assert.assertEquals(edgeCountBefore + 1, graph.getEdgeCount());
				}
				else
				{
					graph.removeEdge();
					Assert.assertEquals(edgeCountBefore - 1, graph.getEdgeCount());
				}
			}
			else
			{
				final int nodeCountBefore = graph.getNodeCount();
				if (random.nextDouble() < nodeInsertProb
					|| graph.getNodeCount() <= graph.getClusterCount())
				{
					graph.addNode();
					Assert.assertEquals(nodeCountBefore + 1, graph.getNodeCount());
				}
				else
				{
					graph.removeNode();
					Assert.assertEquals(nodeCountBefore - 1, graph.getNodeCount());
				}
			}
		}
	}

	@Test
	public void testRandomClusterAndNodeOperations()
	{
		final Random random = new Random();
		final double theta = 0.8;
		final DCRGraph graph = new DCRGraph(0.1, theta, PInSampler.GAUSSIAN);
		final OrdinaryCluster fstCl = new OrdinaryCluster(graph, 0.4);
		final OrdinaryCluster sndCl = new OrdinaryCluster(graph, 0.6);
		final OrdinaryCluster thdCl = new OrdinaryCluster(graph, 0.6);

		graph.addCluster(fstCl);
		graph.addCluster(sndCl);
		graph.addCluster(thdCl);

		for (int i = 0; i < graph.getClusterCount() * 15; ++i)
		{
			graph.addNode();
		}
		for (int i = 0; i < Edge.maxEdgeCount(graph.getNodeCount()); ++i)
		{
			graph.addEdge();
		}

		int numRuns = 10000;
		final double edgeOpProb = 0.1;
		final double nodeInsertProb = 0.6;
		final double edgeInsertProb = 0.6;
		for (int i = 0; i < 3; ++i)
		{
			switch (i)
			{
				case 0:
					;
					break;
				case 1:
					graph.split(fstCl);
					break;
				case 2:
					graph.merge(new Pair<OrdinaryCluster>(sndCl, thdCl));
					break;
				default:
					assert false;
			}
			for (int r = 0; r < numRuns; ++r)
			{
				if (graph.getNodeCount() >= 2 && random.nextDouble() < edgeOpProb)
				{
					final int edgeCountBefore = graph.getEdgeCount();
					if ((graph.getEdgeCount() < Edge.maxEdgeCount(graph.getNodeCount()) && random
						.nextDouble() < edgeInsertProb)
						|| graph.getEdgeCount() == 0)
					{
						graph.addEdge();
						Assert.assertEquals(edgeCountBefore + 1, graph.getEdgeCount());
					}
					else
					{
						graph.removeEdge();
						Assert.assertEquals(edgeCountBefore - 1, graph.getEdgeCount());
					}
				}
				else
				{
					final int nodeCountBefore = graph.getNodeCount();
					if (random.nextDouble() < nodeInsertProb
						|| graph.getNodeCount() <= graph.getClusterCount())
					{
						graph.addNode();
						Assert.assertEquals(nodeCountBefore + 1, graph.getNodeCount());
					}
					else
					{
						graph.removeNode();
						Assert.assertEquals(nodeCountBefore - 1, graph.getNodeCount());
					}
				}
			}
		}
	}

	/**
	 * This test shall 'verify' that the method produces a valid
	 * pin-pout-graph.
	 */
	@Ignore
	@Test
	public void testErdosRenyiCheckPInPOut()
	{
		int numRuns = 100;


		final List<Integer> sizes = Arrays.asList(70, 61, 52, 46, 37);
		List<Double> measuredSizes = new ArrayList<Double>(Collections.nCopies(sizes.size(), 0.0));

		final List<Double> pins = Arrays.asList(0.5, 0.65, 0.65, 0.70, 0.8);
		List<Double> measuredPIns = new ArrayList<Double>(Collections.nCopies(sizes.size(), 0.0));

		final double pOut = 0.01768;
		double measuredPOut = 0.0;

		Assert.assertEquals(sizes.size(), pins.size());

		for (int r = 0; r < numRuns; ++r)
		{
			// theta and the sampler are meaningless as we do not trigger
			// the
			// generation process
			final DCRGraph graph = DCRGraphTest.generateErdosRenyi(sizes, pins, pOut, PInSampler.MEAN,
				0.5);

			measuredSizes = ListUtils.elementwiseSum(measuredSizes, ListUtils.mapType(DCRGraphTest.calculateSizes(graph), Double.class));

			measuredPIns = ListUtils.elementwiseSum(measuredPIns, DCRGraphTest.calculatePIns(graph));

			measuredPOut += DCRGraphTest.calculatePOut(graph);
		}

		System.out.println(ListUtils.elementwiseQuotient(measuredSizes, numRuns));
		System.out.println(sizes);

		System.out.println(ListUtils.elementwiseQuotient(measuredPIns, numRuns));
		System.out.println(pins);

		System.out.println(measuredPOut / numRuns);
		System.out.println(pOut);

	}

	private static DCRGraph generateErdosRenyi(
		final List<Integer> sizes,
		final List<Double> pins, final double pOut,
		final PInSampler mean, final double theta)
	{
		final DCRGraph graph = new DCRGraph(pOut, 0.5, mean, false);
		graph.initAsErdosRenyi(sizes, pins, pOut);
		return graph;
	}

	static List<Double> calculatePIns(final DCRGraph graph)
	{
		final List<Double> pins = new ArrayList<Double>(graph.getClusterCount());

		for (int c = 0; c < graph.getClusterCount(); ++c)
		{
			final OrdinaryCluster cl = graph.getGroundTruth().getCluster(c);
			int intraClEdgeCount = cl.getIntraClusterEdgeCount();

			final double pin = intraClEdgeCount / (double) Edge.maxEdgeCount(cl.getNodeCount());

			Assert.assertTrue("pin: " + pin, 0 <= pin && pin <= 1.0);
			pins.add(pin);
		}

		return pins;
	}

	static double calculatePOut(final DCRGraph graph)
	{
		final int interClEdgeCnt = graph.getInterClusterEdgeCount();
		final long interClNonEdgeCnt = graph.getInterClusterNonEdgeCount();

		final double pOut = interClEdgeCnt / (double) (interClEdgeCnt + interClNonEdgeCnt);

		Assert.assertTrue("p: " + pOut, 0.0 <= pOut && pOut <= 1.0);
		return pOut;
	}

	static List<Integer> calculateSizes(final DCRGraph graph)
	{
		final List<Integer> sizes = new ArrayList<Integer>(graph.getClusterCount());

		for (int c = 0; c < graph.getClusterCount(); ++c)
		{
			sizes.add(graph.getGroundTruth().getCluster(c).getNodeCount());
		}
//		Collections.sort(sizes);

		return sizes;
	}

	static DCRGraph randomGraph()
	{
		final Random random = new Random();
		final int clusterCount = 1 + random.nextInt(19);
		final List<Integer> sizes = new ArrayList<Integer>(clusterCount);
		final List<Double> pIn = new ArrayList<Double>(clusterCount);
		for (int c = 0; c < clusterCount; ++c)
		{
			sizes.add(1 + random.nextInt(20));
			pIn.add(0.4 + random.nextDouble() * 0.6);
		}

		Assert.assertEquals(clusterCount, pIn.size());
		Assert.assertEquals(sizes.size(), pIn.size());

		final double pOut = random.nextDouble() * 0.25;
		final PInSampler pInSampler = (random.nextDouble()) < 0.5 ? PInSampler.MEAN : PInSampler.GAUSSIAN;
		final double theta = 0.5 + random.nextDouble() * 0.3;
		return DCRGraphTest.generateErdosRenyi(sizes, pIn, pOut, pInSampler, theta);
	}

	public static DCRGraph emptyGraph()
	{
		return new DCRGraph();
	}
}
