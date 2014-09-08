package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.iti.ldcrgen.Main;
import edu.kit.iti.ldcrgen.VerbosityLevel;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.BinarySelectionTree;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.TreeNode;
import edu.kit.iti.ldcrgen.io.journaling.ClusteringJournal;
import edu.kit.iti.ldcrgen.io.journaling.GraphJournal;
import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Pair;
import edu.kit.iti.ldcrgen.util.Sequences;

/**
 * Represents a clustered, dynamic and random graph.
 * The graph offers a whole lot of operations for adding and deleting
 * nodes, edges and clusters.
 * Furthermore, the cluster can be represented as {@link GraphJournal} and
 * {@link ClusteringJournal} for the purpose of serializing it to file.
 *
 * @author Roland Kluge
 */
public class DCRGraph
{
	private final RandomProvider random;

	private double theta;
	private PInSampler pInSampler;
	private double pOut;

	private GroundTruth groundTruth;
	private ReferenceClustering referenceClustering;
	private PseudoCluster pseudoCluster;

	private BinarySelectionTree insertionTree;
	private BinarySelectionTree deletionTree;

	private int currentTimeStep;

	private ArrayList<AbstractClusterOperation> runningOperations;
	/*
	 * Maps the global index of an operation to the operation itself
	 * Only contains running operations!
	 */
	private Map<Integer, AbstractClusterOperation> indexOperationMapping;

	private final GraphJournal gJournal = new GraphJournal();
	private final ClusteringJournal clJournal = new ClusteringJournal();

	private int smallScaleOperationCount;
	private int largeScaleOperationCount;

	// stores the maximum-number of intra-cluster edges
	private int maxIntraClusterEdgeCount;

	private boolean useTreeMapInsteadOfHashMap;

	public DCRGraph()
	{
		this.smallScaleOperationCount = 0;
		this.largeScaleOperationCount = 0;
		this.maxIntraClusterEdgeCount = 0;

		this.random = new JavaUtilRandomProvider();
		this.runningOperations = new ArrayList<AbstractClusterOperation>();
		this.indexOperationMapping = new HashMap<Integer, AbstractClusterOperation>();

		this.groundTruth = new GroundTruth();
		this.referenceClustering = new ReferenceClustering();
		this.pseudoCluster = new PseudoCluster(this);
		this.currentTimeStep = 0;

		this.pOut = -1.0;
		this.theta = -1.0;

		this.insertionTree = new BinarySelectionTree();
		this.deletionTree = new BinarySelectionTree();

		this.pseudoCluster.setInsertionTreeNode(//
			this.insertionTree.insert(this.pseudoCluster.getInsertionWeight()));
		this.pseudoCluster.setDeletionTreeNode(//
			this.deletionTree.insert(this.pseudoCluster.getDeletionWeight()));
	}

	public DCRGraph(final double pOut, final double theta, final PInSampler sampler)
	{
		this();
		this.pOut = pOut;
		this.theta = theta;
		this.pInSampler = sampler;
	}

	public DCRGraph(final double pOut, final double theta, final PInSampler sampler,
		final boolean useTreeMapInsteadOfHashMap)
	{
		this(pOut, theta, sampler);
		this.useTreeMapInsteadOfHashMap = useTreeMapInsteadOfHashMap;
		this.pseudoCluster = new PseudoCluster(this);
	}

	/**
	 * Returns the inter-cluster edge probability.
	 *
	 * The probability stays constant over the whole process.
	 *
	 * @return the inter-cluster edge probability
	 */
	public double getPOut()
	{
		return this.pOut;
	}

	public double getClusterOpThreshold()
	{
		return theta;
	}

	PInSampler getSamplerForNewPIn()
	{
		return this.pInSampler;
	}

	public int getSmallScaleOpCount()
	{
		return this.smallScaleOperationCount;
	}

	public int getLargeScaleOpCount()
	{
		return this.largeScaleOperationCount;
	}

