package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.Pair;

/**
 * This is a special type of adjacency list which allows to insert edges
 * where one node is not contained in the adjacency list
 * itself.
 *
 * @author Roland Kluge
 */
public class ClusterAdjacencyList
{
	private int edgeCount;
	private int interClusterEdgeCount;

	private final ArrayList<Node> nodes;
	private final ArrayList<ArrayList<Edge>> interClusterAdjacencies;
	private final ArrayList<ArrayList<Edge>> intraClusterAdjacencies;

	private OrdinaryCluster parent;

	/*
	 * This map is used to efficiently lookup the position of an edge
	 * within a node's list of adjacencies.
	 */
	private Map<Edge, Integer> edgeIndexLookup;

	public ClusterAdjacencyList(final OrdinaryCluster parent)
	{
		this.parent = parent;

		this.edgeCount = 0;
		this.interClusterEdgeCount = 0;
		this.nodes = new ArrayList<Node>();
		this.interClusterAdjacencies = new ArrayList<ArrayList<Edge>>();
		this.intraClusterAdjacencies = new ArrayList<ArrayList<Edge>>();
		this.edgeIndexLookup = new HashMap<Edge, Integer>();
	}

	OrdinaryCluster getParent()
	{
		return this.parent;
	}

	/*
	 * *********************************************************************
	 *
	 * Operations upon nodes
	 *
	 * *********************************************************************
	 */

	public void addNode(final Node node)
	{
		this.interClusterAdjacencies.add(new ArrayList<Edge>());
		this.intraClusterAdjacencies.add(new ArrayList<Edge>());

		nodes.add(node);
		setIndex(node, this.nodes.size() - 1);
	}

	public void removeNode(final Node node)
	{
		/*
		 * Swap the last intra- and inter-cluster lists of adjacencies to
		 * the position of the given node
		 */
		final int index = getIndex(node);
		final Node lastNode = ListUtils.last(this.nodes);
		setIndex(lastNode, index);
		clearIndex(node);

		ListUtils.moveLastTo(this.interClusterAdjacencies, index);
		ListUtils.moveLastTo(this.intraClusterAdjacencies, index);
		ListUtils.moveLastTo(this.nodes, index);
	}

	/**
	 * This is a strict test whether a certain node is assigned to this
	 * adjacency list.
	 *
	 * Note that nodes may even be stored in an adjacency list without
	 * being recognized as 'contained' in it.
	 * This may happen if nodes are about to be transfered from one
	 * cluster to another. In such a case, 'contains' will return
	 * <code>false</code>.
	 *
	 * @param node
	 *            the node to be checked
	 * @return whether the node is in the responsibility of this adjacency
	 *         list
	 */
	public boolean contains(final Node node)
	{
		final int index = getIndex(node);
		return index != Node.INVALID_ID //
			&& index < this.nodes.size() //
			&& this.getNode(index).equals(node);
	}

	public Node getNode(final int index)
	{
		return this.nodes.get(index);
	}

	public Node getLastNode()
	{
		return ListUtils.last(this.nodes);
	}

	public int getNodeCount()
	{
		return this.nodes.size();
	}

	public Iterator<Node> nodeIterator()
	{
		return Collections.unmodifiableList(this.nodes).iterator();
	}

	public Iterator<Edge> intraClusterIterator(final Node node)
	{
		final int index = getIndex(node);
		return Collections.unmodifiableList(this.intraClusterAdjacencies.get(index)).iterator();
	}

	public List<Edge> getIntraClusterAdjacencies(final Node node)
	{
		return Collections.unmodifiableList(this.intraClusterAdjacencies.get(getIndex(node)));
	}

	public Iterator<Edge> interClusterIterator(final Node node)
	{
		final int index = getIndex(node);
		return Collections.unmodifiableList(this.interClusterAdjacencies.get(index)).iterator();
	}

	public List<Edge> getInterClusterAdjacencies(final Node node)
	{
		return Collections.unmodifiableList(this.interClusterAdjacencies.get(getIndex(node)));
	}

	private void clearIndex(final Node node)
	{
		if (parent.isInGroundTruth())
		{
			node.setGtClIndex(Node.INVALID_ID);
			node.setGtCluster(null);
		}

		if (parent.isInReferenceClustering())
		{
			node.setRefClIndex(Node.INVALID_ID);
			node.setRefClCluster(null);
		}
	}

	private void setIndex(final Node node, final int index)
	{
		if (parent.isInGroundTruth())
		{
			node.setGtClIndex(index);
			node.setGtCluster(parent);
		}

		if (parent.isInReferenceClustering())
		{
			node.setRefClIndex(index);
			node.setRefClCluster(parent);
		}
	}

