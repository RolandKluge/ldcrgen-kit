package edu.kit.iti.ldcrgen.data_structures.binary_selection_tree;

/**
 * This is a node in the weighted, randomized binary search tree.
 * It is the interface to its weighted object.
 *
 * @author Roland Kluge
 */
public class TreeNode
{
	private final BinarySelectionTree tree;
	private final Weightable element;
	private int index;
	private double accumulatedWeight;

	TreeNode(final Weightable element, final BinarySelectionTree tree, final int index)
	{
		this.element = element;
		this.tree = tree;
		this.index = index;
		this.recalculateAccumulatedWeight();
	}

	/**
	 * Must be called if the weight of any one of the subtrees has changed.
	 */
	void recalculateAccumulatedWeight()
	{
		this.accumulatedWeight = this.element.getWeight();

		if (this.hasLeft())
		{
			this.accumulatedWeight += this.getLeft().getAccumulatedWeight();
		}

		if (this.hasRight())
		{
			this.accumulatedWeight += this.getRight().getAccumulatedWeight();
		}


	}

	/**
	 * Sets the index of this node in the tree data structure.
	 *
	 * @param index
	 *            the new index
	 */
	void setIndex(final int index)
	{
		this.index = index;
	}

	/**
	 * Invalidates the index of this tree node.
	 */
	void clearIndex()
	{
		this.index = -1;
	}

	/**
	 * Returns the internal index of the tree node.
	 *
	 * @return
	 */
	int getIndex()
	{
		return this.index;
	}

	public Weightable getElement()
	{
		return this.element;
	}

	public double getWeight()
	{
		return element.getWeight();
	}

	/**
	 * Returns the stored accumulated weight.
	 *
	 * This weight must be kept up-to-date by calling
	 * {@link #recalculateAccumulatedWeight()} if the accumulated weight of
	 * one of the subtrees has changed.
	 *
	 * @return the accumulated weight of this node
	 *
	 * @see #recalculateAccumulatedWeight()
	 */
	public double getAccumulatedWeight()
	{
		return this.accumulatedWeight;
	}

	public BinarySelectionTree getTree()
	{
		return this.tree;
	}


	public TreeNode getParent()
	{
		if (this.isRoot())
		{
			return null;
		}
		else
		{
			return this.tree.nodeAt((this.index - 1) / 2);
		}
	}

	public TreeNode getLeft()
	{
		if (!this.hasLeft())
		{
			return null;
		}
		else
		{
			return this.tree.nodeAt(2 * this.index + 1);
		}
	}

	public TreeNode getRight()
	{
		if (!this.hasRight())
			return null;
		else
			return this.tree.nodeAt(2 * this.index + 2);
	}

	public boolean isRoot()
	{
		return !this.hasParent();
	}

	public boolean hasParent()
	{
		return 0 != this.index;
	}

	public boolean hasLeft()
	{
		return this.tree.size() > 2 * this.index + 1;
	}

	public boolean hasRight()
	{
		return this.tree.size() > 2 * this.index + 2;
	}

	/**
	 * This method may be called upon the node in order to
	 * signal that its weight has changed.
	 */
	public void fireWeightChanged()
	{
		this.tree.updateWeight(this);
	}


	@Override
	public int hashCode()
	{
		return element.hashCode();
	}

	/**
	 * Two tree nodes are equal if they contain equal objects.
	 * Their positions or their containing trees are not considered.
	 */
	@Override
	public boolean equals(final Object obj)
	{
		boolean result;
		if (this == obj)
		{
			result = true;
		}
		if (null == obj || obj.getClass() != this.getClass())
		{
			result = false;
		}
		else
		{
			result = this.element.equals(((TreeNode) obj).element);
		}
		return result;
	}


	@Override
	public String toString()
	{
		return "TreeNode [index=" + index + ", element=" + element + ", accumulatedWeight="
				+ accumulatedWeight + "]";
	}

}
