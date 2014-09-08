package edu.kit.iti.ldcrgen.data_structures;

import java.util.Iterator;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.TreeNode;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.Weightable;
import edu.kit.iti.ldcrgen.util.BooleanUtils;
import edu.kit.iti.ldcrgen.util.Pair;

/**
 * <p>
 * The ordinary cluster is fully responsible of the nodes which it contains
 * as long as it is contained in the ground truth. The cluster loses its
 * membership in the ground truth as soon as it is involved in a cluster
 * operation. It will never be member of the ground truth again!
 * </p>
 *
 * <p>
 * A cluster in the reference clustering shall not contain any edges.
 * </p>
 * <p>
 * The cluster will furthermore take care of the management of its
 * contained nodes and edges concerning the pseudo cluster.
 * </p>
 * <p>
 * As each unweighted edges corresponds to two weighted edge in our model,
 * one edge may be "spread" over two clusters. The cluster containing the
 * node with smaller global index is the one which is 'responsible' of it.
 * </p>
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
 */
public class OrdinaryCluster extends AbstractCluster
{
	private static final int INITIAL_COUNTER = 0;
	private static final int DEFAULT_EXPECTED_SIZE = 1;

	private static int counter = INITIAL_COUNTER;

	private int gtIndex;
	private int refIndex;
	private final double pIn;

	private AbstractClusterOperation currentOperation;
	private int indexInListOfNonLockedClusters;
	private ClusterAdjacencyList adjacencyList;

	private final int globalIndex;
	private int expectedSize;
	private TreeNode selectionTreeNode;


	/**
	 * Creates a cluster which has not been assigned to any clustering,
	 * yet.
	 *
	 * The expected size of this cluster is set to 1.
	 *
	 * @param graph
	 *            the graph of this cluster
	 * @param pIn
	 *            intra-cluster edge probability of this cluster
	 */
	OrdinaryCluster(final DCRGraph graph, final double pIn)
	{
		super(graph);

		final double pOut = this.graph.getPOut();
		if (pIn < pOut)
			throw new IllegalArgumentException("Intra-cl probability " + pIn + " is smaller than inter-cl probability " + pOut);

		this.globalIndex = counter++;
		this.gtIndex = INVALID_ID;
		this.refIndex = INVALID_ID;
		this.indexInListOfNonLockedClusters = INVALID_ID;

		this.pIn = pIn;
		this.expectedSize = DEFAULT_EXPECTED_SIZE;
		this.currentOperation = null;
		this.adjacencyList = new ClusterAdjacencyList(this);
	}

	/**
	 * Creates a cluster with its expected size.
	 *
	 * @param gtIndex
	 *            the index in the ground truth
	 * @param gtIndex
	 *            the index in the reference clustering
	 * @param pIn
	 *            intra-cluster edge probability of this cluster
	 */
	OrdinaryCluster(final DCRGraph graph, final double pIn, final int expectedSize)
	{
		this(graph, pIn);
		this.expectedSize = expectedSize;
	}

	/**
	 * Returns the intra-cluster edge probability of this cluster.
	 *
	 * The probability stays constant over the lifetime of this cluster.
	 *
	 * @return the intra-cluster edge probability
	 */
	double getPIn()
	{
		return this.pIn;
	}

	int getGlobalIndex()
	{
		return this.globalIndex;
	}

	/**
	 * Returns the 0-based index which shall be used in the journal.
	 *
	 * @return the index for the journal
	 */
	public int getJournalIndex()
	{
		return this.getGlobalIndex() - INITIAL_COUNTER + 1;
	}

	int getExpectedSize()
	{
		return this.expectedSize;
	}

	/*
	 * *********************************************************************
	 *
	 * Node-specific methods
	 *
	 * *********************************************************************
	 */