	private int getIndex(final Node node)
	{
		int index;
		if (parent.isInGroundTruth())
		{
			index = node.getGtClIndex();
		}
		else
		{
			index = node.getRefClIndex();
		}
		return index;
	}

	/*
	 * *********************************************************************
	 *
	 * Operations upon edges
	 *
	 * *********************************************************************
	 */

	/**
	 * Creates an undirected edge between the two given nodes.
	 *
	 * The start point of the given edge must be contained in the list!
	 *
	 * @param nodes
	 *            the nodes to be connected
	 * @return the resulting directed edge the first node of which has a
	 *         larger global ID than the second one.
	 */
	public void addEdge(final Edge edge)
	{
		final Node src = edge.getSource();
		final Node dst = edge.getTarget();

		final List<Edge> adjacencies = edge.isIntraClusterEdge() //
		? this.intraClusterAdjacencies.get(getIndex(src))
					: this.interClusterAdjacencies.get(getIndex(src));

		adjacencies.add(edge);
		this.edgeIndexLookup.put(edge, adjacencies.size() - 1);

		if (edge.isIntraClusterEdge())
		{
			final List<Edge> adjacenciesRev = this.intraClusterAdjacencies.get(getIndex(dst));
			adjacenciesRev.add(edge.getReverseEdge());

			this.edgeIndexLookup.put(edge.getReverseEdge(), adjacenciesRev.size() - 1);
		}
		else
		// inter-cluster edge
		{
			++this.interClusterEdgeCount;
		}

		++this.edgeCount;
	}

	public Edge getEdge(final Pair<Node> nodes)
	{
		/*
		 * The 'contains' method only relies on the global index
		 * of the edge which in turn is only dependent of the
		 * global indices of its end nodes.
		 */
		final Edge dummyEdge = Edge.createEdge(nodes);
		assert this.contains(dummyEdge.getSource());
		assert this.contains(dummyEdge);

		final int index = this.edgeIndexLookup.get(dummyEdge);

		final Node src = dummyEdge.getSource();

		final List<Edge> adjacencies = dummyEdge.isIntraClusterEdge() //
		? this.intraClusterAdjacencies.get(getIndex(src))
					: this.interClusterAdjacencies.get(getIndex(src));

		return adjacencies.get(index);
	}

	public void removeEdge(final Edge edge) // e= (source,target)
	{
		--this.edgeCount;

		final Node src = edge.getSource();

		final int positionOfEdge = this.edgeIndexLookup.get(edge);

		final List<Edge> adjacencies = edge.isIntraClusterEdge() //
		? this.intraClusterAdjacencies.get(this.getIndex(src))
				: this.interClusterAdjacencies.get(this.getIndex(src));
		final Edge lastInEdgeList = ListUtils.last(adjacencies);
		ListUtils.moveLastTo(adjacencies, positionOfEdge);

		edgeIndexLookup.put(lastInEdgeList, positionOfEdge);
		edgeIndexLookup.remove(edge);

		if (edge.isIntraClusterEdge())
		// swap last edge to the position of e in list of 'target'
		{
			final Node dst = edge.getTarget();

			final Integer positionOfRevEdge = this.edgeIndexLookup.get(edge.getReverseEdge());

			final List<Edge> adjacenciesRev = this.intraClusterAdjacencies.get(getIndex(dst));

			final Edge lastInRevEdgeList = ListUtils.last(adjacenciesRev);
			ListUtils.moveLastTo(adjacenciesRev, positionOfRevEdge);

			edgeIndexLookup.put(lastInRevEdgeList, positionOfRevEdge);
			edgeIndexLookup.remove(edge.getReverseEdge());
		}
		else
		// inter-cluster edge
		{
			--interClusterEdgeCount;
		}


	}

	public void clearEdges()
	{
		this.interClusterEdgeCount = 0;
		this.edgeCount = 0;

		this.interClusterAdjacencies.clear();
		this.interClusterAdjacencies.addAll(Collections.nCopies(this.getNodeCount(),
			new ArrayList<Edge>()));

		this.intraClusterAdjacencies.clear();
		this.intraClusterAdjacencies.addAll(Collections.nCopies(this.getNodeCount(),
			new ArrayList<Edge>()));

		this.edgeIndexLookup.clear();
	}

	/**
	 * Returns the number of undirected,i.e., unique edges.
	 *
	 * Contrary to the behavior of the edge iterator.
	 * The stored pair (u,v) and (v,u) of directed edges is only
	 * counted once.
	 *
	 * @return the number of undirected edges
	 */
	public int getEdgeCount()
	{
		return this.edgeCount;
	}