	/**
	 * This method initializes this graph as G(n,p_in,p_out) graph. <br/>
	 * <p>
	 * None of the parameters may be null! The number of nodes is
	 * determined by summing up all cluster sizes. The list of cluster
	 * sizes and intra-cluster edge probabilities must be of equal length.
	 * <code>pOut</code> and <code>theta</code> need to be probabilities.
	 * </p>
	 * <br/>
	 * <p>
	 * The result of this method is a completely random graph fulfilling
	 * the given model's premises.
	 * </p>
	 *
	 * @param clusterSizes
	 *            the size of each single cluster
	 * @param pInValues
	 *            the intra-cluster edge probabilities
	 * @param pOut
	 *            the inter-cluster edge probability
	 */
	public void initAsErdosRenyi(final List<Integer> clusterSizes, final List<Double> pInValues,
		final double pOut)
	{
		this.pOut = pOut;
		// node count
		final int n = (int) ListUtils.sumUp(ListUtils.mapType(clusterSizes, Double.class));

		/*
		 * Create clusters
		 */
		for (int index = 0; index < pInValues.size(); ++index)
		{
			final double pIn = pInValues.get(index);
			final int clSize = clusterSizes.get(index);

			final OrdinaryCluster cluster = new OrdinaryCluster(this, pIn, clSize);
			this.addCluster(cluster);
		}

		final int clusterCount = this.getClusterCount();
		assert clusterCount == pInValues.size();

		/*
		 * Distribute nodes:
		 * At first make sure, that every cluster has at least size one.
		 */
		// insert one node into every cluster
		for (int c = 0; c < clusterCount; ++c)
		{
			final OrdinaryCluster home = this.groundTruth.getCluster(c);
			final Node node = new Node();
			this.addNode(node, home);
		}

		// distribute remaining nodes
		for (int c = clusterCount; c < n; ++c)
		{
			final OrdinaryCluster home = this.groundTruth.proposeClusterForNewNode();
			final Node node = new Node();
			this.addNode(node, home);
		}

		/*
		 * Create intra-cluster edges
		 * using the geometric method
		 */
		for (int c = 0; c < clusterCount; ++c)
		{
			final OrdinaryCluster cluster = this.groundTruth.getCluster(c);
			final long maxEdgeCount = Edge.maxEdgeCount(cluster.getNodeCount());

			final List<Long> edges = Sequences.binomialSequence(cluster.getPIn(), maxEdgeCount);

			for (final long edge : edges)
			{
				final Pair<Integer> localNodeIDs = Edge.nodeIndices(edge);
				final Pair<Node> nodes = cluster.getNodes(localNodeIDs);

				assert Edge.createEdge(nodes).isIntraClusterEdge() : "nodes: " + nodes;
				this.addEdge(nodes);
			}
		}

		/*
		 * Create inter-cluster edges
		 * (also using geometric method)
		 * The 'indices' of all edges are created at once.
		 * Due to the irregular structure of inter-cluster edges,
		 * these indices need to be transformed first.
		 * We procede as follows:
		 *
		 * 1. iterate over all nodes (source node/source cluster)
		 * 2. For each source node:
		 * iterate over all nodes within clusters having a larger
		 * cluster index than the source cluster (destination
		 * node/destination
		 * cluster)
		 */
		assert this.getInterClusterEdgeCount() == 0;

		for (int srcClusterIdx = 0; srcClusterIdx < this.groundTruth.getClusterCount(); ++srcClusterIdx)
		{
			final OrdinaryCluster srcCluster = this.groundTruth.getCluster(srcClusterIdx);
			for (int targetClusterIdx = srcClusterIdx + 1; targetClusterIdx < this.groundTruth
				.getClusterCount(); ++targetClusterIdx)
			{
				final OrdinaryCluster targetCluster = this.groundTruth.getCluster(targetClusterIdx);
				final int targetNodeCount = targetCluster.getNodeCount();
				final int srcNodeCount = srcCluster.getNodeCount();
				final long maxEdgeCount = srcNodeCount * targetNodeCount;

				final List<Long> edgeIndices = Sequences.binomialSequence(pOut, maxEdgeCount);

				for (final Long edge : edgeIndices)
				{
					long srcNodeIdx = 0;
					long targetNodeIdx = edge;
					while (!targetCluster.isValidNodeIndex(targetNodeIdx))
					{
						++srcNodeIdx;
						targetNodeIdx = targetNodeIdx - targetNodeCount;
					}

					assert srcCluster.isValidNodeIndex(srcNodeIdx);

					final Node srcNode = srcCluster.getNode((int) srcNodeIdx);
					final Node otherNode = targetCluster.getNode((int)
						targetNodeIdx);

					final Pair<Node> nonEdge = new Pair<Node>(srcNode, otherNode);

					this.addEdge(nonEdge);
				}

			}
		}
	}