	/**
	 * Removes an isolated node.
	 *
	 * If this cluster is contained in the ground truth it will remove the
	 * node from this cluster and from the pseudo cluster and finally
	 * update the tree weights.
	 * If it is contained in the reference clustering it will solely remove
	 * the node from its personal adjacency list.
	 *
	 * @param node
	 *            the node to be removed
	 */
	@Override
	void removeNode(final Node node)
	{

		if (this.isInGroundTruth())
		{
			assert this.getNodeCount() > 1;
			int gtIndexOfRemovedNode = node.getGtClIndex();

			// remove from pseudo cluster
			this.graph.getPseudoCluster().removeNode(node);

			/*
			 * Remove node from adjacency list.
			 * The last node will take its place.
			 */
			final Node lastNode = adjacencyList.getLastNode();
			/*
			 * Remove all adjacencies of the last node from the *Fisher-Yates shuffle*
			 * This is a prerequisite of using the #fastResize method of the shuffle!
			 */
			final Iterator<Edge> intraClIterOfLast =
				adjacencyList.intraClusterIterator(lastNode);

			while (intraClIterOfLast.hasNext())
			{
				final Edge edge = intraClIterOfLast.next();
				final long localID = this.getLocalID(edge);

				assert this.shuffle.contains(localID);
				this.shuffle.delete(localID);
			}

			this.adjacencyList.removeNode(node);

			// the last node has taken the place of the removed node
			assert BooleanUtils.implies(!lastNode.equals(node),
				lastNode.getGtClIndex() == gtIndexOfRemovedNode);
			assert BooleanUtils.implies(lastNode.equals(node),
				lastNode.getGtClIndex() == node.getGtClIndex());

			/*
			 * Update Fisher-Yates shuffle:
			 * Insert adjacencies of the former last node
			 * as adjacencies corresponding to the new position it takes!
			 */
			if (!node.equals(lastNode))
			{
				final Iterator<Edge> updatedEdgesIterator =
					adjacencyList.intraClusterIterator(lastNode);

				while (updatedEdgesIterator.hasNext())
				{
					final Edge edge = updatedEdgesIterator.next();
					final long localID = this.getLocalID(edge);

					assert !this.shuffle.contains(localID);
					this.shuffle.select(localID);
				}
			}

			this.shuffle.fastResize(Edge.maxEdgeCount(this.getNodeCount()));

			updateTreeWeights();
		}
		else if (this.isInReferenceClustering())
		{
			this.adjacencyList.removeNode(node);
		}


	}

	/**
	 * Adds a new node to the cluster.
	 *
	 * If the cluster is contained in the ground truth it will also add the
	 * node to the pseudo cluster and update the tree weights.
	 * Otherwise, the insertion is purely local to the cluster.
	 *
	 * @param node
	 *            the new node
	 */
	@Override
	void addNode(final Node node)
	{
		addNodeLocally(node);

		if (this.isInGroundTruth())
		{
			this.graph.getPseudoCluster().addNode(node);
			updateTreeWeights();

		}
	}

	/**
	 * Adds a node to the cluster without affecting any external data
	 * structures
	 * such as the search tree, the pseudo cluster or the potential second
	 * cluster of this edge (in case it is an inter-cluster edge).
	 *
	 * @param node
	 *            the node to be added
	 */
	void addNodeLocally(final Node node)
	{
		this.adjacencyList.addNode(node);

		this.shuffle.resize(Edge.maxEdgeCount(this.getNodeCount()));

		if (this.isLocked())
		{
			assert this.getNodeCount() > 0 : "Found empty cluster! Cl: " + this;
			node.setOperationIndex(this.getNode(0).getOperationIndex());
		}
	}

	@Override
	int getLocalID(final Node node)
	{
		return node.getGtClIndex();
	}

	@Override
	boolean contains(final Node node)
	{
		return this.adjacencyList.contains(node);
	}

	@Override
	Node getNode(final int localID)
	{
		return this.adjacencyList.getNode(localID);
	}

	@Override
	int getNodeCount()
	{
		return this.adjacencyList.getNodeCount();
	}

	public boolean isValidNodeIndex(final long index)
	{
		return index < this.getNodeCount();
	}

	@Override
	public Iterator<Node> nodeIterator()
	{
		return this.adjacencyList.nodeIterator();
	}

