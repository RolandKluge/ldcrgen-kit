package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.BinarySelectionTree;
import edu.kit.iti.ldcrgen.io.journaling.ClusteringJournal;
import edu.kit.iti.ldcrgen.io.journaling.GraphJournal;
import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Pair;

public class MergeOperation extends AbstractClusterOperation
{

	private final RandomProvider random = new JavaUtilRandomProvider();

	private final OrdinaryCluster firstInitialCluster;
	private final OrdinaryCluster secondInitialCluster;

	private OrdinaryCluster resultingCluster;
	private double resultingPIn;
	private int expectedResultingSize;


	MergeOperation(final Pair<OrdinaryCluster> initialClusters,
			final DCRGraph graph)
	{
		super(graph);

		this.firstInitialCluster = initialClusters.getFirst();
		this.secondInitialCluster = initialClusters.getSecond();
	}

	public Pair<OrdinaryCluster> getInitialClusters()
	{
		return new Pair<OrdinaryCluster>(firstInitialCluster, secondInitialCluster);
	}

	public OrdinaryCluster getResultingCluster()
	{
		return this.resultingCluster;
	}

	@Override
	void start()
	{
		sampleClusterSizes();
		samplePIn();
		createNewCluster();

		this.applyUponTrees();

		this.markStartInJournals();
	}

	@Override
	boolean isComplete()
	{
		final double threshold = graph.getClusterOpThreshold() * expectedIntraCount
			+ (1 - graph.getClusterOpThreshold())
			* expectedInterCount;
		return this.edgesBetweenClusters >= threshold;
	}

	@Override
	boolean isRunning()
	{
		return this.equals(this.firstInitialCluster.getCurrentOperation()) &&
			this.equals(this.secondInitialCluster.getCurrentOperation()) &&
				this.equals(this.resultingCluster.getCurrentOperation());
	}

	@Override
	void finish()
	{
		this.unlockClusters();
		graph.getReferenceClustering().apply(this);
		this.markDoneInJournals();
	}

