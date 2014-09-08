package edu.kit.iti.ldcrgen.data_structures;

import org.junit.Assert;
import org.junit.Test;

public class AbstractClusteringTest
{


	@Test
	public void testEqualsLight()
	{
		final AbstractClustering gt1 = new GroundTruth();
		final AbstractClustering refcl1 = new ReferenceClustering();

		final OrdinaryCluster cl1 = new OrdinaryCluster(DCRGraphTest.randomGraph(), 0.5);
		final OrdinaryCluster cl2 = new OrdinaryCluster(DCRGraphTest.randomGraph(), 0.6);

		gt1.add(cl1);
		gt1.add(cl2);
		refcl1.add(cl1);
		refcl1.add(cl2);

		Assert.assertFalse(gt1.equals(null));
		Assert.assertFalse(gt1.equals("test"));
		Assert.assertFalse(gt1.equals(new Object()));


		Assert.assertEquals(gt1, gt1);
		Assert.assertEquals(refcl1, refcl1);

		Assert.assertEquals(gt1, refcl1);
		Assert.assertEquals(refcl1, gt1);

		Assert.assertEquals(gt1.hashCode(), refcl1.hashCode());

	}
}