	/**
	 * Issues the next time step.
	 * Places time step events in both journals.
	 */
	public void nextTimeStep()
	{
		gJournal.nextStepOp();
		clJournal.nextStepOp();

		++currentTimeStep;
		this.logAndPrintTimeStep(currentTimeStep);
	}

	/**
	 * Returns the current time step.
	 *
	 * After generating the graph, the time is set to 0.
	 *
	 * @return the current time
	 */
	public int getCurrentTime()
	{
		return this.currentTimeStep;
	}

	/*
	 * *********************************************************************
	 * ***
	 * Edge operations
	 * ***
	 * *********************************************************************
	 */

	/**
	 * Weighted selection, whether to perform an edge insertion or an
	 * edge deletion.
	 *
	 * @return true if the next edge operation shall be an edge insertion,
	 *         false if the next edge operation shall be an edge deleteion
	 */
	public boolean shallDoEdgeInsertion()
	{
		boolean doInsert = false;

		final double weightOfAllInsertions = this.insertionTree.getWeight();
		final double weightOfAllDeletions = this.deletionTree.getWeight();

		final double rand = random.nextDouble() * (weightOfAllDeletions + weightOfAllInsertions);
		if (rand < weightOfAllInsertions)
		{
			doInsert = true;
		}

		return doInsert;
	}

	/**
	 * Selects a pair of nodes and adds an appropriate edge to the graph.
	 *
	 * There has to be at least once pair of nodes which is unconnected.
	 * Otherwise, the method will leave the graph unchanged.
	 */
	public void addEdge()
	{
		if (this.insertionTree.getWeight() > 0.0)
		{
			final TreeNode tNode = this.insertionTree.select();
			final AbstractCluster cluster = (AbstractCluster) tNode.getElement().getObject();
			final Pair<Node> nodes = cluster.findNonEdge();

			this.addEdge(nodes);
		}
		else
		{
			this.logAndPrintNoEdgeInsertion();
		}
	}

	/**
	 * Connects the given pair of nodes with a new undirected edge.
	 *
	 * The edge must not exist (no multi-edges)!
	 * Moreover, the endpoints of the edge must not be identical
	 * (no loops are allowed)!
	 *
	 * If the nodes participate in an operation, the operation
	 * will be notified and all necessary steps are taken
	 * (update of m(C_1,C_2)).
	 *
	 * @param nonEdge
	 *            the nodes to be connected
	 */
	void addEdge(final Pair<Node> nonEdge)
	{
		++this.smallScaleOperationCount;
		this.logAndPrintEdgeInsertion(nonEdge);

		final Edge edge = Edge.createEdge(nonEdge);
		edge.getCluster().addEdge(edge);

		if (edge.isInvolvedInOperation())
		{
			edge.getOperation().notifyEdgeInsertion(edge);
		}

		this.gJournal.createEdgeOp(//
			edge.getSource().getJournalIndex(), //
			edge.getTarget().getJournalIndex());

	}

