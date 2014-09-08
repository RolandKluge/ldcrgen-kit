package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>
 * The Fisher-Yates shuffle is a data structure which can be used to
 * generate random subsets of the set {0,...,n-1} where n is the maximum
 * size of the generated subset (it is called maxNum in the API of the
 * shuffle).
 * </p>
 * <p>
 * The shuffle always starts in a state were all elements are unselected.
 * It offers the operations delete (reduce subset by one element) and
 * select (add a new element to the set).
 * </p>
 * <p>
 * Besides the nondeterministic methods, their deterministic counterparts
 * exist in order to generate specific subsets.
 * </p>
 * <p>
 * The selected elements can be retrieved by means of the
 * getSelectedElements() method. Conversely, the getUnselectedElements()
 * method returns all elements which may still be chosen.
 * </p>
 * <p>
 * The shuffle can be resized to a new value for n by means of the resize()
 * method.
 * </p>
 * <p>
 * The memory consumption of the shuffle is linear in the number of
 * selected elements.
 * </p>
 *
 * <br/>
 * <p>
 * <b>Caution</b> As this is an internal data structure, the preconditions
 * in the comments have to be followed. Otherwise, even though the data
 * structure will still seem to work, it will be deeply corrupted and not
 * work properly anymore!
 * </p>
 *
 * @author Roland Kluge
 *
 */
public class FisherYatesShuffle
{

	private RandomProvider random = new JavaUtilRandomProvider();

	private long borderIndex; // pseudo code: i
	private long maxNum; // pseudo code: n
	private Map<Long, Long> replace;

	/**
	 * Initializes the Fisher-Yates shuffle with a given value for n.
	 *
	 * @param maxNum
	 *            the number of elements to choose from
	 */
	public FisherYatesShuffle(final long maxNum)
	{
		if (maxNum < 0)
		{
			throw new IllegalArgumentException("Maximum number of elements must be positive but was: " + maxNum);
		}

		this.maxNum = maxNum;
		this.borderIndex = 0;
		this.replace = new HashMap<Long, Long>(10000, 0.6f);
	}

	public FisherYatesShuffle(final long maxNum, final boolean useTreeMapInsteadOfHashMap)
	{
		this(maxNum);
		if (useTreeMapInsteadOfHashMap)
		{
			this.replace = new TreeMap<Long, Long>();
		}
	}

	/**
	 * The maximum number of elements which can be selected.
	 *
	 * @return the maximum size
	 */
	public long getMaxNum()
	{
		return this.maxNum;
	}

	/**
	 * Returns an element which may be deleted.
	 *
	 * Calling this method is far more effective than calling
	 * getSelectedElements() and then making a random choice.
	 *
	 * Note that the shuffle shall not be empty when calling this method!
	 *
	 * @return the proposed element
	 */
	public long proposeForDeletion()
	{
		final long drawnIndex = Math.abs(this.random.nextLong() % this.borderIndex);

		return this.replace.containsKey(drawnIndex) ? this.replace.get(drawnIndex) : drawnIndex;
	}

	/**
	 * Deletes the specific element.
	 *
	 * The shuffle must contain this element!
	 *
	 * @param i
	 *            the element to be deleted
	 */
	public void delete(final long i)
	{

		if (this.replace.containsKey(i))
		{
			if (! (i >= this.borderIndex && i < this.maxNum))
			{
				throw new IllegalArgumentException("Element " + i + " cannot be deleted as it is not selected.");
			}
			deleteByIndex(this.replace.get(i));
		}
		else
		{
			if (! (i >= 0 && i < this.borderIndex))
			{
				throw new IllegalArgumentException("Element " + i + " cannot be deleted as it is not selected.");
			}
			deleteByIndex(i);
		}
	}

	/**
	 * Deletes an element which has been chosen uniformly at random.
	 *
	 * The shuffle shall not be empty!
	 *
	 * @return the deleted element
	 */
	public long delete()
	{

		final long drawnIndex = Math.abs(this.random.nextLong() % this.borderIndex);

		return deleteByIndex(drawnIndex);
	}

