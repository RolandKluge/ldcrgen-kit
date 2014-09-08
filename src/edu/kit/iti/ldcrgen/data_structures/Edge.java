package edu.kit.iti.ldcrgen.data_structures;

import edu.kit.iti.ldcrgen.util.Pair;

/**
 * Edges are designed to be 'passive' entities:
 * The contain as few information as possible.
 * Their properties (such as being an inter-/intra-cluster edge and so on)
 * is calculated dynamically from the endpoints.
 *
 * The only property which needs to be set for an edge is its
 * index in the list of inter-cluster edges.
 *
 * As Edge actually represents an undirected edge, one instance can
 * only exist together with its reverse edge.
 * Therefore there is no public constructor of this class but only
 * the static {@link Edge.createEdge} method.
 *
 * @author Roland Kluge
 */
public final class Edge
{
	public static final int INVALID_ID = -1;

	private final long globalIndex;
	private final Node source;
	private final Node target;
	private Edge reverseEdge;

	private int interClusterEdgeListID;

	private Edge(final Node source, final Node target, final Edge reverseEdge)
	{
		this.source = source;
		this.target = target;
		this.globalIndex = Edge.edgeIndex(source.getGlobalIndex(), target.getGlobalIndex());
		this.reverseEdge = reverseEdge;

		this.interClusterEdgeListID = INVALID_ID;
	}

	/**
	 * Creates two directed edges representing the undirected edge from
	 * source to target.
	 *
	 * The resulting edge is (source,target).
	 *
	 * @param nodes
	 *            the nodes <code>source</code> and <code>target</code>
	 *            from which the edge will be built
	 * @return one directed edge representing the undirected edge
	 *
	 * @see #getReverseEdge()
	 */
	static Edge createEdge(final Pair<Node> nodes)
	{
		final Node source = nodes.getFirst();
		final Node target = nodes.getSecond();

		final Edge first = new Edge(source, target, null);
		final Edge second = new Edge(target, source, first);
		first.reverseEdge = second;

		return first;
	}

	public Node getSource()
	{
		return this.source;
	}

	public Node getTarget()
	{
		return this.target;
	}

	Edge getReverseEdge()
	{
		return this.reverseEdge;
	}

	/**
	 * Sets the index in the list of inter-cluster edges (pseudo cluster).
	 *
	 * This method will also set the index for the reverse edge.
	 *
	 * @param interClusterEdgeListID
	 *            the new index
	 */
	void setInterClusterEdgeListID(final int interClusterEdgeListID)
	{
		this.interClusterEdgeListID = interClusterEdgeListID;
		this.reverseEdge.interClusterEdgeListID = interClusterEdgeListID;
	}

	int getInterClusterEdgeListID()
	{
		return interClusterEdgeListID;
	}

	public long getGlobalIndex()
	{
		return this.globalIndex;
	}

	/**
	 * Being an intra-cluster edge or not is always determined by the
	 * ground truth
	 * clusters of the incident nodes.
	 *
	 * @return whether this edge is an intra-cluster edge
	 */
	public boolean isIntraClusterEdge()
	{
		return null != this.source.getGtCluster()
			&& this.source.getGtCluster() == this.target.getGtCluster();
	}

	/**
	 * Being an inter-cluster edge or not is always determined by the
	 * ground truth
	 * clusters of the incident nodes.
	 *
	 * @return whether this edge is an intra-cluster edge
	 */
	public boolean isInterClusterEdge()
	{
		return !this.isIntraClusterEdge();
	}

	/**
	 * Returns whether this edge is an inter-cluster edge between
	 * the two given clusters.
	 *
	 * Note that first and second have to be distinct clusters
	 * because otherwise the semantics of an inter-cluster edge
	 * would be violated.
	 *
	 * @param first
	 *            the first cluster
	 * @param second
	 *            the second cluster
	 * @return whether this edge is an inter-cluster edge between the two
	 *         clusters
	 */
	public boolean isInterClusterEdge(final OrdinaryCluster first, final OrdinaryCluster second)
	{
		return this.source.getGtCluster().equals(first)
			&& this.target.getGtCluster().equals(second)
				|| this.source.getGtCluster().equals(second)
				&& this.target.getGtCluster().equals(first);
	}

	// getLocalRefClusterIndex not needed as reference clusters do not
	// contain any edge

	/**
	 * Returns the index of this edge within the ground truth clusters of
	 * its
	 * endpoints.
	 *
	 * This method may only be called if this edge is an intra-cluster
	 * edge.
	 *
	 * @return the local index of this edge
	 */
	public long getLocalGtClusterIndex()
	{
		return Math.abs(Edge.edgeIndex(this.getSource().getGtClIndex(),//
			this.getTarget().getGtClIndex()));
	}

