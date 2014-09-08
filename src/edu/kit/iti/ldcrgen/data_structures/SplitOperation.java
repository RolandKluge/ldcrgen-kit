package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.BinarySelectionTree;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.TreeNode;
import edu.kit.iti.ldcrgen.io.journaling.ClusteringJournal;
import edu.kit.iti.ldcrgen.io.journaling.GraphJournal;
import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Pair;

public class SplitOperation extends AbstractClusterOperation
{
	private final RandomProvider random = new JavaUtilRandomProvider();
	private final OrdinaryCluster initialCluster;

	private OrdinaryCluster firstResultingCluster;
	private double firstResultingPIn;
	private int firstExpectedSize;

	private OrdinaryCluster secondResultingCluster;
	private double secondResultingPIn;
	private int secondExpectedSize;

	SplitOperation(final OrdinaryCluster initialCluster, final DCRGraph graph)
	{
		super(graph);

		this.pInSingleCluster = initialCluster.getPIn();
		this.initialCluster = initialCluster;
	}

	public Pair<OrdinaryCluster> getResultingClusters()
	{
		return new Pair<OrdinaryCluster>(firstResultingCluster, secondResultingCluster);
	}

	public OrdinaryCluster getInitialCluster()
	{
		return this.initialCluster;
	}

	@Override
	void start()
	{
		samplePIn();
		sampleClusterSizes();
		createNewClusters();

		this.applyUponTrees();

		this.markStartInJournals();
	}

	@Override
	boolean isComplete()
	{
		final double threshold = graph.getClusterOpThreshold() * expectedIntraCount
			+ (1 - graph.getClusterOpThreshold())
			* expectedInterCount;
		return this.edgesBetweenClusters <= threshold;
	}

	@Override
	boolean isRunning()
	{
		return this.equals(this.initialCluster.getCurrentOperation()) &&
			this.equals(this.firstResultingCluster.getCurrentOperation()) &&
			this.equals(this.secondResultingCluster.getCurrentOperation());
	}

	@Override
	void finish()
	{
		this.unlockClusters();
		graph.getReferenceClustering().apply(this);

		this.markDoneInJournals();
	}

	/*
	 * Calculates the new expected size of the clusters.
	 * The calculation uses a normal distribution.
	 */
	private void sampleClusterSizes()
	{
		final List<Integer> clusterSizes = graph.getGroundTruth().getExpectedSizes();
		if (clusterSizes.size() > 1)
		{
			final List<Double> doubleSizes = ListUtils.mapType(clusterSizes, Double.class);
			final double mean = ListUtils.arithmeticMean(doubleSizes);
			final double stddev = Math.sqrt(ListUtils.variance(doubleSizes));

			do
			{
				this.firstExpectedSize = (int) (stddev * random.nextGaussian() + mean);
			}
			while (this.firstExpectedSize <= 0);

			do
			{
				this.secondExpectedSize = (int) (stddev * random.nextGaussian() + mean);
			}
			while (this.secondExpectedSize <= 0);
		}
		else
		{
			final int origSize = clusterSizes.get(0);
			this.firstExpectedSize = origSize;
			this.secondExpectedSize = origSize;
		}
	}

	/*
	 * Determines the p_in values for the new clusters.
	 */
	private void samplePIn()
	{
		final PInSampler sampler = graph.getSamplerForNewPIn();
		final List<Double> pInValues = graph.getGroundTruth().getPIn();

		if (PInSampler.MEAN == sampler || pInValues.size() == 1)
		{
			this.firstResultingPIn = initialCluster.getPIn();
			this.secondResultingPIn = initialCluster.getPIn();
		}
		else if (PInSampler.GAUSSIAN == sampler)
		{
			final double mean = ListUtils.arithmeticMean(pInValues);
			final double stddev = pInValues.size() > 1 //
			? Math.sqrt(ListUtils.variance(pInValues)) //
				: Math.abs(pInValues.get(0) - mean);
			final Random random = new Random();

			final double pOut = this.graph.getPOut();

			do
			{
				this.firstResultingPIn = stddev * random.nextGaussian() + mean;
			}
			while (this.firstResultingPIn < pOut || this.firstResultingPIn > 1);

			do
			{
				this.secondResultingPIn = stddev * random.nextGaussian() + mean;
			}
			while (this.secondResultingPIn < pOut || this.secondResultingPIn > 1);
		}
		else
		{
			assert false : "Invalid sampler or sampler not yet implemented!";
		}
	}

