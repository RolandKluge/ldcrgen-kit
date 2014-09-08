package edu.kit.iti.ldcrgen.data_structures.binary_selection_tree;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.BinarySelectionTree;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.TreeNode;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.Weightable;
import edu.kit.iti.ldcrgen.util.BooleanUtils;

public class TreeNodeTest
{
	@Test
	public void testEqualsAndHashCode()
	{
		final BinarySelectionTree tree = new BinarySelectionTree();
		final List<TreeNode> nodes = new ArrayList<TreeNode>();
		for (int i = 0; i < 100; ++i)
		{
			final Weightable element = new WeightDummy((i % 10) / 2.5);
			nodes.add(tree.insert(element));
		}

		for (int i = 0; i < nodes.size(); ++i)
		{
			// reflexivity
			final TreeNode fst = nodes.get(i);
			Assert.assertEquals(fst, fst);
			Assert.assertFalse(fst.equals(null));

			for (int j = 0; j < nodes.size(); ++j)
			{
				final TreeNode snd = nodes.get(j);

				// symmetry
				Assert.assertTrue(BooleanUtils.equivalent(fst.equals(snd), snd.equals(fst)));

				//
				Assert.assertTrue("fst: " + fst + " snd: " + snd,
					BooleanUtils.implies(fst.equals(snd), fst.hashCode() == snd.hashCode()));

				for (int k = 0; k < nodes.size(); ++k)
				{
					final TreeNode thd = nodes.get(k);

					// transitivity
					Assert.assertTrue(BooleanUtils.implies(fst.equals(snd) && snd.equals(thd),
						fst.equals(thd)));
				}
			}
		}
	}
}
