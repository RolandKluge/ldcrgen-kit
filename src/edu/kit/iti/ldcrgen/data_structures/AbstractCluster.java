package edu.kit.iti.ldcrgen.data_structures;

import java.util.Iterator;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.TreeNode;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.Weightable;
import edu.kit.iti.ldcrgen.util.Pair;

/**
 * This abstract class contains the common aspects of ordinary and pseudo
 * clusters.
 * For example, both possess a weight in the insertion and deletion tree.
 *
 * As the concept of a pseudo cluster differs from an ordinary cluster
 * care must be taken when querying e.g. the edge count of the cluster.
 *
 * <br/>
 * <p>
 * <b>Caution</b> As this is an internal data structure, the preconditions
 * in the comments have to be followed. Otherwise, even though the data
 * structure will still seem to work, it will be deeply corrupted and not
 * work properly anymore!
 * </p>
 *
 * @author Roland Kluge
 *
 */
public abstract class AbstractCluster
{
	public static final int INVALID_ID = -1;

	protected final FisherYatesShuffle shuffle;
	protected final DCRGraph graph;

	private double insWeightValue;
	private TreeNode insTreeNode;

	private double delWeightValue;
	private TreeNode delTreeNode;

	AbstractCluster(final DCRGraph graph)
	{
		if (null == graph)
			throw new IllegalArgumentException("Graph must not be null!");

		this.graph = graph;
		this.shuffle = new FisherYatesShuffle(0, graph.isUsingTreemapInsteadOfHashmap());
	}

	/*
	 * *********************************************************************
	 *
	 * Node operations
	 *
	 * *********************************************************************
	 */
	/**
	 * Returns the localID of the given node
	 * in the cluster.
	 *
	 * The method will return a negative index in case the given node is
	 * not contained in this cluster.
	 *
	 * @param node
	 *            the node
	 * @param its
	 *            index or a negative value if it is not contained in this
	 *            cluster
	 */
	abstract int getLocalID(final Node node);

	/**
	 * Adds a node to this cluster.
	 *
	 * The node shall not be contained in the cluster, yet!
	 *
	 * @param node
	 *            the node to be added
	 */
	abstract void addNode(final Node node);

	/**
	 * Removes a node from this cluster.
	 *
	 * The node shall be contained in the cluster and it must be isolated!
	 *
	 * @param node
	 *            the node to be removed
	 */
	abstract void removeNode(final Node node);

	/**
	 * Returns the number of nodes of this cluster.
	 *
	 * @return the number of nodes of this cluster
	 */
	abstract int getNodeCount();

	/**
	 * This method returns the node at the local index in this cluster.
	 *
	 * The index has to be in the range of 0 to getNodeCount() (exclusive).
	 *
	 * @param localID
	 *            the local ID of the node to be returned
	 * @return the node
	 */
	abstract Node getNode(final int localID);

	/**
	 * Returns an iterator over all stored nodes.
	 *
	 * @return iterator over nodes
	 */
	abstract Iterator<Node> nodeIterator();

	/**
	 * Returns whether the given node is contained in this cluster.
	 *
	 * @param node
	 *            the node to be checked
	 * @return whether this cluster contains the node
	 */
	abstract boolean contains(final Node node);

	/**
	 * Convenience method which returns the nodes for the given local
	 * indices.
	 *
	 * The result is the same as calling getNode() for each of the indices
	 * and
	 * afterwards creating a pair from the nodes.
	 *
	 * The indices both have to fulfill the preconditions of getNode().
	 *
	 * @param localNodeIDs
	 *            the indices
	 * @return the nodes
	 */
	Pair<Node> getNodes(final Pair<Integer> localNodeIDs)
	{
		return new Pair<Node>(//
			this.getNode(localNodeIDs.getFirst()),//
			this.getNode(localNodeIDs
				.getSecond()));
	}

	/*
	 * *********************************************************************
	 *
	 * Edge operations
	 *
	 * *********************************************************************
	 */
	/**
	 * Adds the given edge to the cluster.
	 *
	 * The cluster shall not contain the edge, yet!
	 *
	 * @param edge
	 *            the new edge
	 */
	abstract void addEdge(final Edge edge);

	/**
	 * Returns a contained edge which is selected uniformly at random
	 * from all feasible edges.
	 *
	 * Feasibility in this context differs very much depending on the type
	 * of the cluster (see appropriate comments)!
	 *
	 * This method may only be called if there exists at least one feasible
	 * edge in the cluster.
	 *
	 * @return a contained edge
	 */
	abstract Edge findEdge();

