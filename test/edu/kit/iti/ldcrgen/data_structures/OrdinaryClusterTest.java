package edu.kit.iti.ldcrgen.data_structures;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class OrdinaryClusterTest
{
	@Test
	public void testEqualsLight()
	{
		final DCRGraph graph = DCRGraphTest.randomGraph();
		final OrdinaryCluster cl = new OrdinaryCluster(graph , 0.5);

		Assert.assertFalse(cl.equals(null));
		Assert.assertFalse(cl.equals("test"));

		Assert.assertEquals(cl.hashCode(), cl.hashCode());
	}

	//TODO rkluge bad test
	@Ignore
	@Test(expected=UnsupportedOperationException.class)
	public void testcClusterSpecIterator()
	{
		final DCRGraph graph = DCRGraphTest.randomGraph();

		Assert.assertTrue(0 < graph.getClusterCount());
		final OrdinaryCluster cl = graph.getGroundTruth().getCluster(0);
		final OrdinaryCluster other = graph.getGroundTruth().getCluster(1);

		cl.interClusterEdgeIterator(other).remove();
	}
}