	/**
	 * Returns the number of intra-cluster edges adjacent to this node.
	 *
	 * The node has to be contained in this cluster!
	 *
	 * @param node
	 *            the node
	 * @return the number of intra-cluster edges
	 */
	public int getDegreeIntra(final Node node)
	{
		return this.adjacencyList.getIntraClusterAdjacencies(node).size();
	}

	/**
	 * Returns the number of inter-cluster edges adjacent to this node.
	 *
	 * The node has to be contained in this cluster!
	 *
	 * @param node
	 *            the node
	 * @return the number of inter-cluster edges
	 */
	public int getDegreeInter(final Node node)
	{
		return this.adjacencyList.getInterClusterAdjacencies(node).size();
	}

	/*
	 * *********************************************************************
	 *
	 * Edge-specific methods
	 *
	 * *********************************************************************
	 */

	@Override
	void addEdge(final Edge edge)
	{
		addEdgeLocally(edge);

		if (edge.isInterClusterEdge())
		// inter-cluster edge
		{
			final Node nodeOutsideCl =
				this.contains(edge.getSource()) ? edge.getTarget() : edge.getSource();
			if (this.equals(edge.getCluster()))
			{
				nodeOutsideCl.getGtCluster().addEdge(edge.getReverseEdge());
			}
		}

		if (this.equals(edge.getCluster()))
		{
			this.graph.getPseudoCluster().addEdge(edge);
		}

		updateTreeWeights();
	}

	/**
	 * Adds an edge to the cluster without affecting any other data
	 * structures
	 * such as the search tree, the pseudo cluster or the potential second
	 * cluster of this edge (in case it is an inter-cluster edge).
	 *
	 * @param edge
	 *            the edge to be added purely locally
	 */
	void addEdgeLocally(final Edge edge)
	{
		if (edge.isIntraClusterEdge())
		{
			this.adjacencyList.addEdge(edge);
			final long localIndex = edge.getLocalGtClusterIndex();
			this.shuffle.select(localIndex);
		}
		else
		{
			final Node nodeInsideCl =
				this.contains(edge.getSource()) ? edge.getSource() : edge.getTarget();
			if (edge.getSource().equals(nodeInsideCl))
			{
				this.adjacencyList.addEdge(edge);
			}
			else
			{
				this.adjacencyList.addEdge(edge.getReverseEdge());
			}
		}

	}

	@Override
	void updateTreeWeights()
	{
		this.updateInsertionWeight(//
		this.getIntraClusterNonEdgeCount() * (this.pIn - this.graph.getPOut()));

		this.updateDeletionWeight(//
		this.getIntraClusterEdgeCount() * (1 - this.pIn));
	}

	@Override
	Edge findEdge()
	{
		final long proposedIndex = this.shuffle.proposeForDeletion();
		final Pair<Integer> localNodeIndices = Edge.nodeIndices(proposedIndex);
		final Pair<Node> nodes = this.getNodes(localNodeIndices);

		final Edge result = this.adjacencyList.getEdge(nodes);

		return result;
	}

	@Override
	void removeEdge(final Edge edge)
	{
		assert this.isInGroundTruth();

		if (this.equals(edge.getCluster()))
		{
			final PseudoCluster pcl = this.graph.getPseudoCluster();
			final int edgeCountBefore = pcl.getEdgeCount();
			pcl.removeEdge(edge);
			assert pcl.getEdgeCount() == edgeCountBefore - 1 : "exp: " + (edgeCountBefore - 1)
				+ " act: " + pcl.getEdgeCount();
		}

		this.removeEdgeLocally(edge);

		if (edge.isIntraClusterEdge())
		{
			final long localIndex = edge.getLocalGtClusterIndex();
			this.shuffle.delete(localIndex);
		}
		else
		{
			final Node nodeOutsideCl =
				this.contains(edge.getSource()) ? edge.getTarget() : edge.getSource();
			if (this.equals(edge.getCluster()))
			{
				nodeOutsideCl.getGtCluster().removeEdge(edge);
			}
		}

		updateTreeWeights();
	}