	/**
	 * Selects and removes an edge from the graph.
	 *
	 * The method will only take effect if the graph has at least one
	 * edge.
	 */
	public void removeEdge()
	{
		if (this.deletionTree.getWeight() > 0.0)
		{
			final TreeNode tNode = this.deletionTree.select();
			final AbstractCluster cluster = (AbstractCluster) tNode.getElement().getObject();

			final Edge edge = cluster.findEdge();

			this.removeEdge(edge);
		}
		else
		{
			this.logAndPrintNoEdgeDeletion();
		}
	}

	/**
	 * Removes the given edge from the graph.
	 *
	 * The edge must be contained in the graph!
	 *
	 * If the endpoints of the edge participate in an operation, the
	 * operation
	 * will be notified and all necessary steps are taken
	 * (update of m(C_1,C_2)).
	 *
	 * @param edge
	 *            the edge to be removed
	 */
	void removeEdge(final Edge edge)
	{
		++this.smallScaleOperationCount;
		this.logAndPrintEdgeDeletion(edge);

		if (edge.isInvolvedInOperation())
		{
			edge.getOperation().notifyEdgeDeletion(edge);
		}

		edge.getCluster().removeEdge(edge);

		this.gJournal.removeEdgeOp(//
			edge.getSource().getJournalIndex(), //
			edge.getTarget().getJournalIndex());
	}

	/**
	 * Removes all edges of a node.
	 *
	 * The node must be contained in the graph!
	 * For convenience reasons the method returns the number of edges
	 * deleted during the operation.
	 *
	 * @param node
	 *            the node which shall be isolated.
	 * @return the number of deleted edges
	 */
	public int removeAllEdges(final Node node)
	{
		final OrdinaryCluster cluster = node.getGtCluster();
		final List<Edge> edges = ListUtils.toList(cluster.edgeIterator(node));

		for (final Edge e : edges)
		{
			this.removeEdge(e);
		}
		return edges.size();
	}

	/**
	 * Returns whether this graph contains the given edge.
	 *
	 * @param edge
	 * @return
	 */
	public boolean contains(final Edge edge)
	{
		boolean result = false;
		if (this.groundTruth.contains(edge.getSource().getGtCluster()))
		{
			result = edge.getSource().getGtCluster().contains(edge);
		}
		// reference clusters do not contain edges

		return result;
	}

	public boolean isConnected(final Pair<Node> nonEdge)
	{
		return this.contains(Edge.createEdge(nonEdge));
	}

	/**
	 * The number of undirected edges of this graph.
	 *
	 * @return the edge count
	 */
	public int getEdgeCount()
	{
		return this.pseudoCluster.getEdgeCount();
	}

	/**
	 * The number of undirected non-edges of this graph.
	 *
	 * @return the edge count
	 */
	public long getNonEdgeCount()
	{
		return this.pseudoCluster.getNonEdgeCount();
	}

	public int getInterClusterEdgeCount()
	{
		return this.pseudoCluster.getInterClusterEdgeCount();
	}

	public long getInterClusterNonEdgeCount()
	{
		return Edge.maxEdgeCount(this.getNodeCount()) //
			- this.maxIntraClusterEdgeCount
			- this.getInterClusterEdgeCount();
	}

	public int getIntraClusterEdgeCount()
	{
		return this.getEdgeCount() - this.getInterClusterEdgeCount();
	}

	/*
	 * *********************************************************************
	 * ***
	 * Node operations
	 * ***
	 * *********************************************************************
	 */

	/**
	 * Creates an isolated node and assigns it to a cluster.
	 *
	 * The cluster will be chosen according to the distribution of expected
	 * cluster size.
	 */
	public void addNode()
	{
		final Node node = new Node();
		final OrdinaryCluster cl = this.groundTruth.proposeClusterForNewNode();
		addNode(node, cl);

	}

