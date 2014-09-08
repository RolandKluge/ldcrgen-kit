package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.BooleanUtils;

public class NodeTest
{

	// rather silly :-)
	@Test
	public void testToString()
	{
		new Node().toString();
	}

	@Test
	public void testEqualsAndHashCode()
	{
		final List<Node> nodes = new ArrayList<Node>();
		final DCRGraph graph = new DCRGraph();
		final Random random = new Random();
		for (int i = 0; i < 10; ++i)
		{
			final Node node = new Node();
			node.setGtCluster(new OrdinaryCluster(graph, random.nextDouble()));
			node.setGtClIndex(random.nextInt());
			node.setPsClIndex(random.nextInt());
			node.setOperationIndex(random.nextInt());
			nodes.add(node);
		}

		nodes.add(null);
		nodes.add(null);
		nodes.add(null);

		for (int i = 0; i < nodes.size(); ++i)
		{
			// reflexivity
			final Node fst = nodes.get(i);
			if (fst != null)
			{
				Assert.assertEquals(fst, fst);
				Assert.assertFalse(fst.equals(null));

				for (int j = 0; j < nodes.size(); ++j)
				{
					final Node snd = nodes.get(j);

					if (snd != null)
					{
						// symmetry
						Assert
							.assertTrue(BooleanUtils.equivalent(fst.equals(snd), snd.equals(fst)));

						//
						Assert.assertTrue(BooleanUtils.implies(fst.equals(snd),
							fst.hashCode() == snd.hashCode()));

						for (int k = 0; k < nodes.size(); ++k)
						{
							final Node thd = nodes.get(k);

							// transitivity
							Assert.assertTrue(BooleanUtils.implies(
								fst.equals(snd) && snd.equals(thd),
								fst.equals(thd)));
						}
					}
					else
					{
						Assert.assertFalse(fst.equals(snd));
					}
				}
			}
		}

		for (int i = 0; i < nodes.size(); ++i)
		{
			final Node node = nodes.get(i);
			if (node != null)
			{
				Assert.assertFalse(node.equals(new Object()));
				Assert.assertFalse(node.equals("Hallo Test!"));
			}
		}
	}
}