	/**
	 * Removes an edge from the cluster without affecting any external data
	 * structures
	 * such as the search tree, the pseudo cluster or the potential second
	 * cluster of this edge (in case it is an inter-cluster edge).
	 *
	 * @param edge
	 *            the edge to be removed purely locally
	 */
	private void removeEdgeLocally(final Edge edge)
	{
		final Node nodeInsideCl =
			this.contains(edge.getSource()) ? edge.getSource() : edge.getTarget();
		if (edge.isIntraClusterEdge() || edge.getSource().equals(nodeInsideCl))
		{
			this.adjacencyList.removeEdge(edge);
		}
		else
		{
			this.adjacencyList.removeEdge(edge.getReverseEdge());
		}
	}

	/**
	 * This method removes all edges from the given cluster.
	 *
	 * This method is tailored to the needs of cluster operations so it
	 * assumes that the cluster is currently located in the reference
	 * clustering!
	 */
	void clearEdges()
	{
		assert this.isInReferenceClustering() : "Currently only supported for ref cl clusters!";

		this.adjacencyList.clearEdges();
		this.shuffle.clear();
	}

	@Override
	boolean contains(final Edge edge)
	{
		return this.adjacencyList.contains(edge);
	}

	public int getInterClusterEdgeCount()
	{
		return this.adjacencyList.getInterClusterEdgeCount();
	}

	public int getInterClusterEdgeCount(final OrdinaryCluster other)
	{
		// Efficiency: This may not be very efficient as it is in O(m_out)
		final Iterator<Edge> iter = this.interClusterEdgeIterator(other);
		int counter = 0;
		while (iter.hasNext())
		{
			iter.next();
			++counter;
		}
		return counter;
	}

	public int getIntraClusterEdgeCount()
	{
		return this.adjacencyList.getIntraClusterEdgeCount();
	}

	public long getIntraClusterNonEdgeCount()
	{
		return Edge.maxEdgeCount(this.getNodeCount())
				- this.adjacencyList.getIntraClusterEdgeCount();
	}

	public Iterator<Edge> edgeIterator(final Node node)
	{
		return this.adjacencyList.edgeIterator(node);
	}

	public Iterator<Edge> intraClusterEdgeIterator()
	{
		return this.adjacencyList.intraClusterEdgeIterator();
	}

	public Iterator<Edge> interClusterEdgeIterator()
	{
		return adjacencyList.interClusterEdgeIterator();
	}

	public Iterator<Edge> interClusterEdgeIterator(final OrdinaryCluster other)
	{
		return adjacencyList.interClusterEdgeIterator(other);
	}

	/*
	 * *********************************************************************
	 *
	 * Clustering-specific methods
	 *
	 * *********************************************************************
	 */

	int getGroundTruthIndex()
	{
		return this.gtIndex;
	}

	void setGroundTruthIndex(final int index)
	{
		final int oldIdx = this.gtIndex;
		this.gtIndex = index;

		/*
		 * If the cluster has just been added to or deleted from the
		 * clustering, exactly one of both indices is negative!
		 *
		 * In all other cases no notification of the contained nodes
		 * is necessary!
		 */
		if (BooleanUtils.xor(oldIdx == AbstractCluster.INVALID_ID,
			this.gtIndex == AbstractCluster.INVALID_ID))
		{
			for (int i = 0; i < this.adjacencyList.getNodeCount(); ++i)
			{
				final Node v = this.adjacencyList.getNode(i);
				if (this.gtIndex != AbstractCluster.INVALID_ID)
				{
					assert v.getGtClIndex() == Node.INVALID_ID : "node: " + v;
					v.setGtClIndex(i);
					v.setGtCluster(this);
				}
				else
				{
					assert v.getGtClIndex() != Node.INVALID_ID : "node: " + v;
					v.setGtClIndex(Node.INVALID_ID);
					v.setGtCluster(null);
				}
			}
		}
	}

	public boolean isInGroundTruth()
	{
		return INVALID_ID != this.gtIndex;
	}

	int getReferenceClusteringIndex()
	{
		return this.refIndex;
	}

