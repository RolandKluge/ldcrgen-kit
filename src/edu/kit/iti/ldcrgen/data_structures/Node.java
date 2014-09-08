package edu.kit.iti.ldcrgen.data_structures;

public class Node
{
	public static final int INVALID_ID = -1;

	private static final int INIT_COUNTER = 2;

	// VERY IMPORTANT! Do not start with 0 or 1, because this will entail
	// two edges with global index 0!
	private static int counter = INIT_COUNTER;

	private final int globalIndex;
	private int localPsClIndex;
	private int localGtClIndex;
	private int localRefClIndex;

	private int operationIndex;

	private OrdinaryCluster gtCluster;
	private OrdinaryCluster refClCluster;


	Node()
	{
		this.globalIndex = counter++;
		this.localPsClIndex = INVALID_ID;

		this.localGtClIndex = INVALID_ID;
		this.gtCluster = null;

		this.localRefClIndex = INVALID_ID;
		this.refClCluster = null;

		this.operationIndex = 0;

	}

	int getPsClIndex()
	{
		return localPsClIndex;
	}

	void setPsClIndex(final int localIndexPseudoCluster)
	{
		this.localPsClIndex = localIndexPseudoCluster;
	}

	int getGtClIndex()
	{
		return localGtClIndex;
	}

	void setGtClIndex(final int localIndexHomeCluster)
	{
		this.localGtClIndex = localIndexHomeCluster;
	}

	void setGtCluster(final OrdinaryCluster cluster)
	{
		this.gtCluster = cluster;
	}

	OrdinaryCluster getGtCluster()
	{
		return this.gtCluster;
	}

	void setRefClIndex(final int index)
	{
		this.localRefClIndex = index;
	}

	int getRefClIndex()
	{
		return this.localRefClIndex;
	}

	void setRefClCluster(final OrdinaryCluster cluster)
	{
		this.refClCluster = cluster;
	}

	OrdinaryCluster getRefClCluster()
	{
		return this.refClCluster;
	}

	public int getGlobalIndex()
	{
		return globalIndex;
	}


	void setOperationIndex(final int operationIndex)
	{
		this.operationIndex = operationIndex;
	}

	int getOperationIndex()
	{
		return this.operationIndex;
	}

	public int getDegree()
	{
		return this.getDegreeInter() + this.getDegreeIntra();
	}

	/**
	 * Returns the number of intra-cluster adjacencies.
	 *
	 * @return the number of intra-cluster edges incident to this node
	 */
	public int getDegreeIntra()
	{
		return this.gtCluster.getDegreeIntra(this);
	}

	/**
	 * Returns the number of inter-cluster adjacencies.
	 *
	 * @return the number of inter-cluster edges incident to this node
	 */
	public int getDegreeInter()
	{
		return this.gtCluster.getDegreeInter(this);
	}

	@Override
	public int hashCode()
	{
		return this.globalIndex;
	}

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
		final Node other = (Node) obj;
		return this.getGlobalIndex() == other.getGlobalIndex();
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("Node [glo: ");
		builder.append(this.globalIndex);
		builder.append(", gt: ");
		builder.append(this.localGtClIndex);
		builder.append(", ps: ");
		builder.append(this.localPsClIndex);
		builder.append(", ref: ");
		builder.append(this.localRefClIndex);
		builder.append(", gtcl: ");
		builder.append(this.gtCluster != null ? gtCluster.getGlobalIndex() : "null");
		builder.append(", refcl: ");
		builder.append(this.refClCluster != null ? refClCluster.getGlobalIndex() : "null");
		builder.append(", opid: ");
		builder.append(this.operationIndex);
		builder.append("]");

		return builder.toString();
	}

	public int getJournalIndex()
	{
		return this.globalIndex - INIT_COUNTER + 1;
	}

}
