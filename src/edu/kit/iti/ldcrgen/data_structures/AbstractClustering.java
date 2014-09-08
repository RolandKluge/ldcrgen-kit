package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.kit.iti.ldcrgen.util.ListUtils;

/**
 * Clusterings only accept new clusters if these clusters are not locked.
 * Furthermore, clusters may only be removed from the clustering if their
 * operation is finished.
 *
 * @author Roland Kluge
 */
public abstract class AbstractClustering
{
	protected final List<OrdinaryCluster> clusters;
	private final List<Double> pInList;
	private final List<Integer> expectedSizeList;

	private int hashCode = 31;

	AbstractClustering()
	{
		this.clusters = new ArrayList<OrdinaryCluster>();
		this.pInList = new ArrayList<Double>();
		this.expectedSizeList = new ArrayList<Integer>();
	}

	/**
	 * Returns whether the given cluster is stored in this clustering.
	 *
	 * The result will be <code>false</code> if the cluster is
	 * <code>null</code>.
	 *
	 * @param cluster
	 *            the cluster to be searched
	 * @return whether this clustering contains the given graph
	 */
	public boolean contains(final OrdinaryCluster cluster)
	{
		boolean result = false;

		if (null != cluster)
		{
			final int index = this.getLocalIndex(cluster);
			if (0 <= index && index <= this.clusters.size())
			{
				result = this.clusters.get(index).equals(cluster);
			}
		}
		return result;
	}

	/**
	 * Adds the given non-locked cluster to this clustering.
	 *
	 * This clustering shall not contain the cluster yet!
	 *
	 * @param cluster
	 *            the new cluster
	 */
	void add(final OrdinaryCluster cluster)
	{
		this.clusters.add(cluster);
		this.pInList.add(cluster.getPIn());
		this.expectedSizeList.add(cluster.getExpectedSize());

		final int localIndexOfCluster = clusters.size() - 1;
		this.assignLocalIndex(cluster, localIndexOfCluster);

		this.hashCode += cluster.hashCode();

	}

	/**
	 * Removes the given non-locked cluster from this clustering.
	 *
	 * The clustering has to contain the cluster!
	 *
	 * @param cluster
	 *            the cluster
	 */
	void remove(final OrdinaryCluster cluster)
	{
		final int index = this.getLocalIndex(cluster);

		final OrdinaryCluster last = ListUtils.last(this.clusters);

		ListUtils.moveLastTo(this.clusters, index);
		ListUtils.moveLastTo(this.pInList, index);
		ListUtils.moveLastTo(this.expectedSizeList, index);

		this.assignLocalIndex(last, index);
		this.clearIndex(cluster);

		this.hashCode -= cluster.hashCode();
	}

	/**
	 * Applies the given merge operation upon this clustering.
	 * This operation is equivalent to removing the initial clusters and
	 * adding the resulting cluster.
	 *
	 * @param operation
	 *            the operation to apply
	 */
	void apply(final MergeOperation operation)
	{
		this.remove(operation.getInitialClusters().getFirst());
		this.remove(operation.getInitialClusters().getSecond());
		this.add(operation.getResultingCluster());
	}

	/**
	 * Applies the given merge operation upon this clustering.
	 * This operation is equivalent to removing the initial cluster and
	 * adding the resulting clusters.
	 *
	 * @param operation
	 *            the operation to apply
	 */
	void apply(final SplitOperation operation)
	{
		this.remove(operation.getInitialCluster());
		this.add(operation.getResultingClusters().getFirst());
		this.add(operation.getResultingClusters().getSecond());
	}

	int getClusterCount()
	{
		return this.clusters.size();
	}

	/**
	 * Returns the list of intra-cluster edge probabilities ('p_in').
	 *
	 * @return the intra-cluster edge probabilities
	 */
	List<Double> getPIn()
	{
		return Collections.unmodifiableList(this.pInList);
	}

	List<Integer> getExpectedSizes()
	{
		return Collections.unmodifiableList(this.expectedSizeList);
	}

	/**
	 * Returns the cluster with the given local index.
	 * The index has to be in the range of 0 to getClusterCount()
	 * (exclusive).
	 *
	 * @param index
	 *            the index of the cluster
	 * @return the cluster
	 */
	OrdinaryCluster getCluster(final int index)
	{
		return this.clusters.get(index);
	}

	/**
	 * Assigns the new index to the given cluster.
	 *
	 * This method distinguishes between ground truth and reference
	 * clustering.
	 *
	 * @param cluster
	 *            the cluster to be modified
	 * @param index
	 *            the new index of the cluster
	 */
	protected abstract void assignLocalIndex(final OrdinaryCluster cluster, final int index);

	/**
	 * Invalidates the index correlated to this clustering.
	 * May furthermore notify the cluster to invalidate any related
	 * information.
	 */
	protected abstract void clearIndex(final OrdinaryCluster cluster);

	/**
	 * Retrieves the index of the given cluster.
	 *
	 * This method distinguishes between ground truth and reference
	 * clustering.
	 *
	 * @param cluster
	 *            the cluster to be searched
	 */
	protected abstract int getLocalIndex(final OrdinaryCluster cluster);


	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	/**
	 * Two clusterings are equal if they contain the same clusters.
	 * The order is unimportant.
	 *
	 * @param obj
	 *            the other object
	 */
	@Override
	public final boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof AbstractClustering))
			return false;
		final AbstractClustering other = (AbstractClustering) obj;
		return other.clusters.containsAll(this.clusters)
			&& this.clusters.containsAll(other.clusters);
	}


}