	public int getInterClusterEdgeCount()
	{
		return this.interClusterEdgeCount;
	}

	/**
	 * Returns the number of undirected intra-cluster edges.
	 *
	 * Contrary to the behavior of the edge iterator.
	 * The stored pair (u,v) and (v,u) of directed edges is only
	 * counted once.
	 *
	 * @return the number of undirected intra-cluster edges
	 */
	public int getIntraClusterEdgeCount()
	{
		return this.getEdgeCount() - this.getInterClusterEdgeCount();
	}

	public boolean contains(final Edge edge)
	{
		return this.edgeIndexLookup.containsKey(edge);
	}

	public boolean isConnected(final Pair<Node> nodes)
	{
		return this.contains(Edge.createEdge(nodes));
	}

	public boolean isUnconnected(final Pair<Node> nodes)
	{
		return !this.isConnected(nodes);
	}

	public Iterator<Edge> edgeIterator()
	{
		return Collections.unmodifiableSet(edgeIndexLookup.keySet()).iterator();
	}

	public Iterator<Edge> edgeIterator(final Node node)
	{
		return new NodeSpecificEdgeIterator(node);
	}

	public Iterator<Edge> intraClusterEdgeIterator()
	{
		return new ClusterEdgeIterator(Collections.unmodifiableList(this.intraClusterAdjacencies));
	}

	public Iterator<Edge> interClusterEdgeIterator()
	{
		return new ClusterEdgeIterator(this.interClusterAdjacencies);
	}

	public Iterator<Edge> interClusterEdgeIterator(final OrdinaryCluster other)
	{
		return new ClusterSpecificEdgeIterator(other);
	}

	/**
	 * An iterator over all intra- and inter-cluster edges of a cluster.
	 */
	private class ClusterEdgeIterator implements Iterator<Edge>
	{

		Edge nextEdge;
		final List<ArrayList<Edge>> list;
		int nextNode;
		Iterator<Edge> edgeIterator;
		private boolean found;

		ClusterEdgeIterator(final List<ArrayList<Edge>> list)
		{
			this.found = false;
			this.list = list;
			assert !list.isEmpty();

			this.nextNode = 0;

			edgeIterator = this.list.get(this.nextNode++).iterator();
		}

		@Override
		public boolean hasNext()
		{
			while (!found && (edgeIterator.hasNext() || nextNode < this.list.size()))
			{
				if (edgeIterator.hasNext())
				{
					nextEdge = edgeIterator.next();
					found = true;
				}
				else
				{
					edgeIterator = this.list.get(nextNode++).iterator();
				}
			}

			return found;
		}

		@Override
		public Edge next()
		{
			found = false;
			return nextEdge;
		}

		@Override
		@Deprecated
		public void remove()
		{
			throw new UnsupportedOperationException("Read-only iterator");
		}

	}

	private class NodeSpecificEdgeIterator implements Iterator<Edge>
	{
		private final Iterator<Edge> interIter;
		private final Iterator<Edge> intraIter;
		private Iterator<Edge> currentIter;

		public NodeSpecificEdgeIterator(final Node node)
		{
			this.intraIter = intraClusterIterator(node);
			this.interIter = interClusterIterator(node);

			this.currentIter = this.intraIter;
		}

		@Override
		public boolean hasNext()
		{
			boolean hasNext;
			if (this.currentIter.hasNext())
			{
				hasNext = true;
			}
			else
			{
				this.currentIter = this.interIter;
				hasNext = this.currentIter.hasNext();
			}
			return hasNext;
		}

		@Override
		public Edge next()
		{
			return this.currentIter.next();
		}

		@Override
		@Deprecated
		public void remove()
		{
			throw new UnsupportedOperationException("Read-only iterator!");
		}

	}

	private class ClusterSpecificEdgeIterator implements Iterator<Edge>
	{
		private final OrdinaryCluster other;
		private final Iterator<Edge> interClusterEdgeIterator;
		private Edge nextEdge = null;
		private boolean found;

		public ClusterSpecificEdgeIterator(final OrdinaryCluster other)
		{
			assert null != other;

			this.other = other;
			this.found = false;
			this.interClusterEdgeIterator = interClusterEdgeIterator();
		}

		@Override
		public boolean hasNext()
		{
			while (!found && interClusterEdgeIterator.hasNext())
			{
				nextEdge = interClusterEdgeIterator.next();
				if (nextEdge.getClusters().getSecond().equals(other))
				{
					found = true;
				}
			}

			return found;
		}

		@Override
		public Edge next()
		{
			found = false;
			return nextEdge;
		}

		@Override
		@Deprecated
		public void remove()
		{
			throw new UnsupportedOperationException("Read-only iterator!");
		}

	}

}