	private void samplePIn()
	{
		final PInSampler sampler = graph.getSamplerForNewPIn();
		final List<Double> pInValues = graph.getGroundTruth().getPIn();

		double result = -1.0;

		if (PInSampler.MEAN == sampler || pInValues.size() == 1)
		{
			result = ListUtils.arithmeticMean(pInValues);
		}
		else if (PInSampler.GAUSSIAN == sampler)
		{
			final double mean = ListUtils.arithmeticMean(pInValues);
			final double stddev = Math.sqrt(ListUtils.variance(pInValues));
			final Random random = new Random();
			final double pOut = this.graph.getPOut();


			do
			{
				result = stddev * random.nextGaussian() + mean;
			}
			while (result < pOut || result > 1);
		}
		else
		{
			assert false : "Invalid sampler or sampler not yet implemented!";
		}

		this.resultingPIn = result;
		this.pInSingleCluster = this.resultingPIn;
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
				this.expectedResultingSize = (int) (stddev * random.nextGaussian() + mean);
			}
			while (this.expectedResultingSize <= 0);

		}
		else
		{
			this.expectedResultingSize = clusterSizes.get(0);
		}
	}

	private void createNewCluster()
	{
		this.resultingCluster = new OrdinaryCluster(graph, this.resultingPIn,
			this.expectedResultingSize);

		/*
		 * Remember edges
		 */
		final List<Edge> storedEdges = new ArrayList<Edge>();
		final List<Edge> storedEdgesBetweenClusters = new ArrayList<Edge>();
		final Iterator<Edge> intraClusterEdgesOfFirst = firstInitialCluster
				.intraClusterEdgeIterator();
		while (intraClusterEdgesOfFirst.hasNext())
		{
			final Edge edge = intraClusterEdgesOfFirst.next();
			if (edge.getSource().getGlobalIndex() < edge.getTarget().getGlobalIndex())
			{
				storedEdges.add(edge);
			}
		}

		final Iterator<Edge> intraClusterEdgesOfSecond = secondInitialCluster
				.intraClusterEdgeIterator();
		while (intraClusterEdgesOfSecond.hasNext())
		{
			final Edge edge = intraClusterEdgesOfSecond.next();
			if (edge.getSource().getGlobalIndex() < edge.getTarget().getGlobalIndex())
			{
				storedEdges.add(edge);
			}
		}

		final Iterator<Edge> interClusterEdgesofFirst = this.firstInitialCluster
				.interClusterEdgeIterator();
		while (interClusterEdgesofFirst.hasNext())
		{
			final Edge edge = interClusterEdgesofFirst.next();
			storedEdges.add(edge);

			if (edge.isInterClusterEdge(this.firstInitialCluster, this.secondInitialCluster))
			{
				storedEdgesBetweenClusters.add(edge);
			}
		}
		this.edgesBetweenClusters = storedEdgesBetweenClusters.size();

		final Iterator<Edge> interClusterEdgesofSecond = this.secondInitialCluster
				.interClusterEdgeIterator();
		while (interClusterEdgesofSecond.hasNext())
		{
			final Edge edge = interClusterEdgesofSecond.next();
			if (!edge.isInterClusterEdge(this.firstInitialCluster, this.secondInitialCluster))
			{
				storedEdges.add(edge);
			}
		}


		final int interClusterEdgeCountBefore = //
		firstInitialCluster.getInterClusterEdgeCount()
			+ secondInitialCluster.getInterClusterEdgeCount()
				- edgesBetweenClusters;
		final int intraClusterEdgeCountBefore = //
		firstInitialCluster.getIntraClusterEdgeCount()
			+ secondInitialCluster.getIntraClusterEdgeCount();
		assert storedEdges.size() == intraClusterEdgeCountBefore + interClusterEdgeCountBefore : //
		"exp: "
			+ storedEdges.size() //
			+ " act: "
			+ (intraClusterEdgeCountBefore + interClusterEdgeCountBefore) + //
			" (edgesBetw: " + edgesBetweenClusters + ")";

		/*
		 * Relabel clusters and nodes and lock them
		 */
		graph.getGroundTruth().apply(this);

		assert resultingCluster.isInGroundTruth();
		graph.getGroundTruth().lock(resultingCluster, this);

		assert firstInitialCluster.isInReferenceClustering();
		firstInitialCluster.setCurrentOperation(this);

		assert secondInitialCluster.isInReferenceClustering();
		secondInitialCluster.setCurrentOperation(this);

		/*
		 * Insert nodes
		 */
		for (int v = 0; v < firstInitialCluster.getNodeCount(); ++v)
		{
			final Node node = firstInitialCluster.getNode(v);
			resultingCluster.addNodeLocally(node);
			node.setOperationIndex(-this.globalIndex);
		}

		for (int v = 0; v < secondInitialCluster.getNodeCount(); ++v)
		{
			final Node node = secondInitialCluster.getNode(v);
			resultingCluster.addNodeLocally(node);
			node.setOperationIndex(this.globalIndex);
		}

		/*
		 * Remove all edges from the initial clusters
		 * Changes to the clusters will only be local - no information
		 * in the pseudo cluster is affected.
		 * The graph is now in an INCONSISTENT state!
		 */
		firstInitialCluster.clearEdges();
		secondInitialCluster.clearEdges();

		/*
		 * Insert edges
		 */
		for (int e = 0; e < storedEdges.size(); ++e)
		{
			final Edge edge = storedEdges.get(e);
			assert !this.resultingCluster.contains(edge) : "edge: " + edge;
			resultingCluster.addEdgeLocally(edge);
		}

		/*
		 * Check conservation of edges
		 */
		final int interClusterEdgeCountAfter = resultingCluster.getInterClusterEdgeCount();
		final int intraClusterEdgeCountAfter = resultingCluster.getIntraClusterEdgeCount();
		assert interClusterEdgeCountAfter == interClusterEdgeCountBefore - edgesBetweenClusters;
		assert intraClusterEdgeCountAfter == intraClusterEdgeCountBefore + edgesBetweenClusters;

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

		insTree.delete(firstInitialCluster.getInsertionTreeNode());
		firstInitialCluster.clearInsertionTreeNode();
		insTree.delete(secondInitialCluster.getInsertionTreeNode());
		secondInitialCluster.clearInsertionTreeNode();

		delTree.delete(firstInitialCluster.getDeletionTreeNode());
		firstInitialCluster.clearDeletionTreeNode();
		delTree.delete(secondInitialCluster.getDeletionTreeNode());
		secondInitialCluster.clearDeletionTreeNode();

		resultingCluster.setInsertionTreeNode(//
			insTree.insert(resultingCluster.getInsertionWeight()));
		resultingCluster.setDeletionTreeNode(//
			delTree.insert(resultingCluster.getDeletionWeight()));
	}

	@Override
	protected void markStartInJournals()
	{
		final ClusteringJournal clJournal = graph.getClusteringJournal();
		final GraphJournal gJournal = graph.getGraphJournal();

		clJournal.mergeOp(//
			this.firstInitialCluster.getJournalIndex(), //
			this.secondInitialCluster.getJournalIndex(), //
			this.resultingCluster.getJournalIndex());

		for (int v = 0; v < this.resultingCluster.getNodeCount(); ++v)
		{
			gJournal.setClusterOp(this.resultingCluster.getNode(v).getJournalIndex(),
				this.resultingCluster.getJournalIndex());
		}
	}

	@Override
	protected void markDoneInJournals()
	{
		final ClusteringJournal clJournal = graph.getClusteringJournal();
		final GraphJournal gJournal = graph.getGraphJournal();

		clJournal.mergeDone(//
			this.firstInitialCluster.getJournalIndex(), //
			this.secondInitialCluster.getJournalIndex(), //
			this.resultingCluster.getJournalIndex());

		for (int v = 0; v < this.resultingCluster.getNodeCount(); ++v)
		{
			gJournal.setRefClusterOp(this.resultingCluster.getNode(v).getJournalIndex(),
				this.resultingCluster.getJournalIndex());
		}
	}

	@Override
	protected void unlockClusters()
	{
		final GroundTruth gt = graph.getGroundTruth();

		this.firstInitialCluster.clearCurrentOperation();
		this.secondInitialCluster.clearCurrentOperation();
		gt.unlock(this.resultingCluster);

		for (int v = 0; v < this.resultingCluster.getNodeCount(); ++v)
		{
			final Node node = this.resultingCluster.getNode(v);
			node.setOperationIndex(AbstractClusterOperation.INVALID_OP_INDEX);
		}


	}

	/**
	 * Weighted selection among clusters
	 */
	@Override
	OrdinaryCluster referenceClusterForNewNode()
	{
		assert isRunning();
		final int totalSize = firstInitialCluster.getNodeCount()
			+ secondInitialCluster.getNodeCount();
		return Math.random() * totalSize < firstInitialCluster.getNodeCount() //
		? firstInitialCluster
			: secondInitialCluster;
	}


	@Override
	protected void updateExpectedEdgeDensities()
	{
		final double maxEdgeCount = this.firstInitialCluster.getNodeCount()
					* this.secondInitialCluster.getNodeCount();
		this.expectedIntraCount = maxEdgeCount * this.pInSingleCluster;
		this.expectedInterCount = maxEdgeCount * this.graph.getPOut();
	}
}