	/**
	 * Creates a node, assigns it to a cluster and create the expected
	 * amount of edges.
	 *
	 * The cluster will be chosen according to the distribution of expected
	 * cluster size.
	 *
	 * @return the number of edges which have been created
	 */
	public int addAndConnectNode()
	{
		final Node node = new Node();
		final OrdinaryCluster cl = this.groundTruth.proposeClusterForNewNode();
		addNode(node, cl);
		return this.addExpectedEdges(node);
	}

	/**
	 * Adds a node to the given cluster.
	 *
	 * The node must not be contained in this graph and the cluster must be
	 * contained within the ground truth!
	 *
	 * @param node
	 *            the new node
	 * @param cluster
	 *            the cluster to contain the node
	 */
	void addNode(final Node node, final OrdinaryCluster cluster)
	{
		maxIntraClusterEdgeCount -= Edge.maxEdgeCount(cluster.getNodeCount());
		cluster.addNode(node);
		maxIntraClusterEdgeCount += Edge.maxEdgeCount(cluster.getNodeCount());

		int referenceIndex;
		/*
		 * If the cluster for the new node is involved in an operation
		 * the initial cluster(s) of the operation are still present in the
		 * reference clustering.
		 */
		if (cluster.isLocked())
		{
			final AbstractClusterOperation op = cluster.getCurrentOperation();
			final OrdinaryCluster referenceCluster = op.referenceClusterForNewNode();

			assert referenceCluster.isInReferenceClustering();

			referenceCluster.addNode(node);
			referenceIndex = referenceCluster.getJournalIndex();
			op.notifyNodeInsertion(node);

			assert Math.abs(node.getOperationIndex()) == op.getGlobalIndex();
		}
		else
		// the cluster is present in both clusterings
		{
			referenceIndex = cluster.getJournalIndex();

			groundTruth.notifyNodeAdded(cluster);
		}

		gJournal.createNodeOp(cluster.getJournalIndex(), referenceIndex);
		++this.smallScaleOperationCount;
		this.logAndPrintNodeInsertion(node);
	}

	/**
	 * Adds edges according to the expectation value.
	 *
	 * This method will randomly generate adjacencies to other nodes in the
	 * graph.
	 * It adheres to the expected intra- and inter-cluster edge
	 * probabilities.
	 *
	 * This method may only be called if the node is isolated!
	 *
	 * @param node
	 *            the node to be connected
	 *
	 * @return the degree of the node
	 */
	public int addExpectedEdges(final Node node)
	{
		final OrdinaryCluster cl = node.getGtCluster();

		/*
		 * Intra-cluster edges
		 */
		final List<Long> intraClPartners =
			Sequences.binomialSequence(cl.getPIn(), cl.getNodeCount() - 1);
		for (final long index : intraClPartners)
		{
			// mind not to create self loops!
			final Node other = cl.getNode(index >= node.getGtClIndex() ? (int) index + 1
				: (int) index);
			this.addEdge(new Pair<Node>(node, other));
		}

		/*
		 * Inter-cluster edges
		 */
		final List<Long> interClPartners =
			Sequences.binomialSequence(this.getPOut(), this.getNodeCount() - cl.getNodeCount());
		int c = 0;
		int previousBorder = 0;
		for (final long index : interClPartners)
		{
			long corrected = index - previousBorder;

			AbstractCluster otherCluster = this.groundTruth.getCluster(c);
			while (corrected >= otherCluster.getNodeCount() || otherCluster.equals(cl))
			{
				if (!otherCluster.equals(cl))
				{
					previousBorder += otherCluster.getNodeCount();

					corrected -= otherCluster.getNodeCount();
				}

				++c;
				assert c < this.groundTruth.getClusterCount();
				otherCluster = this.groundTruth.getCluster(c);
			}

			assert 0 <= corrected && corrected < otherCluster.getNodeCount() : "idx: " + index;

			final Node other = otherCluster.getNode((int) corrected);

			assert !other.equals(node);

			this.addEdge(new Pair<Node>(node, other));
		}

		return node.getDegree();
	}