	/*
	 * Takes an index in the range of 0 to borderIndex-1 and deletes
	 * the element being stored at this index.
	 */
	private long deleteByIndex(final long index)
	{
		assert 0 <= index && index < this.borderIndex : "Index: " + index + " - Border: "
				+ this.borderIndex + " - MaxNum: " + this.maxNum + " replace: "
			+ this.replace.get(index);

		final long preBorderIndex = this.borderIndex - 1;
		final long preBorderElement = replace.containsKey(preBorderIndex) ? this.replace
				.get(preBorderIndex) : preBorderIndex;
		final long drawnElement = replace.containsKey(index) ? this.replace.get(index) : index;

		final long result = drawnElement;
		if (preBorderIndex == index)
		{
			assert preBorderElement == drawnElement;

			if (preBorderElement != preBorderIndex)
			{
				replace.remove(preBorderElement);
				replace.remove(preBorderIndex);
			}
		}
		else
		{
			createReplacePointer(preBorderElement, index);

			if (preBorderElement != preBorderIndex) // Cases 2 and 4
			{
				replace.remove(preBorderIndex);
			}

			if (drawnElement != index) // Cases 3 and 4
			{
				replace.remove(drawnElement);
			}

		}

		--this.borderIndex;

		return result;
	}

	/**
	 * Returns an element which may still be selected.
	 *
	 * Calling this method is far more effective than calling
	 * getUnselectedElements() and then making a random choice.
	 *
	 * Note that the shuffle shall not be full when calling this method!
	 *
	 * @return the proposed element
	 */
	public long proposeForSelection()
	{
		final long drawnIndex = this.borderIndex
			+ Math.abs(this.random.nextLong() % (maxNum - this.borderIndex));

		return this.replace.containsKey(drawnIndex) ? this.replace.get(drawnIndex) : drawnIndex;
	}

	/**
	 * Selects a specific element.
	 *
	 * The given element may be in the range of 0 to maxNum - 1
	 * and it may not be contained in the shuffle yet.
	 *
	 * @param element
	 *            the element to be selected
	 *
	 * @see #maxNum()
	 */
	public void select(final long element)
	{
		if (element >= this.maxNum)
		{
			throw new IllegalArgumentException("Element " + element + "is too large: Maximum allowed value is " + this.maxNum);
		}

		if (this.replace.containsKey(element))
		{
			assert 0 <= element && element < this.borderIndex;
			selectByIndex(this.replace.get(element));
		}
		else
		{
			selectByIndex(element);
		}
	}

	/**
	 * Selects an element uniformly at random.
	 *
	 * The shuffle shall not be full!
	 *
	 * @return the selected element
	 */
	public long select()
	{
		if (this.isFull())
		{
			throw new IllegalStateException("Maximum number of elements has already been selected.");
		}

		final long drawnIndex = this.borderIndex + Math.abs(this.random.nextLong())
			% (maxNum - this.borderIndex);

		return selectByIndex(drawnIndex);
	}

	/*
	 * Takes an index in the range of borderIndex to maxNum-1 and selects
	 * the element being stored at this index.
	 */
	private long selectByIndex(final long index)
	{
		assert this.borderIndex <= index && index < this.maxNum : "idx: " + index + " i: "
			+ borderIndex + " max: " + maxNum;

		final long borderElement = replace.containsKey(borderIndex) ? this.replace.get(borderIndex)
				: borderIndex;
		final long drawnElement = replace.containsKey(index) ? this.replace.get(index) : index;

		final long result = drawnElement;
		if (borderIndex == index) // Special case: i = j
		{
			assert borderElement == drawnElement;

			if (borderElement != borderIndex)
			{
				replace.remove(borderElement);
				replace.remove(borderIndex);
			}
		}
		else
		{
			createReplacePointer(index, borderElement);

			if (borderElement != borderIndex) // Cases 2 and 4
			{
				replace.remove(borderIndex);
			}

			if (drawnElement != index) // Cases 3 and 4
			{
				replace.remove(drawnElement);
			}
		}

		++this.borderIndex;

		return result;
	}

