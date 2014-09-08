package edu.kit.iti.ldcrgen.data_structures.binary_selection_tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.BinarySelectionTree;
import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.TreeNode;

/**
 *
 * @author roland
 * @see BinarySelectionTree
 */
public class BinarySelectionTreeTest
{
	private static double addAll(final List<TreeNode> list)
	{
		double result = 0.0;
		final Iterator<TreeNode> it = list.iterator();
		while (it.hasNext())
		{
			result += it.next().getWeight();
		}
		return result;
	}

	private static List<TreeNode> bfs(final BinarySelectionTree tree)
	{
		final List<TreeNode> result = new ArrayList<TreeNode>();
		final Queue<TreeNode> queue = new ArrayDeque<TreeNode>();

		if (tree.hasRoot())
		{
			queue.add(tree.getRoot());
			while (!queue.isEmpty())
			{
				final TreeNode current = queue.poll();
				result.add(current);
				if (current.hasLeft())
				{
					queue.add(current.getLeft());
				}
				if (current.hasRight())
				{
					queue.add(current.getRight());
				}
			}
		}

		return result;
	}

	@Test(
			expected = IllegalStateException.class)
	public void testInitialState()
	{
		final BinarySelectionTree tree = new BinarySelectionTree();
		Assert.assertEquals(0, tree.size());
		Assert.assertFalse(tree.contains(null));
		Assert.assertEquals(0.0, tree.getWeight());
		Assert.assertFalse(tree.hasRoot());

		tree.getRoot();
	}

	@Test
	public void testOneElementRetrieval()
	{
		final WeightDummy weight1 = new WeightDummy(1.0);
		final BinarySelectionTree tree = new BinarySelectionTree();

		final TreeNode node1 = tree.insert(weight1);
		Assert.assertEquals(1, tree.size());

		Assert.assertEquals(weight1, node1.getElement());
		Assert.assertTrue(tree.contains(node1));
		Assert.assertEquals(weight1.getWeight(), node1.getWeight());
		Assert.assertEquals(weight1.getWeight(), node1.getAccumulatedWeight());

		Assert.assertEquals(weight1.getWeight(), tree.getWeight());

		Assert.assertEquals(node1, tree.getRoot());
		Assert.assertNull(node1.getLeft());
		Assert.assertNull(node1.getRight());
		Assert.assertNull(node1.getParent());

		/* Delete */
		Assert.assertTrue(tree.contains(node1));
		tree.delete(node1);
		Assert.assertEquals(0, tree.size());
		Assert.assertEquals(0.0, tree.getWeight());
		Assert.assertFalse(tree.hasRoot());

	}

	@Test
	public void testFewElementsRetrieval()
	{
		final WeightDummy weight1 = new WeightDummy(1.0);
		final WeightDummy weight2 = new WeightDummy(2.0);
		final WeightDummy weight3 = new WeightDummy(3.0);
		BinarySelectionTree tree;
		/* Part 1 */
		tree = new BinarySelectionTree();

		final TreeNode node1 = tree.insert(weight1);

		final TreeNode node2 = tree.insert(weight2);
		Assert.assertEquals(2, tree.size());
		Assert.assertTrue(tree.contains(node2));
		Assert.assertEquals(weight1.getWeight() + weight2.getWeight(), tree.getWeight());

		final TreeNode node3 = tree.insert(weight3);
		Assert.assertEquals(3, tree.size());
		Assert.assertTrue(tree.contains(node3));
		Assert.assertEquals(weight1.getWeight() + weight2.getWeight() + weight3.getWeight(),
				tree.getWeight());

		final List<TreeNode> originalNodes = Arrays.asList(node1, node2, node3);
		final List<TreeNode> probedNodes = bfs(tree);
		Assert.assertTrue(probedNodes.containsAll(originalNodes));
		Assert.assertTrue(originalNodes.containsAll(probedNodes));
	}

	@Test
	public void testManyElementsRetrieval()
	{
		final int num = (int) 1e3;

		final List<TreeNode> originalNodes = new ArrayList<TreeNode>(num);
		BinarySelectionTree tree = new BinarySelectionTree();

		double expectedWeight = 0;
		for (int i = 0; i < num; ++i)
		{
			// final WeightDummy dummy = new WeightDummy(i);
			final WeightDummy dummy = new WeightDummy(Math.random());
			final TreeNode node = tree.insert(dummy);
			expectedWeight += dummy.getWeight();

			originalNodes.add(node);
			Assert.assertEquals(expectedWeight, tree.getWeight(), 1e-7);
		}

		/*
		 * Check elements and parent-child relationship
		 */
		final List<TreeNode> probedNodes = bfs(tree);

		Assert.assertEquals(originalNodes.size(), probedNodes.size());
		Assert.assertEquals(probedNodes.size(), tree.size());
		for (final TreeNode probe : probedNodes)
		{
			Assert.assertTrue(tree.contains(probe));
		}

		Assert.assertTrue(probedNodes.containsAll(originalNodes));
		Assert.assertTrue(originalNodes.containsAll(probedNodes));

		TreeNode probedRoot = null;
		for (final TreeNode probe : probedNodes)
		{
			// assert that there exists only one root
			if (probe.isRoot())
			{
				Assert.assertNull(probedRoot);
				probedRoot = probe;
			}
		}

		/*
		 * Check weights
		 */
		double sumOfWeights = addAll(originalNodes);
		Assert.assertEquals(sumOfWeights, tree.getWeight(), 1e-7);

		/*
		 * Delete elements
		 */
		for (int i = 0; i < num; ++i)
		{
			final TreeNode current = originalNodes.get(i);

			Assert.assertTrue(tree.contains(current));
			tree.delete(current);
			Assert.assertFalse(tree.contains(current));

			expectedWeight -= current.getWeight();

			Assert.assertEquals(num - i - 1, tree.size());
			Assert.assertEquals(expectedWeight, tree.getWeight(), 1e-7);
		}
	}

	@Test
	public void testLargeRandom()
	{
		final int maxSize = (int) 1e4;
		final int numOps = (int) 1e5;
		final Random random = new Random(1);

		final BinarySelectionTree tree = new BinarySelectionTree();
		for (int i = 0; i < numOps; ++i)
		{
			final int remainingSize = maxSize - tree.size();

			if (random.nextInt(maxSize) < remainingSize)
			{
				tree.insert(new WeightDummy(random.nextDouble()));
				Assert.assertEquals(maxSize - tree.size(), remainingSize - 1);
			}
			else
			{
				tree.delete();
				Assert.assertEquals(maxSize - tree.size(), remainingSize + 1);
			}
		}

	}

	@Test
	public void testMethodsWithNonContainedElements()
	{
		final BinarySelectionTree treeUnderTest = new BinarySelectionTree();
		final BinarySelectionTree sourceTree = new BinarySelectionTree();

		Assert.assertFalse(//
			treeUnderTest.contains(sourceTree.insert(new WeightDummy(Math.random()))));

	}
}