	/**
	 * Selects an existing node and removes it together with its potential
	 * adjacent edges.
	 *
	 * This method will only have an effect if there are more nodes
	 * than clusters because no cluster may run out of nodes!
	 *
	 * @return the number of adjacent edges the node had
	 */
	public int removeNode()
	{
		if (this.getNodeCount() > this.getClusterCount())
		{
			Node victim = null;
			do
			{
				final int r = random.nextInt(this.pseudoCluster.getNodeCount());
				victim = this.pseudoCluster.getNode(r);
			}
			while (victim.getGtCluster().getNodeCount() == 1);

			return this.removeNode(victim);
		}
		else
		{
			this.logAndPrintNoNodeDeletion();
			return 0;
		}
	}

	/**
	 * Removes the given node from the graph.
	 *
	 * The graph must contain the node!
	 *
	 * @param node
	 *            the node to be removed
	 * @return the degree of the node just before the removal.
	 */
	int removeNode(final Node node)
	{
		final OrdinaryCluster gtCluster = node.getGtCluster();
		assert null != gtCluster : "v: " + node;
		assert gtCluster.getNodeCount() > 1 : "v: " + node;

		final int outdegree = node.getDegree();
		++this.smallScaleOperationCount;
		this.logAndPrintNodeDeletion(node);

		this.removeAllEdges(node);

		assert node.getDegree() == 0;

		if (gtCluster.isLocked())
		{
			assert 0 != node.getOperationIndex() : "v: " + node;

			final AbstractClusterOperation op = this.indexOperationMapping.get(node
				.getOperationIndex());

			assert null != op : "v: " + node;

			op.notifyNodeDeletion(node);
			node.getRefClCluster().removeNode(node);
		}
		else
		{
			assert 0 == node.getOperationIndex() : "v: " + node;

			this.groundTruth.notifyNodeRemoved(gtCluster);
		}

		maxIntraClusterEdgeCount -= Edge.maxEdgeCount(gtCluster.getNodeCount());
		gtCluster.removeNode(node);
		maxIntraClusterEdgeCount += Edge.maxEdgeCount(gtCluster.getNodeCount());

		this.gJournal.removeNodeOp(node.getJournalIndex());

		return outdegree;
	}

	public boolean contains(final Node node)
	{
		return null != node.getGtCluster() && node.getGtCluster().contains(node);
	}

	public int getNodeCount()
	{
		return this.pseudoCluster.getNodeCount();
	}

	/*
	 * *********************************************************************
	 * ***
	 * Cluster operations
	 * ***
	 * *********************************************************************
	 */

	/**
	 * Selects and splits a cluster.
	 *
	 * The cluster must have at least two nodes in order to assure
	 * that each of the split products is non-empty.
	 */
	public void split()
	{
		final OrdinaryCluster cluster = groundTruth.proposeClusterForSplit();

		if (cluster != null)
		{
			split(cluster);
		}
		else
		{
			this.logAndPrintNoSplit();
		}
	}

	/**
	 * Splits the given cluster.
	 *
	 * The cluster must have at least two nodes and it may not be involved
	 * in any other concurrent operation!
	 *
	 * @param cluster
	 *            the cluster to be split
	 */
	void split(final OrdinaryCluster cluster)
	{
		final SplitOperation splitOp = new SplitOperation(cluster, this);

		++this.largeScaleOperationCount;
		this.logAndPrintSplit(cluster);

		maxIntraClusterEdgeCount -= Edge.maxEdgeCount(cluster.getNodeCount());
		splitOp.start();

		final Pair<OrdinaryCluster> newClusters = splitOp.getResultingClusters();
		maxIntraClusterEdgeCount += Edge.maxEdgeCount(newClusters.getFirst().getNodeCount());
		maxIntraClusterEdgeCount += Edge.maxEdgeCount(newClusters.getSecond().getNodeCount());

		this.runningOperations.add(splitOp);
		this.indexOperationMapping.put(splitOp.getGlobalIndex(), splitOp);
		this.indexOperationMapping.put(-splitOp.getGlobalIndex(), splitOp);
	}

