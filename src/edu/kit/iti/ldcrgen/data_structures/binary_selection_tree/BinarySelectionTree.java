package edu.kit.iti.ldcrgen.data_structures.binary_selection_tree;

import java.util.ArrayList;

import edu.kit.iti.ldcrgen.data_structures.JavaUtilRandomProvider;
import edu.kit.iti.ldcrgen.data_structures.RandomProvider;
import edu.kit.iti.ldcrgen.util.ListUtils;

/**
 * This binary selection tree is a randomized version of the general binary
 * tree.
 *
 * Each tree node is weighted. If the {@link #select()} method is called
 * upon the tree, then it will draw a random double value and this value
 * will define a path trough the tree leading to the selected tree node.
 *
 * Whenever the weight of a tree node has changed, the
 * {@link #updateWeight(TreeNode)} method has to be called in order to keep
 * the tree consistent.
 *
 * @author Roland Kluge
 *
 */
public class BinarySelectionTree
{
	private static final int INITIAL_CAPACITY = 100;
	private RandomProvider random = new JavaUtilRandomProvider();
	private ArrayList<TreeNode> binaryTree = new ArrayList<TreeNode>(INITIAL_CAPACITY);

	public double getWeight()
	{
		return this.size() > 0 ? this.getRoot().getAccumulatedWeight() : 0;
	}

	public boolean hasRoot()
	{
		return this.size() > 0;
	}

	public TreeNode getRoot()
	{
		if (!this.hasRoot())
		{
			throw new IllegalStateException("Tree is empty!");
		}
		return this.binaryTree.get(0);
	}

	TreeNode nodeAt(final int index)
	{
		return binaryTree.get(index);
	}

	/**
	 * Performs a weighted selection and returns the node.
	 *
	 * All nodes have a probability to be selected which is proportional
	 * to their weight.
	 *
	 * The tree shall not be empty!
	 *
	 * @return the selected node
	 */
	public TreeNode select()
	{

		double delta = random.nextDouble();

		// normalize value to [0,root.accWeight)
		delta *= this.getRoot().getAccumulatedWeight();

		TreeNode node = this.getRoot();
		assert null != node;
		while (node.hasLeft()
				&& (delta < node.getLeft().getAccumulatedWeight() || delta >= node.getLeft()
						.getAccumulatedWeight() + node.getWeight()))
		{
			if (delta < node.getLeft().getAccumulatedWeight())
			{
				assert node.hasLeft() : "Broken invariant: Expected a left child";

				node = node.getLeft();
			}
			else
			{
				assert delta >= node.getLeft().getAccumulatedWeight() + node.getWeight();
				assert node.hasRight() : "Broken invariant: Expected a right child";

				delta -= node.getLeft().getAccumulatedWeight() + node.getWeight();
				node = node.getRight();
			}
		}

		return node;
	}

	/**
	 * Inserts a new object into the tree and returns the containing tree
	 * node.
	 *
	 * @param element
	 *            the new element
	 * @return the new tree node for the element
	 */
	public TreeNode insert(final Weightable element)
	{
		final TreeNode newNode = new TreeNode(element, this, this.size());
		this.binaryTree.add(newNode);

		this.updateWeight(newNode);
		return newNode;
	}

	/**
	 * Convenience method which calls {@link #select()} and removes the
	 * resulting tree node.
	 *
	 * The same preconditions as for {@link #select()} apply.
	 */
	public TreeNode delete()
	{
		final TreeNode result = this.select();
		this.delete(result);
		return result;
	}

	/**
	 * Deletes a specific tree node.
	 *
	 * The node has to be contained in the tree!
	 *
	 * @param node
	 *            the node to be deleted
	 */
	public void delete(final TreeNode node)
	{
		/*
		 * The last node (if exists) takes the place of the node to be
		 * deleted.
		 * Afterwards two nodes have to update their weight and the weight
		 * of all their parents:
		 * 1. the last node (now located at the place of the original node
		 * 2. the former parent of the 'last node'
		 *
		 * Some special cases need to be covered:
		 * 1. last node is root
		 * 2. node is the parent of last node
		 * 3. last node is the node to be deleted
		 */
		final TreeNode lastNode = ListUtils.last(this.binaryTree);
		final TreeNode parentOfLastNode = lastNode.getParent();


		final int index = node.getIndex();

		ListUtils.moveLastTo(this.binaryTree, index);
		node.clearIndex();

		if (!node.equals(lastNode))
		{
			lastNode.setIndex(index);
			this.updateWeight(lastNode);
		}

		if (null != parentOfLastNode && !node.equals(parentOfLastNode))
		{
			this.updateWeight(parentOfLastNode);
		}
	}

	/**
	 * Notification method for stating that the weight of the given node
	 * has changed.
	 *
	 * If the weight has not changed since the last time, this call will
	 * have no effect.
	 *
	 * The node has to be contained in this tree!
	 *
	 * @param node
	 *            the node the weight of which has changed
	 */
	public void updateWeight(final TreeNode node)
	{
		TreeNode currentNode = node;
		while (currentNode.hasParent())
		{
			currentNode.recalculateAccumulatedWeight();
			currentNode = currentNode.getParent();
		}

		// reached the root
		assert currentNode.isRoot() : "Broken invariant!";
		currentNode.recalculateAccumulatedWeight();
	}

	/**
	 * Returns whether the given node is contained in this tree.
	 *
	 * @param node
	 *            the node to be checked
	 * @return whether it is contained
	 */
	public boolean contains(final TreeNode node)
	{
		return node != null //
			&& 0 <= node.getIndex() //
			&& node.getIndex() < this.binaryTree.size() //
			&& this.binaryTree.get(node.getIndex()).equals(node);
	}

	/**
	 * The number of tree nodes in this tree
	 *
	 * @return the size of this tree
	 */
	public int size()
	{
		return binaryTree.size();
	}

	public boolean isEmpty()
	{
		return this.size() == 0;
	}
}
