package edu.kit.iti.ldcrgen.data_structures;


/**
 * The clustering which stores the state of the clustering which may
 * be recognized by an outside observer.
 *
 * The clusters in this clustering only consist of nodes. All edges have
 * been removed from them.
 *
 * @author Roland Kluge
 */
public class ReferenceClustering extends AbstractClustering
{
	@Override
	protected void assignLocalIndex(final OrdinaryCluster cluster, final int index)
	{
		cluster.setReferenceClusteringIndex(index);
	}

	@Override
	protected int getLocalIndex(final OrdinaryCluster cluster)
	{
		return cluster.getReferenceClusteringIndex();
	}

	@Override
	protected void clearIndex(final OrdinaryCluster cluster)
	{
		cluster.setReferenceClusteringIndex(AbstractCluster.INVALID_ID);
	}


}