	void setReferenceClusteringIndex(final int index)
	{
		final int oldIdx = this.refIndex;
		this.refIndex = index;

		/*
		 * If the cluster has just been added to or deleted from the
		 * clustering, exactly one of both indices is negative!
		 */
		if (BooleanUtils.xor(oldIdx == AbstractCluster.INVALID_ID,
			this.refIndex == AbstractCluster.INVALID_ID))
		{
			for (int i = 0; i < this.adjacencyList.getNodeCount(); ++i)
			{
				final Node v = this.adjacencyList.getNode(i);
				if (this.refIndex != AbstractCluster.INVALID_ID)
				{
					assert v.getRefClIndex() == Node.INVALID_ID : "node: " + v;
					v.setRefClIndex(i);
					v.setRefClCluster(this);
				}
				else
				{
					assert v.getRefClIndex() != Node.INVALID_ID : "node: " + v;
					v.setRefClIndex(Node.INVALID_ID);
					v.setRefClCluster(null);
				}
			}
		}
	}

	public boolean isInReferenceClustering()
	{
		return INVALID_ID != this.refIndex;
	}

	/*
	 * *********************************************************************
	 *
	 * Operation-specific methods
	 *
	 * *********************************************************************
	 */

	AbstractClusterOperation getCurrentOperation()
	{
		return this.currentOperation;
	}

	/**
	 * Sets the current operation.
	 *
	 * For clearing the opertion use the explicit clearCurrentOperation
	 * method.
	 *
	 * @param op
	 *            the non-null operation
	 */
	void setCurrentOperation(final AbstractClusterOperation op)
	{
		currentOperation = op;
	}

	void clearCurrentOperation()
	{
		currentOperation = null;
	}

	void setIndexInListOfNonLockedClusters(final int index)
	{
		this.indexInListOfNonLockedClusters = index;
	}

	void clearIndexInListOfNonLockedClusters()
	{
		this.indexInListOfNonLockedClusters = AbstractCluster.INVALID_ID;
	}

	int getIndexInListOfNonLockedClusters()
	{
		return this.indexInListOfNonLockedClusters;
	}

	/**
	 * Returns whether the cluster is currently participating in a cluster
	 * operation.
	 *
	 * Clusters may only participate in one cluster operation at a time.
	 *
	 * @return whether the cluster is locked by a cluster operation.
	 */
	boolean isLocked()
	{
		return null != this.currentOperation;
	}

	/*
	 * *********************************************************************
	 *
	 * Operation-specific methods
	 *
	 * Ordinary clusters are member of the selection tree
	 * which is used to determine the cluster for a new node
	 * *********************************************************************
	 */

	Weightable getExpectedSizeWeight()
	{
		return new Weightable()
		{
			@Override
			public double getWeight()
			{
				return expectedSize;
			}

			@Override
			public Object getObject()
			{
				return OrdinaryCluster.this;
			}
		};
	}

	void setSelectionTreeNode(final TreeNode node)
	{
		this.selectionTreeNode = node;
	}

	void clearSelectionTreeNode()
	{
		this.selectionTreeNode = null;
	}

	TreeNode getSelectionTreeNode()
	{
		return this.selectionTreeNode;
	}

	/**
	 * The hash code is fully determined by the global index of this
	 * cluster and therefore
	 * constant over time.
	 */
	@Override
	public int hashCode()
	{
		return this.globalIndex;
	}

	@Override
	public boolean equals(final Object obj)
	{

		if (obj == null || getClass() != obj.getClass())
			return false;

		return this == obj || globalIndex == ((OrdinaryCluster) obj).globalIndex;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("OrdinaryCluster [");
		builder.append("gl: ");
		builder.append(this.globalIndex);
		builder.append(", gt:");
		builder.append(this.gtIndex);
		builder.append(", ref: ");
		builder.append(this.refIndex);
		builder.append(", pIn: ");
		builder.append(this.pIn);
		builder.append(", op: ");
		builder.append(this.isLocked() ? this.currentOperation.getGlobalIndex() : "null");
		builder.append(", non-locked ID: ");
		builder.append(this.indexInListOfNonLockedClusters);
		builder.append("]");

		return builder.toString();
	}
}
