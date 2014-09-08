package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import edu.kit.iti.ldcrgen.util.ListUtils;

/**
 * <p>
 * The pseudo cluster has first been designed for incorporating all
 * inter-cluster edges. However, theoretical issues made it necessary to
 * let it contain even all edges of the graph.
 * </p>
 * <p>
 * The pseudo cluster shall not be used directly from outside this package
 * as each node also has an appropriate ground truth cluster being
 * responsible of it. Therefore, consistency among this 'home' cluster and
 * the pseudo cluster has to be established. The 'home' cluster is
 * responsible of this property.
 * </p>
 * <p>
 * The most peculiar behavior of the pseudo cluster is that its
 * findEdge/findNonEdge methods do not behave symmetrically. While it
 * returns all possible non-edges of the graph, it will only return
 * inter-cluster edges when findEdge is called.
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
public class PseudoCluster extends AbstractCluster
{
	private RandomProvider random = new JavaUtilRandomProvider();
	private ArrayList<Edge> interClusterEdges = new ArrayList<Edge>();
	private ArrayList<Node> nodes = new ArrayList<Node>();

	public PseudoCluster(final DCRGraph graph)
	{
		super(graph);
	}

	/*
	 * *********************************************************************
	 *
	 * Node-specific methods
	 *
	 * *********************************************************************
	 */
	@Override
	void addNode(final Node node)
	{
		this.nodes.add(node);
		node.setPsClIndex(nodes.size() - 1);

		this.shuffle.resize(Edge.maxEdgeCount(this.getNodeCount()));

		updateTreeWeights();
	}

	@Override
	void removeNode(final Node node)
	{
		final Node lastNode = ListUtils.last(this.nodes);
		/*
		 * Remove all adjacencies of the last node from the *Fisher-Yates shuffle*
		 * This is a prerequisite of using the #fastResize method of the shuffle!
		 */
		final Iterator<Edge> edgeIterOfLast =
			lastNode.getGtCluster().edgeIterator(lastNode);

		while (edgeIterOfLast.hasNext())
		{
			final Edge edge = edgeIterOfLast.next();
			final long localID = this.getLocalID(edge);

			assert this.shuffle.contains(localID);
			this.shuffle.delete(localID);
		}

		ListUtils.moveLastTo(this.nodes, node.getPsClIndex());
		lastNode.setPsClIndex(node.getPsClIndex());
		node.setPsClIndex(Node.INVALID_ID);

		/*
		 * Update Fisher-Yates shuffle:
		 * Copy edges of last node to the new index of the node
		 */
		if (!node.equals(lastNode))
		{
			final Iterator<Edge> updateIterator =
				lastNode.getGtCluster().edgeIterator(lastNode);

			while (updateIterator.hasNext())
			{
				final Edge edge = updateIterator.next();
				final long localID = this.getLocalID(edge);

				assert !this.shuffle.contains(localID);
				this.shuffle.select(localID);
			}
		}

		this.shuffle.fastResize(Edge.maxEdgeCount(getNodeCount()));

		updateTreeWeights();
	}

	@Override
	int getLocalID(final Node node)
	{
		return node.getPsClIndex();
	}

	@Override
	Node getNode(final int localID)
	{
		return this.nodes.get(localID);
	}

	@Override
	Iterator<Node> nodeIterator()
	{
		return Collections.unmodifiableCollection(this.nodes).iterator();
	}

	@Override
	int getNodeCount()
	{
		return this.nodes.size();
	}

	@Override
	boolean contains(final Node node)
	{
		final int index = node.getPsClIndex();
		return index != Node.INVALID_ID
				&& index < this.nodes.size()
				&& this.nodes.get(index).equals(node);
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
		final long localIndex = edge.getLocalPseudoClusterIndex();
		this.shuffle.select(localIndex);

		if (edge.isInterClusterEdge())
		{
			this.interClusterEdges.add(edge);
			edge.setInterClusterEdgeListID(this.interClusterEdges.size() - 1);
		}

		updateTreeWeights();
	}

	/**
	 * Returns an inter-cluster edge which has been selected uniformly at
	 * random from all inter-cluster edges.
	 *
	 * This method may only be called if there exist inter-cluster edges.
	 */
	@Override
	Edge findEdge()
	{
		final int r = random.nextInt(this.getInterClusterEdgeCount());
		final Edge result = this.interClusterEdges.get(r);

		return result;
	}

	@Override
	void removeEdge(final Edge edge)
	{
		final long localID = this.getLocalID(edge);

		if (edge.isInterClusterEdge())
		{
			assert edge.getInterClusterEdgeListID() != Edge.INVALID_ID : "Inconsistent edge! "
				+ edge;
			final Edge last = ListUtils.last(this.interClusterEdges);
			ListUtils.moveLastTo(this.interClusterEdges, edge.getInterClusterEdgeListID());

			last.setInterClusterEdgeListID(edge.getInterClusterEdgeListID());
			edge.setInterClusterEdgeListID(Edge.INVALID_ID);
		}

		this.shuffle.delete(localID);

		this.updateTreeWeights();
	}

	/**
	 * Checks the actual status of the given edge and performs internal
	 * changes if necessary.
	 *
	 * This method will apply changes for the edge and its reverse edge!
	 *
	 * @param edge
	 *            the edge to be checked
	 */
	void updateStatus(final Edge edge)
	{
		// new inter-cluster edge
		final int interClusterEdgeListID = edge.getInterClusterEdgeListID();
		if (edge.isInterClusterEdge() && interClusterEdgeListID == Edge.INVALID_ID)
		// the edge has become an inter-cluster edge since it was
		// considered for the last time.
		// This may happen during split operations!
		{
			this.interClusterEdges.add(edge);
			edge.setInterClusterEdgeListID(this.interClusterEdges.size() - 1);
			edge.getReverseEdge().setInterClusterEdgeListID(this.interClusterEdges.size() - 1);
		}
		else if (edge.isIntraClusterEdge() && interClusterEdgeListID != Edge.INVALID_ID)
		// the edge has become an intra-cluster edge since it was
		// considered for the last time
		// This may happen during merge operations!
		{
			final Edge last = ListUtils.last(this.interClusterEdges);

			ListUtils.moveLastTo(this.interClusterEdges, interClusterEdgeListID);

			last.setInterClusterEdgeListID(interClusterEdgeListID);
			edge.setInterClusterEdgeListID(Edge.INVALID_ID);
			edge.getReverseEdge().setInterClusterEdgeListID(Edge.INVALID_ID);
		}

		updateTreeWeights();
	}

	/**
	 * Returns whether the given edge is contained in the graph(!).
	 *
	 * It will check whether an intra-cluster edge is contained in its
	 * 'home' cluster. For inter-cluster edges it will check whether
	 * the edge is contained in the list of inter-cluster edges.
	 */
	@Override
	boolean contains(final Edge edge)
	{
		boolean result = false;
		final int interClusterIndex = edge.getInterClusterEdgeListID();

		result |= edge.isInterClusterEdge() //
			&& interClusterIndex != Edge.INVALID_ID
			&& interClusterIndex < this.getInterClusterEdgeCount()
			&& (this.interClusterEdges.get(interClusterIndex).equals(edge)
				|| this.interClusterEdges.get(interClusterIndex).equals(edge.getReverseEdge()));

		result |= edge.isIntraClusterEdge()
			&& edge.getCluster().contains(edge);

		return result;
	}

	/**
	 * The number of non-edges.
	 *
	 * @return the number of non-edges
	 */
	long getNonEdgeCount()
	{
		return Edge.maxEdgeCount(this.getNodeCount()) - this.getEdgeCount();
	}

	/**
	 * The number of inter-cluster edges of the graph.
	 *
	 * @return the number of inter-cluster edges of the graph
	 */
	int getInterClusterEdgeCount()
	{
		return this.interClusterEdges.size();
	}

	@Override
	void updateTreeWeights()
	{
		this.updateInsertionWeight(//
		this.getNonEdgeCount() * this.graph.getPOut());

		this.updateDeletionWeight(//
		this.getInterClusterEdgeCount() * (1 - this.graph.getPOut()));
	}
}