	/**
	 * Selects and merges a pair of clusters.
	 *
	 * This method will only take effect if there are two clusters not
	 * being
	 * involved in a running operation.
	 */
	public void merge()
	{
		final Pair<OrdinaryCluster> clusters = groundTruth.proposeClustersForMerge();

		if (null != clusters)
		{
			merge(clusters);
		}
		else
		{
			this.logAndPrintNoMerge();
		}
	}

	/**
	 * Merges the given clusters.
	 *
	 * The clusters may not be involved
	 * in any other concurrent operation!
	 *
	 * @param clusters
	 *            the cluster to be merged
	 */
	void merge(final Pair<OrdinaryCluster> clusters)
	{
		++this.largeScaleOperationCount;
		this.logAndPrintMerge(clusters);

		final MergeOperation mergeOp = new MergeOperation(clusters, this);


		maxIntraClusterEdgeCount -= Edge.maxEdgeCount(clusters.getFirst().getNodeCount());
		maxIntraClusterEdgeCount -= Edge.maxEdgeCount(clusters.getSecond().getNodeCount());

		mergeOp.start();

		maxIntraClusterEdgeCount += Edge.maxEdgeCount(mergeOp.getResultingCluster().getNodeCount());

		this.runningOperations.add(mergeOp);
		this.indexOperationMapping.put(mergeOp.getGlobalIndex(), mergeOp);
		this.indexOperationMapping.put(-mergeOp.getGlobalIndex(), mergeOp);
	}

	/**
	 * Checks every running operation for completeness and finishes if
	 * true.
	 */
	public void checkClusterOperationsForCompleteness()
	{
		final StringBuilder logMessage = new StringBuilder();
		logMessage.append("Checking for completeness...");

		final ArrayList<AbstractClusterOperation> remainingOps = new ArrayList<AbstractClusterOperation>();
		for (final AbstractClusterOperation op : this.runningOperations)
		{
			logMessage.append("\n\tOperation ");
			logMessage.append(op.getGlobalIndex());
			logMessage.append(" complete: ");

			if (op.isComplete())
			{
				logMessage.append("POSITIVE!");

				op.finish();

				this.indexOperationMapping.remove(op.getGlobalIndex());
				this.indexOperationMapping.remove(-op.getGlobalIndex());
			}
			else
			{
				logMessage.append("NEGATIVE!");
				remainingOps.add(op);
			}
		}
		logMessage.append("\n\tFinished: ");
		logMessage.append(this.runningOperations.size() - remainingOps.size());
		logMessage.append("\n\tStill running: ");
		logMessage.append(remainingOps.size());
		logMessage.append("\n\tGT: ");
		logMessage.append(this.groundTruth.getClusterCount());
		logMessage.append("\n\tRefCl: ");
		logMessage.append(this.referenceClustering.getClusterCount());

		Main.logAndPrintInfo(logMessage.toString(), VerbosityLevel.LEVEL_3);

		this.runningOperations = remainingOps;
		assert 2 * this.runningOperations.size() == this.indexOperationMapping.size() : //
		this.runningOperations.size() + "/=" + this.indexOperationMapping.size();

	}

	List<AbstractClusterOperation> getListOfOperations()
	{
		return this.runningOperations;
	}

	/**
	 * Manually adds a cluster to the graph (ground truth and reference
	 * clustering).
	 *
	 * The cluster may not contain any edges or nodes and the cluster shall
	 * not be contained in this graph yet!
	 *
	 * @param cluster
	 *            the empty cluster to be added
	 */
	void addCluster(final OrdinaryCluster cluster)
	{
		this.groundTruth.add(cluster);
		this.referenceClustering.add(cluster);

		maxIntraClusterEdgeCount += Edge.maxEdgeCount(cluster.getNodeCount());

		cluster.setInsertionTreeNode(this.insertionTree.insert(cluster.getInsertionWeight()));
		cluster.setDeletionTreeNode(this.deletionTree.insert(cluster.getDeletionWeight()));
	}