	private void createNewClusters()
	{
		firstResultingCluster = new OrdinaryCluster(graph, this.firstResultingPIn,
			this.firstExpectedSize);
		secondResultingCluster = new OrdinaryCluster(graph, this.secondResultingPIn,
			this.secondExpectedSize);

		/*
		 * Save intra-cluster edges of initial cluster.
		 * As the iterator returns edges for both directions, only one of
		 * them is actually added.
		 */
		final ArrayList<Edge> storedEdges = new ArrayList<Edge>();
		final Iterator<Edge> intraClusterEdgeIter = initialCluster.intraClusterEdgeIterator();
		while (intraClusterEdgeIter.hasNext())
		{
			final Edge edge = intraClusterEdgeIter.next();
			if (edge.getSource().getGlobalIndex() < edge.getTarget().getGlobalIndex())
			{
				storedEdges.add(edge);
			}
		}

		// inter-cluster edges
		ListUtils.appendTo(initialCluster.interClusterEdgeIterator(), storedEdges);

		final int interClusterEdgeCountBefore = initialCluster.getInterClusterEdgeCount();
		final int intraClusterEdgeCountBefore = initialCluster.getIntraClusterEdgeCount();
		assert storedEdges.size() == intraClusterEdgeCountBefore
			+ interClusterEdgeCountBefore;


		// relabel the nodes so that they belong to their new clusters
		final GroundTruth groundTruth = graph.getGroundTruth();
		groundTruth.apply(this);

		assert initialCluster.isInReferenceClustering();
		assert !initialCluster.isInGroundTruth();
		initialCluster.setCurrentOperation(this);

		assert firstResultingCluster.isInGroundTruth();
		groundTruth.lock(firstResultingCluster, this);

		assert secondResultingCluster.isInGroundTruth();
		groundTruth.lock(secondResultingCluster, this);

		/*
		 * Distribute nodes
		 * via weighted selection
		 */
		final int totalExpectedSize = this.firstExpectedSize + this.secondExpectedSize;
		final Iterator<Node> iter = initialCluster.nodeIterator();

		// Each cluster gets at least one node
		assert iter.hasNext();

		final Node fstBasicNode = iter.next();
		firstResultingCluster.addNodeLocally(fstBasicNode);
		fstBasicNode.setOperationIndex(this.globalIndex);

		assert iter.hasNext();

		final Node sndBasicNode = iter.next();
		secondResultingCluster.addNodeLocally(sndBasicNode);
		sndBasicNode.setOperationIndex(-this.globalIndex);

		while (iter.hasNext())
		{
			final Node node = iter.next();
			if (random.nextInt(totalExpectedSize) < this.firstExpectedSize)
			{
				this.firstResultingCluster.addNodeLocally(node);
				node.setOperationIndex(this.globalIndex);
			}
			else
			{
				this.secondResultingCluster.addNodeLocally(node);
				node.setOperationIndex(-this.globalIndex);
			}
		}

		/*
		 * Remove all edges from the initial cluster
		 * Changes to the cluster will only be local - no information
		 * in the pseudo cluster is affected.
		 * The graph is now in an INCONSISTENT state!
		 */
		initialCluster.clearEdges();

		/*
		 * Add edges to the corresponding clusters
		 */
		final List<Edge> storedEdgesBetweenClusters = new ArrayList<Edge>();
		for (final Edge edge : storedEdges)
		{
			// edge.getCluster() does not work because the resulting
			// cluster may not be involved!
			final Pair<OrdinaryCluster> clusters = edge.getClusters();

			int pos = clusters.find(firstResultingCluster);
			if (pos < 0)
			{
				pos = clusters.find(secondResultingCluster);
			}
			assert pos >= 0 : "cls: " + clusters;
			final OrdinaryCluster cluster = clusters.get(pos);

			assert !cluster.contains(edge) : "dup: " + edge;
			cluster.addEdgeLocally(edge);

			if (edge.isInterClusterEdge(firstResultingCluster, secondResultingCluster))
			{
				final OrdinaryCluster other = clusters.getOther(cluster);

				other.addEdgeLocally(edge);

				storedEdgesBetweenClusters.add(edge);
			}
		}
		this.edgesBetweenClusters = storedEdgesBetweenClusters.size();

		/*
		 * Check conservation of edges
		 */
		final int interClusterEdgeCountAfter = firstResultingCluster.getInterClusterEdgeCount()
			+ secondResultingCluster.getInterClusterEdgeCount();
		final int intraClusterEdgeCountAfter = firstResultingCluster.getIntraClusterEdgeCount()
			+ secondResultingCluster.getIntraClusterEdgeCount();

		assert interClusterEdgeCountAfter == interClusterEdgeCountBefore
			+ 2 * edgesBetweenClusters : "exp: "
			+ (interClusterEdgeCountBefore + edgesBetweenClusters)
			+ " - act: " + interClusterEdgeCountAfter;
		assert intraClusterEdgeCountAfter == intraClusterEdgeCountBefore
			- edgesBetweenClusters : "exp: " + (intraClusterEdgeCountBefore - edgesBetweenClusters)
			+ " - act: " + intraClusterEdgeCountAfter;

		/*
		 * Update dependend information
		 */
		this.firstResultingCluster.updateTreeWeights();
		this.secondResultingCluster.updateTreeWeights();

		// we only need to check the edges which have now become
		// inter-cluster edges
		for (final Edge edge : storedEdgesBetweenClusters)
		{
			graph.getPseudoCluster().updateStatus(edge);
		}

		/*
		 * Calculate expected edge counts
		 */
		updateExpectedEdgeDensities();
	}

