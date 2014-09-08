package edu.kit.iti.ldcrgen.data_structures;


/**
 * A cluster operation keeps track of all information which have to be
 * stored until the operation is considered to be 'complete'.
 *
 * There are three steps to perform:
 *
 * - generate the cluster operation(new ...Operation(...))
 *
 * - Start the operation (see {@link AbstractClusterOperation#start()})
 * This will create the resulting cluster(s) with all the data such as
 * nodes, edges, p_in values etc.
 * Furthermore, all changes will be applied upon the ground truth
 * clustering.
 * Finally, the started operations are written to the clustering journal.
 *
 * - Check the completeness of the operation
 *
 * - Finish the operation (see {@link AbstractClusterOperation#finish()}):
 * The caller has decided that the operation has finished so the operation
 * will be applied to the reference clustering and the respective
 * events will be written to the journal.
 *
 * Furthermore, as the relevant measures for determining if the operation
 * is
 * complete change over time, there are several methods for notifying the
 * operation.
 *
 * @author Roland Kluge
 */
public abstract class AbstractClusterOperation
{
	public static final int INVALID_OP_INDEX = 0;
	private static final int INITIAL_INDEX = 1;

	private static int counter = INITIAL_INDEX;

	protected final DCRGraph graph;

	protected int edgesBetweenClusters; // needed for calculating progress

	protected double expectedIntraCount;
	protected double expectedInterCount;

	protected double pInSingleCluster;

	protected final int globalIndex;


	protected AbstractClusterOperation(final DCRGraph graph)
	{
		this.globalIndex = counter++;
		this.graph = graph;
	}

	/**
	 * Unambiguously identifies this operation during the generation
	 * process.
	 *
	 * @return the global index of this operation
	 */
	public int getGlobalIndex()
	{
		return this.globalIndex;
	}

	/*
	 * 'Protocol' methods
	 */
	/**
	 * Starts the operation.
	 * Afterwards, the resulting cluster(s) will be contained in the
	 * ground truth and the original cluster(s) can still be found
	 * in the reference clustering.
	 */
	abstract void start();

	/**
	 * Checks whether the operation is complete
	 *
	 * @return whether the operation is considered complete
	 */
	abstract boolean isComplete();

	/**
	 * Whether the operation has already been finished.
	 * This is different to being complete as {@link #isComplete()} only
	 * checks whether the operation shall be considered to be complete but
	 * it does not actually change the state.
	 * The {@link #finish()} method, in turn, finishes all outstanding
	 * work.
	 *
	 * @return whether the operation has not been finished, yet
	 */
	abstract boolean isRunning();

	/**
	 * Cleans up all information concerning the operation.
	 *
	 * The operation lock will be removed from all concerned clusters and
	 * nodes.
	 */
	abstract void finish();

	abstract OrdinaryCluster referenceClusterForNewNode();

	/**
	 * Notifies this operation that the given edge has been deleted.
	 *
	 * The edge has to be involved in this operation which means
	 * that it connects two clusters which both participate in the
	 * same operation.
	 *
	 * @param edge
	 *            the deleted edge
	 */
	void notifyEdgeDeletion(final Edge edge)
	{
		assert this.equals(edge.getOperation()) : //
		"Bad notification for: " + edge;

		--this.edgesBetweenClusters;
	}

	/**
	 * Notifies this operation that the given node has been deleted.
	 *
	 * The node has to be involved in this operation which means that it is
	 * contained in one of the the participating clusters.
	 *
	 * @param node
	 *            the deleted node
	 */
	void notifyNodeDeletion(final Node node)
	{
		assert Math.abs(node.getOperationIndex()) == this.globalIndex : //
		"Bad notification for: " + node;

		this.updateExpectedEdgeDensities();
	}

	/**
	 * Notifies this operation that the given edge has been inserted.
	 *
	 * The edge has to be involved in this operation which means
	 * that it connects two clusters which both participate in the
	 * same operation.
	 *
	 * @param edge
	 *            the inserted edge
	 */
	void notifyEdgeInsertion(final Edge edge)
	{
		assert this.equals(edge.getOperation()) : //
		"Bad notification for: " + edge;

		++this.edgesBetweenClusters;
	}

	/**
	 * Notifies this operation that the given node has been inserted.
	 *
	 * The node has to be involved in this operation which means that it is
	 * contained in one of the the participating clusters.
	 *
	 * @param node
	 *            the inserted node
	 */
	void notifyNodeInsertion(final Node node)
	{
		assert Math.abs(node.getOperationIndex()) == this.globalIndex : //
		"Bad notification for: " + node;

		this.updateExpectedEdgeDensities();
	}

	/**
	 * Returns the number of edges which connect the two participating
	 * clusters.
	 *
	 * These are the edges between the original clusters in a merge
	 * operation and the edges between the resulting clusters in a split
	 * operation.
	 *
	 * @return the number of edges
	 */
	int getEdgeCountBetweenClusters()
	{
		return this.edgesBetweenClusters;
	}

	/*
	 * Use visitor pattern for modifying other data structures
	 */
	/**
	 * Removes the initial cluster(s) from the deletion and
	 * insertion trees and adds the resulting cluster to it.
	 */
	protected abstract void applyUponTrees();

	/**
	 * Writes notifications into the journal that
	 * the new clusters and their respective nodes
	 * are contained in the ground truth clustering, now.
	 */
	protected abstract void markStartInJournals();

	/**
	 * Writes notifications into the journal that
	 * the new clusters and their respective nodes
	 * are contained in the reference clustering, now.
	 */
	protected abstract void markDoneInJournals();

	/**
	 * Removes all locks from the participating clusters.
	 */
	protected abstract void unlockClusters();

	/**
	 * Updates the node count of the relevant
	 * pair of clusters.
	 */
	protected abstract void updateExpectedEdgeDensities();

}
