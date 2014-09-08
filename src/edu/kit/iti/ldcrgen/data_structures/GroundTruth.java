package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.List;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.BinarySelectionTree;
import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Pair;

/**
 * The ground truth contains the actual clustering of the graph.
 * All clusters within it take care of the graph's structure.
 *
 * @author Roland Kluge
 */
public class GroundTruth extends AbstractClustering
{
	final RandomProvider random = new JavaUtilRandomProvider();
	private BinarySelectionTree clusterSelectionTree;

	private final List<OrdinaryCluster> nonLockedClusters;
	private int nodeCountOfClustersInNonLockedList;

	public GroundTruth()
	{
		this.clusterSelectionTree = new BinarySelectionTree();
		this.nonLockedClusters = new ArrayList<OrdinaryCluster>();
	}

	@Override
	protected void assignLocalIndex(final OrdinaryCluster cluster, final int index)
	{
		cluster.setGroundTruthIndex(index);
	}

	@Override
	protected void clearIndex(final OrdinaryCluster cluster)
	{
		cluster.setGroundTruthIndex(AbstractCluster.INVALID_ID);
	}

	@Override
	protected int getLocalIndex(final OrdinaryCluster cluster)
	{
		return cluster.getGroundTruthIndex();
	}


	/*
	 * *********************************************************************
	 *
	 * Locking / Unlocking
	 *
	 * The ground truth keeps track of the current status of each
	 * of its contained clusters.
	 * Therefore, the add and remove methods need to update this
	 * information.
	 * *********************************************************************
	 */
	@Override
	void add(final OrdinaryCluster cluster)
	{
		super.add(cluster);
		cluster.setSelectionTreeNode(//
			this.clusterSelectionTree.insert(cluster.getExpectedSizeWeight()));


		addToNonLockedClusters(cluster);
	}

	@Override
	void remove(final OrdinaryCluster cluster)
	{
		super.remove(cluster);
		this.clusterSelectionTree.delete(cluster.getSelectionTreeNode());
		cluster.clearSelectionTreeNode();

		assert cluster.getIndexInListOfNonLockedClusters() < this.nonLockedClusters.size() : "cl: "
			+ cluster;
		removeFromUnlockedClusters(cluster);
	}

	public void lock(final OrdinaryCluster cluster, final AbstractClusterOperation op)
	{
		removeFromUnlockedClusters(cluster);
		cluster.setCurrentOperation(op);
		cluster.clearIndexInListOfNonLockedClusters();
	}

	public void unlock(final OrdinaryCluster cluster)
	{
		addToNonLockedClusters(cluster);
		cluster.clearCurrentOperation();
		cluster.setIndexInListOfNonLockedClusters(this.nonLockedClusters.size() - 1);
	}

	private void addToNonLockedClusters(final OrdinaryCluster cluster)
	{
		this.nonLockedClusters.add(cluster);
		nodeCountOfClustersInNonLockedList += cluster.getNodeCount();
		cluster.setIndexInListOfNonLockedClusters(this.nonLockedClusters.size() - 1);
	}

	private void removeFromUnlockedClusters(final OrdinaryCluster cluster)
	{
		final OrdinaryCluster lastUnused = ListUtils.last(this.nonLockedClusters);
		final int index = cluster.getIndexInListOfNonLockedClusters();

		nodeCountOfClustersInNonLockedList -= cluster.getNodeCount();

		ListUtils.moveLastTo(this.nonLockedClusters, index);

		lastUnused.setIndexInListOfNonLockedClusters(index);
		cluster.setIndexInListOfNonLockedClusters(AbstractCluster.INVALID_ID);

	}

	void notifyNodeAdded(final OrdinaryCluster cluster)
	{
		assert !cluster.isLocked();
		assert this.contains(cluster);

		++this.nodeCountOfClustersInNonLockedList;
	}

	void notifyNodeRemoved(final OrdinaryCluster cluster)
	{
		assert !cluster.isLocked();
		assert this.contains(cluster);

		--this.nodeCountOfClustersInNonLockedList;
	}

	/**
	 * Finds a cluster which is not involved in an operation.
	 * If no such cluster exists the result will be <code>null</code>.
	 *
	 * @return the proposed cluster
	 */
	OrdinaryCluster proposeClusterForSplit()
	{
		OrdinaryCluster result = null;
		// make sure there exists a non-locked cluster with at least *two*
		// nodes
		if (!this.nonLockedClusters.isEmpty()
			&& this.nodeCountOfClustersInNonLockedList >= this.nonLockedClusters.size() + 1)
		{
			do
			{
				final int r = random.nextInt(this.nonLockedClusters.size());
				result = this.nonLockedClusters.get(r);
			}
			while (result.getNodeCount() < 2);
		}
		return result;
	}

	/**
	 * Finds a pair of clusters which are not involved in an operation.
	 * If no such pair exists the result will be <code>null</code>.
	 *
	 * @return the proposed clusters
	 */
	Pair<OrdinaryCluster> proposeClustersForMerge()
	{
		Pair<OrdinaryCluster> result = null;
		if (this.nonLockedClusters.size() >= 2)
		{
			final Pair<Integer> indices = random.nextUnequalInts(this.nonLockedClusters.size());

			final OrdinaryCluster first = this.nonLockedClusters.get(indices.getFirst());
			final OrdinaryCluster second = this.nonLockedClusters.get(indices.getSecond());

			assert !first.equals(second) : "fst: " + first + " snd: " + second;
			result = new Pair<OrdinaryCluster>(first, second);
		}

		return result;
	}

	/**
	 * Returns a cluster which is suitable for inserting a node.
	 * The cluster is selected according to the distribution of expected
	 * sizes.
	 *
	 * @return the proposed cluster
	 */
	OrdinaryCluster proposeClusterForNewNode()
	{
		return (OrdinaryCluster) this.clusterSelectionTree.select().getElement().getObject();
	}
}