	/*
	 * *********************************************************************
	 * ***
	 * Clustering operations
	 * ***
	 * *********************************************************************
	 */

	PseudoCluster getPseudoCluster()
	{
		return this.pseudoCluster;
	}

	GroundTruth getGroundTruth()
	{
		return this.groundTruth;
	}

	ReferenceClustering getReferenceClustering()
	{
		return this.referenceClustering;
	}

	/**
	 * Returns the number of clusters in the ground truth clustering.
	 *
	 * @return the number of clusters in the ground truth (pseudo code:
	 *         'k')
	 */
	public int getClusterCount()
	{
		return this.groundTruth.getClusterCount();
	}

	/*
	 * *********************************************************************
	 * ***
	 * Journals
	 * ***
	 * *********************************************************************
	 */

	BinarySelectionTree getInsertionTree()
	{
		return this.insertionTree;
	}

	BinarySelectionTree getDeletionTree()
	{
		return this.deletionTree;
	}

	public GraphJournal getGraphJournal()
	{
		return this.gJournal;
	}

	public ClusteringJournal getClusteringJournal()
	{
		return this.clJournal;
	}

	private void logAndPrintTimeStep(final int currentTimeStep)
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("**Time: ");
			builder.append(currentTimeStep);
			builder.append("\n\tn: ");
			builder.append(this.getNodeCount());
			builder.append(" --- m: ");
			builder.append(this.getEdgeCount());
			builder.append(" --- k: ");
			builder.append(this.getClusterCount());
			Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintMerge(final Pair<OrdinaryCluster> clusters)
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("!!");
			builder.append(this.largeScaleOperationCount);
			builder.append(" Merge operation: ");
			builder.append(clusters);
			Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintNoMerge()
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			Main.logAndPrintInfo("??? No clusters to merge!", VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintSplit(final OrdinaryCluster cluster)
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("!!");
			builder.append(this.largeScaleOperationCount);
			builder.append(" Split operation: ");
			builder.append(cluster);
			Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_3);
		}
	}



	private void logAndPrintNoSplit()
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			Main.logAndPrintInfo("??? No cluster to split!", VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintEdgeInsertion(final Pair<Node> nonEdge)
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("#");
			builder.append(this.smallScaleOperationCount);
			builder.append(" Edge insertion: ");
			builder.append(nonEdge);
			Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintNoEdgeInsertion()
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			Main.logAndPrintInfo("??? No edge to insert!", VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintEdgeDeletion(final Edge edge)
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("#");
			builder.append(this.smallScaleOperationCount);
			builder.append(" Edge deletion: ");
			builder.append(edge);
			Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintNoEdgeDeletion()
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			Main.logAndPrintInfo("??? No edge to delete!", VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintNodeInsertion(final Node node)
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("#");
			builder.append(this.smallScaleOperationCount);
			builder.append(" Node deletion: ");
			builder.append(node);
			Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintNodeDeletion(final Node node)
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("#");
			builder.append(this.smallScaleOperationCount);
			builder.append(" Node deletion: ");
			builder.append(node);
			Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_3);
		}
	}

	private void logAndPrintNoNodeDeletion()
	{
		if (VerbosityLevel.LEVEL_3.getLevel() <= Main.getVerbosityLevel().getLevel()
			|| Main.isLoggingEnabled())
		{
			Main.logAndPrintInfo("??? No node to delete!", VerbosityLevel.LEVEL_3);
		}
	}

	public boolean isUsingTreemapInsteadOfHashmap()
	{
		return this.useTreeMapInsteadOfHashMap;
	}
}