	/**
	 * Remove the given edge from the cluster.
	 *
	 * The cluster shall contain the edge!
	 *
	 * @param edge
	 *            the edge to be removed
	 */
	abstract void removeEdge(final Edge edge);

	/**
	 * Returns whether the given edge is contained in this cluster.
	 *
	 * For the different types of clusters, this method has varying
	 * semantics!
	 *
	 * @param edge
	 *            the edge to be checked
	 * @return whether this cluster contains the edge
	 */
	abstract boolean contains(final Edge edge);

	/**
	 * Returns the graph to which this cluster belongs.
	 *
	 * @return the containing graph
	 */
	DCRGraph getGraph()
	{
		return this.graph;
	}

	/**
	 * Returns the edges count of this cluster.
	 *
	 * For ordinary clusters C this is the number of edges in the
	 * node-induced subgraph
	 * of C.
	 * For the pseudo cluster this number is equal to the number of all
	 * edges in the
	 * graph.
	 *
	 * @return the edge count
	 */
	int getEdgeCount()
	{
		return (int) this.shuffle.getSelectionCount();
	}

	/**
	 * Returns a pair of nodes within this cluster which are not connected
	 * by an edge.
	 *
	 * @return an unconnected pair of nodes or <code>null</code> if no such
	 *         pair exists
	 */
	Pair<Node> findNonEdge()
	{
		Pair<Node> result = null;
		if (!this.shuffle.isFull())
		{
			final long proposedIndex = this.shuffle.proposeForSelection();
			final Pair<Integer> localNodeIndices = Edge.nodeIndices(proposedIndex);

			result = this.getNodes(localNodeIndices);
		}
		return result;
	}

	/**
	 * Returns the local id of the edge.
	 *
	 * The index is guaranteed to be positive.
	 *
	 * The edge must be contained in this cluster.
	 *
	 * @param edge
	 *            the edge contained in this cluster
	 * @return the positive local index of the edge
	 */
	long getLocalID(final Edge edge)
	{
		final Node source = edge.getSource();
		final Node target = edge.getTarget();

		return Math.abs(Edge.edgeIndex(this.getLocalID(source), this.getLocalID(target)));
	}

	/*
	 * *********************************************************************
	 *
	 * Operations and data structures concerning the cluster trees
	 *
	 * *********************************************************************
	 */

	/**
	 * Recalculates the tree weights of this cluster.
	 */
	abstract void updateTreeWeights();

	/*
	 * Insertion tree
	 */
	/**
	 * Returns the weight used in the insertion tree.
	 */
	Weightable getInsertionWeight()
	{
		return new Weightable()
		{
			@Override
			public double getWeight()
			{
				return insWeightValue;
			}

			@Override
			public Object getObject()
			{
				return AbstractCluster.this;
			}
		};
	}

	void setInsertionTreeNode(final TreeNode node)
	{
		if (null == node)
			throw new IllegalArgumentException("Tree node must not be null!");

		this.insTreeNode = node;
	}


	void clearInsertionTreeNode()
	{
		this.insTreeNode = null;
	}

	TreeNode getInsertionTreeNode()
	{
		return this.insTreeNode;
	}

	protected void updateInsertionWeight(final double value)
	{
		if (value < 0)
			throw new IllegalArgumentException("Tree weights must be positive but was: " + value);

		this.insWeightValue = value;
		if (null != this.insTreeNode)
		{
			this.insTreeNode.fireWeightChanged();
		}
	}

	/*
	 * Deletion tree
	 */
	/**
	 * Returns the weight used in the deletion tree.
	 */
	Weightable getDeletionWeight()
	{
		return new Weightable()
		{
			@Override
			public double getWeight()
			{
				return delWeightValue;
			}

			@Override
			public Object getObject()
			{
				return AbstractCluster.this;
			}
		};
	}

	void setDeletionTreeNode(final TreeNode node)
	{
		if (null == node)
			throw new IllegalArgumentException("Tree node must not be null!");

		this.delTreeNode = node;
	}

	void clearDeletionTreeNode()
	{
		this.delTreeNode = null;
	}

	TreeNode getDeletionTreeNode()
	{
		return this.delTreeNode;
	}

	protected void updateDeletionWeight(final double value)
	{
		if (value < 0)
			throw new IllegalArgumentException("Tree weights must be positive but was: " + value);

		this.delWeightValue = value;
		if (null != this.delTreeNode)
		{
			this.delTreeNode.fireWeightChanged();
		}
	}
}