	/*
	 * Creates a bidirectional pointer indicating that the elements at the
	 * given positions have exchanged their position.
	 */
	private void createReplacePointer(final long preBorderElement, final long index)
	{
		replace.put(preBorderElement, index);
		replace.put(index, preBorderElement);
	}

	/**
	 * Returns whether the given element is contained in the set of
	 * selected elements.
	 *
	 * @param element
	 *            the element
	 * @return whether it is selected
	 */
	public boolean contains(final long element)
	{
		/*
		 * An element identified by its homonymous index exists in one of
		 * the following cases:
		 * 1. 'element' is in place and left of the border
		 * 2. 'element' is not in place and on the right-hand side.
		 */
		return (element >= this.borderIndex && this.replace.containsKey(element)) || //
			(element < this.borderIndex && element >= 0 && !this.replace.containsKey(element));
	}

	/**
	 * The shuffle is empty if no element has been selected.
	 *
	 * @return whether this is empty
	 */
	public boolean isEmpty()
	{
		return 0 == this.borderIndex;
	}

	/**
	 * The shuffle is full if all possible elements 0 to maxNum-1
	 * have been selected.
	 *
	 * @return whether this is full
	 */
	public boolean isFull()
	{
		return this.borderIndex == this.maxNum;
	}

	/**
	 * Returns the number of selected elements.
	 *
	 * @return
	 */
	public long getSelectionCount()
	{
		return this.borderIndex;
	}

	/**
	 * Returns a list with all selected elements.
	 *
	 * This operation is costly and takes time O(#selected elements).
	 *
	 * @return the set of selected elements
	 */
	public ArrayList<Long> getSelectedElements()
	{
		final ArrayList<Long> result = new ArrayList<Long>((int) this.getSelectionCount());

		for (long i = 0; i < this.borderIndex; ++i)
		{
			final long element = this.replace.containsKey(i) ? this.replace.get(i) : i;
			result.add(element);
		}

		return result;
	}

	/**
	 * Returns a list with all non-selected elements.
	 *
	 * This operation is costly and takes time O(maxNum - #selected
	 * elements).
	 *
	 * @return the set of non-selected elements
	 */
	public List<Long> getUnselectedElements()
	{
		final ArrayList<Long> result = new ArrayList<Long>(
			(int) (maxNum - this.getSelectionCount()));

		for (long i = this.borderIndex; i < this.maxNum; ++i)
		{
			final long element = this.replace.containsKey(i) ? this.replace.get(i) : i;
			result.add(element);
		}

		assert result.size() == maxNum - this.getSelectionCount();
		return result;
	}

	/**
	 * Resizes the shuffle to the given size.
	 * If the new size is smaller than the previous value of maxNum, the
	 * shuffle shrinks and all elements with indices &gt;= size will be
	 * deleted.
	 *
	 * @param size
	 *            the new size
	 */
	public void resize(final long size)
	{

		if (size < this.maxNum) // shrink operation
		{
			for (long i = size; i < this.maxNum; ++i)
			{
				if (this.contains(i))
				{
					this.delete(i);
				}
			}
		}

		this.fastResize(size);

	}

	/**
	 * Offers the same functionality as {@link #resize(long)} but presumes
	 * that all elements in the range by which the shuffle potentially shrinks
	 * have been removed.
	 *
	 * @param size
	 *            the new size
	 */
	public void fastResize(final long size)
	{
		if (size < 0)
		{
			throw new IllegalArgumentException("Maximum number of elements must be positive but was: " + size);
		}
		this.maxNum = size;
	}

	/**
	 * Removes all elements from the shuffle.
	 * The maximum number stays the same.
	 */
	public void clear()
	{
		this.replace.clear();
		this.borderIndex = 0;
	}

}