	/**
	 * Returns the index of this edge within the pseudo cluster of
	 * its endpoints.
	 *
	 * @return the local index of this edge
	 */
	public long getLocalPseudoClusterIndex()
	{
		return Math.abs(Edge.edgeIndex(this.getSource().getPsClIndex(),//
			this.getTarget().getPsClIndex()));
	}

	/**
	 * Returns the cluster which is 'responsible' of this edge.
	 *
	 * This cluster contains the endpoint of the edge with the lower
	 * global index.
	 * For intra-cluster edges this is of course also the containing
	 * cluster of the edge.
	 *
	 * @return the cluster of this edge
	 */
	public OrdinaryCluster getCluster()
	{
		return this.source.getGlobalIndex() < this.target.getGlobalIndex() //
		? this.source.getGtCluster() //
			: this.target.getGtCluster();
	}

	/**
	 * Returns the ground truth clusters of this edge's endpoints.
	 *
	 * @return the cluster of this edge
	 */
	public Pair<OrdinaryCluster> getClusters()
	{
		return new Pair<OrdinaryCluster>(this.source.getGtCluster(), this.target.getGtCluster());
	}

	/**
	 * Returns whether this edge connects two clusters
	 * which participate in the same operation.
	 *
	 * @return whether this edge connects two clusters of the same
	 *         operation
	 */
	public boolean isInvolvedInOperation()
	{
		return this.source.getOperationIndex() != 0 &&
			this.source.getOperationIndex() == -this.target.getOperationIndex();
	}

	/**
	 * Returns the clustering operation which is affected by deleting or
	 * adding this edge.
	 *
	 * @return the operation affected by adding or deleting this edge
	 */
	public AbstractClusterOperation getOperation()
	{
		return this.source.getGtCluster().getCurrentOperation();
	}

	/**
	 * Returns the edge index resulting from the triangular indexing
	 * scheme.
	 * In order to distinguish the two directed parts of an edge the
	 * resulting
	 * indices get opposite signs:
	 *
	 * If for edge (s,t) it holds that the global ID of s is larger than
	 * the global ID of t, then and only then the result is positive.
	 *
	 * @param first
	 *            the first index
	 * @param second
	 *            the second index
	 * @return the (enhanced) triangular index
	 */
	public static long edgeIndex(final int first, final int second)
	{
		final int max = Math.max(first, second);
		final int min = Math.min(first, second);

		return (max * (max - 1L) / 2L + min) * (first > second ? 1 : -1);
	}

	/**
	 * Returns the node indices which correspond to the given edge index.
	 *
	 * The edge index may be any integer but only its absolute value is
	 * used. The result is an ordered pair (u,v) with u > v if edge is positive
	 * and u < v if negative - for edge=0 the order is undefined.
	 * This behavior is consistent with the generation of edge indices.
	 *
	 * @param edge
	 *            the edge index
	 * @return the node indices which correspond to this edge index
	 *
	 * @see Edge#edgeIndex(int, int)
	 */
	public static Pair<Integer> nodeIndices(final long edge)
	{
		final int u = 1 + (int) Math.floor(-0.5 + Math.sqrt(0.25 + 2 * Math.abs(edge)));
		final int v = (int) (Math.abs(edge) - u * (u - 1L) / 2);

		return (edge >= 0) ? new Pair<Integer>(u, v) : new Pair<Integer>(v, u);
	}

	/**
	 * Returns the maximum number of edges for an undirected and simple
	 * graph with the given node count.
	 *
	 * @return the maximum edge count under the given premises
	 */
	public static long maxEdgeCount(final int nodeCount)
	{
		return 1L * nodeCount * (nodeCount - 1) / 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return (int) (this.globalIndex ^ (this.globalIndex >>> 32));
	}

	/**
	 * Two edges are equal if they possess the same global id.
	 *
	 * Note that this definition distinguishes between both parts
	 * of an undirected edge.
	 *
	 * @param obj
	 *            the other object
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (null == obj || obj.getClass() != getClass())
		{
			return false;
		}
		return this.getGlobalIndex() == ((Edge) obj).getGlobalIndex();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		if (this.isInterClusterEdge())
		{
			builder.append("inter");
			builder.append(this.interClusterEdgeListID);
		}
		else
		{
			builder.append("intra");
		}
		builder.append(" edge [global:");
		builder.append(this.globalIndex);
		builder.append(", (");
		builder.append(this.source.toString());
		builder.append(" -> ");
		builder.append(this.target.toString());
		builder.append(")]");

		return builder.toString();
	}
}