	@Override
	protected void applyUponTrees()
	{
		final BinarySelectionTree insTree = graph.getInsertionTree();
		final BinarySelectionTree delTree = graph.getDeletionTree();

		/*
		 * Insertion tree
		 */
		insTree.delete(initialCluster.getInsertionTreeNode());
		initialCluster.clearInsertionTreeNode();

		final TreeNode firstNewInsNode =
				insTree.insert(firstResultingCluster.getInsertionWeight());
		firstResultingCluster.setInsertionTreeNode(firstNewInsNode);

		final TreeNode secondNewInsNode =
				insTree.insert(secondResultingCluster.getInsertionWeight());
		secondResultingCluster.setInsertionTreeNode(secondNewInsNode);

		/*
		 * Insertion tree
		 */
		delTree.delete(initialCluster.getDeletionTreeNode());
		initialCluster.clearDeletionTreeNode();

		final TreeNode firstNewDelNode =
				delTree.insert(firstResultingCluster.getDeletionWeight());
		firstResultingCluster.setDeletionTreeNode(firstNewDelNode);

		final TreeNode secondNewDelNode =
				delTree.insert(secondResultingCluster.getDeletionWeight());
		secondResultingCluster.setDeletionTreeNode(secondNewDelNode);
	}

	@Override
	protected void markStartInJournals()
	{
		final ClusteringJournal clJournal = graph.getClusteringJournal();
		final GraphJournal gJournal = graph.getGraphJournal();

		clJournal.splitOp(//
			this.initialCluster.getJournalIndex(), //
			this.firstResultingCluster.getJournalIndex(), //
			this.secondResultingCluster.getJournalIndex());

		for (int v = 0; v < this.firstResultingCluster.getNodeCount(); ++v)
		{
			gJournal.setClusterOp(//
				this.firstResultingCluster.getNode(v).getJournalIndex(),
				this.firstResultingCluster.getJournalIndex());
		}

		for (int v = 0; v < this.secondResultingCluster.getNodeCount(); ++v)
		{
			gJournal.setClusterOp(//
				this.secondResultingCluster.getNode(v).getJournalIndex(),
				this.secondResultingCluster.getJournalIndex());
		}
	}

	@Override
	protected void markDoneInJournals()
	{
		final ClusteringJournal clJournal = graph.getClusteringJournal();
		final GraphJournal gJournal = graph.getGraphJournal();

		clJournal.splitDone(//
			this.initialCluster.getJournalIndex(), //
			this.firstResultingCluster.getJournalIndex(), //
			this.secondResultingCluster.getJournalIndex());

		for (int v = 0; v < this.firstResultingCluster.getNodeCount(); ++v)
		{
			gJournal.setRefClusterOp(//
				this.firstResultingCluster.getNode(v).getJournalIndex(),
				this.firstResultingCluster.getJournalIndex());
		}

		for (int v = 0; v < this.secondResultingCluster.getNodeCount(); ++v)
		{
			gJournal.setRefClusterOp(//
				this.secondResultingCluster.getNode(v).getJournalIndex(),
				this.secondResultingCluster.getJournalIndex());
		}
	}

	@Override
	protected void unlockClusters()
	{
		final GroundTruth gt = graph.getGroundTruth();

		initialCluster.clearCurrentOperation();

		for (int v = 0; v < this.firstResultingCluster.getNodeCount(); ++v)
		{
			this.firstResultingCluster.getNode(v).setOperationIndex(
				AbstractClusterOperation.INVALID_OP_INDEX);
		}
		gt.unlock(this.firstResultingCluster);

		for (int v = 0; v < this.secondResultingCluster.getNodeCount(); ++v)
		{
			this.secondResultingCluster.getNode(v).setOperationIndex(
				AbstractClusterOperation.INVALID_OP_INDEX);
		}
		gt.unlock(this.secondResultingCluster);
	}

	@Override
	OrdinaryCluster referenceClusterForNewNode()
	{
		assert this.isRunning();
		return this.initialCluster;
	}

	@Override
	protected void updateExpectedEdgeDensities()
	{
		final double maxEdgeCount = this.firstResultingCluster.getNodeCount()
					* this.secondResultingCluster.getNodeCount();
		this.expectedIntraCount = maxEdgeCount * this.pInSingleCluster;
		this.expectedInterCount = maxEdgeCount * this.graph.getPOut();
	}
}
