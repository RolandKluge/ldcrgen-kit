package edu.kit.iti.ldcrgen.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class which offers operations upon lists.
 *
 * Note that none of the methods expecting a list will accept a
 * <code>null</code> reference!
 *
 * @author Roland Kluge
 */
public final class ListUtils
{
	/**
	 * Gives the order in which elements shall be sorted
	 */
	private enum SortOrder
	{
		ASC,
		DESC;
	}

	private ListUtils()
	{
		// utilitiy class -> hidden constructor
	}

	/**
	 * Copies the last element of the list to the given position.
	 * The position must be a valid position in the range of 0 to
	 * list.size() (exclusive).
	 *
	 * This operation will not change the size of the list!
	 *
	 * @param list
	 *            the list to be modified. Shall not be empty!
	 * @param position
	 *            the position where the last element shall go. Shall be in
	 *            the range of [0,list.size()).
	 */
	public static <T> void copyLastTo(final List<T> list, final int position)
	{
		assert null != list;
		assert 0 <= position && position < list.size();

		list.set(position, ListUtils.last(list));
	}


	/**
	 * Similar to {@link copyLastTo} but also removes the last element
	 * after
	 * copying it to the respective position.
	 *
	 * @param list
	 *            the list to be modified
	 * @param position
	 *            the position where the last element shall go
	 * @return
	 */
	public static <T> T moveLastTo(final List<T> list, final int position)
	{
		assert null != list;
		assert 0 <= position && position < list.size() : //
			"pos: " + position + " size: " + list.size();

		final T result = list.get(position);
		ListUtils.copyLastTo(list, position);
		list.remove(list.size() - 1);

		return result;
	}

	/**
	 * Maps a list of one numeric type to another numeric type.
	 *
	 * Note that this may cause information loss, e.g., when converting
	 * from Double to Integer.
	 *
	 * Currently, only conversion among Integer and Double are possible.
	 * All other conversions will trigger an exception.
	 *
	 * @param list
	 *            the original list
	 * @param dst
	 *            the type of which the result shall be
	 * @return a new list with the converted values
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number, S extends Number> List<S> mapType(final List<T> list,
			final Class<S> dst)
	{
		assert null != list;

		final List<S> result = new ArrayList<S>(list.size());
		final Iterator<T> iter = list.iterator();
		while (iter.hasNext())
		{
			if (Double.class == dst)
			{
				result.add((S) Double.valueOf(iter.next().doubleValue()));
			}
			else if (Integer.class == dst)
			{
				result.add((S) Integer.valueOf(iter.next().intValue()));
			}
			else
			{
				throw new UnsupportedOperationException("Cannot convert to the given type!");
			}
		}
		return result;
	}

	/**
	 * Sums up the elements in the list.
	 *
	 * <code>null</code> will be counted as 0.
	 *
	 * @param list
	 *            the list of which the elements shall be summed up
	 * @return the sum over all elements in the list
	 */
	public static double sumUp(final List<Double> list)
	{
		assert null != list;

		double result = 0;
		final Iterator<Double> iter = list.iterator();
		while (iter.hasNext())
		{
			final Double next = iter.next();
			if (null != next)
			{
				result += next.doubleValue();
			}
		}
		return result;
	}

	/**
	 * Calculates the arithmetic mean of the given list.
	 *
	 * The list shall not be empty!
	 *
	 * @param list
	 *            the non-empty list of which the arithmetic mean is to be
	 *            calculated
	 * @return the arithmetic mean
	 */
	public static double arithmeticMean(final List<Double> list)
	{
		assert null != list;
		assert !list.isEmpty();

		double result = 0;
		final Iterator<Double> iter = list.iterator();
		while (iter.hasNext())
		{
			result += iter.next().doubleValue();
		}
		result /= list.size();
		return result;
	}

	/**
	 * Calculates the variance of the given list.
	 *
	 * The list has to contain at least 2 elements.
	 *
	 * @param list
	 *            the list
	 * @return the variance
	 */
	public static double variance(final List<Double> list)
	{
		assert null != list;
		assert list.size() > 1;

		final double mean = ListUtils.arithmeticMean(list);
		double result = 0;
		final Iterator<Double> iter = list.iterator();
		while (iter.hasNext())
		{
			final double current = iter.next();
			result += (current - mean) * (current - mean);
		}
		result /= list.size();
		return result;
	}

	/**
	 * Returns whether all elements of the given list are equal.
	 *
	 * The method will return <code>true</code> for the empty list.
	 * For non-empty lists the result is true, if for all elements
	 * x,y it holds that: <code>x.equals(y)</code>.
	 *
	 * Note that <code>null</code> is not equal to anything!
	 *
	 * @param list
	 *            the list to be checked
	 * @return whether all elements are equal
	 */
	public static <T> boolean allEqual(final List<T> list)
	{
		return list.isEmpty() || ListUtils.allEqual(list, list.get(0));
	}

	/**
	 * This method is very similar to allEqual except for the fact that
	 * all elements of the list have to be equal to the given element.
	 *
	 * @param list
	 *            the list to be checked
	 * @param element
	 *            the item to compare each item of the list to
	 * @return whether all elements of the list are equal to
	 *         <code>element</code>
	 */
	public static <T> boolean allEqual(final List<T> list, final T element)
	{
		boolean result = true;
		result &= (element != null);
		final Iterator<T> iter = list.iterator();
		while (iter.hasNext() && result)
		{
			result &= element.equals(iter.next());
		}
		return result;
	}

	/**
	 * Returns the list of prefix sums of the given list of values.
	 *
	 * Let <code>values</code>=(v1,v2,v3,...,vn) then the result will be a
	 * list of size n and with the content
	 * (v1,v1+v2,v1+v2+v3,...,sum_i(vi)).
	 * Therewith, the last field of the resulting list ist the sum of all
	 * elements.
	 *
	 * If the input is empty so is the output list.
	 *
	 * @param values
	 *            the list to build the prefix sum list from
	 * @return the prefix sum list
	 */
	public static List<Integer> inclusivePrefixSum(final List<Integer> values)
	{
		final ArrayList<Integer> result = new ArrayList<Integer>(values.size());
		final Iterator<Integer> iterator = values.iterator();

		int temp = 0;
		while (iterator.hasNext())
		{
			temp += iterator.next();
			result.add(temp);
		}

		return result;
	}

	/**
	 * Returns whether the given list is sorted ascendingly.
	 *
	 * A list is sorted ascendingly if for two subsequent elements
	 * x,y the function x.compareTo(y) returns 0 or -1.
	 *
	 * @param list
	 *            the list to be checked
	 * @return whether the list is sorted
	 */
	public static <T extends Comparable<T>> boolean isSortedAscending(final List<T> list)
	{
		return isSorted(list, SortOrder.ASC);
	}

	/**
	 * Returns whether the given list is sorted descendingly.
	 *
	 * A list is sorted descendingly if for two subsequent elements
	 * x,y the function x.compareTo(y) returns 0 or 1.
	 *
	 * @param list
	 *            the list to be checked
	 * @return whether the list is sorted
	 */
	public static <T extends Comparable<T>> boolean isSortedDescending(final List<T> list)
	{
		return isSorted(list, SortOrder.DESC);
	}

	private static <T extends Comparable<T>> boolean isSorted(final List<T> list,
			final SortOrder order)
	{
		boolean result = true;
		final Iterator<T> firstIt = list.iterator();
		final Iterator<T> secondIt = list.iterator();
		if (secondIt.hasNext())
		{
			secondIt.next();
			while (result && secondIt.hasNext())
			{
				if (SortOrder.ASC == order)
				{
					result &= firstIt.next().compareTo(secondIt.next()) <= 0;
				}
				else
				{
					result &= firstIt.next().compareTo(secondIt.next()) >= 0;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the last element of a non-empty list
	 *
	 * @param list
	 *            the non-empty list
	 * @return the last element
	 */
	public static <T> T last(final List<T> list)
	{
		assert null != list;
		assert !list.isEmpty();

		return list.get(list.size() - 1);
	}

	/**
	 * Appends all elements which the iterator is willing to produce to the
	 * given list.
	 *
	 * @param iter
	 *            the iterator
	 * @param list
	 *            the list
	 */
	public static <T> void appendTo(final Iterator<T> iter, final List<T> list)
	{
		assert null != iter;
		assert null != list;

		while (iter.hasNext())
		{
			list.add(iter.next());
		}
	}

	/**
	 * Creates a list of all elements returned by the given iterator.
	 *
	 * @param iter
	 *            the iterator
	 * @return the resulting list
	 */
	public static <T> List<T> toList(final Iterator<T> iter)
	{
		final ArrayList<T> list = new ArrayList<T>();
		ListUtils.appendTo(iter, list);
		return list;
	}

	/**
	 * Splits the list at the given position.
	 *
	 * The first list will contain all elements from the original list
	 * located at indices 0 to i(exclusive) and consequently the second
	 * list
	 * will contain all elements with indices greater or equal to i.
	 *
	 * For negative indices, the first list will be empty.
	 * For indices larger than the size of the list the second list will be
	 * empty.
	 *
	 * @param list
	 *            the list to be split
	 * @param i
	 *            the index at which to split
	 * @return the two resulting lists
	 */
	public static <T> Pair<List<T>> split(final List<T> list, final int i)
	{
		final Pair<List<T>> result = new Pair<List<T>>(new ArrayList<T>(), new ArrayList<T>());
		int counter = 0;
		final Iterator<T> iter = list.iterator();
		while (iter.hasNext())
		{
			final T elem = iter.next();
			if (counter < i)
			{
				result.getFirst().add(elem);
			}
			else
			{
				result.getSecond().add(elem);
			}

			++counter;
		}

		return result;
	}

	/**
	 * Produces the elementwise sum of two lists.
	 *
	 * The result will be of the length of the shortest of the two lists.
	 *
	 * For example, the elementwise sum of (1,2,3), (9,11,13,17,23) is
	 * (10, 13, 16).
	 *
	 * @param first the first list
	 * @param snd the second list
	 * @return the list containing the elementwise sum of both
	 */
	public static List<Double> elementwiseSum(final List<Double> first,
		final List<Double> snd)
	{
		assert null != first;
		assert null != snd;

		final List<Double> result = new ArrayList<Double>(Math.min(first.size(), snd.size()));

		final Iterator<Double> fstIter = first.iterator();
		final Iterator<Double> sndIter = snd.iterator();

		while (fstIter.hasNext() && sndIter.hasNext())
		{
			result.add(fstIter.next() + sndIter.next());
		}

		return result;
	}

	/**
	 * Creates a list of all elements of the given list divided by the divisor.
	 *
	 * As usual, the divisor must not be zero.
	 *
	 * @param list the list of values
	 * @param divisor the divisor
	 * @return the list of quotients
	 */
	public static List<Double> elementwiseQuotient(final List<Double> list, final double divisor)
	{
		assert null != list;
		assert 0.0 != divisor;

		final List<Double> result = new ArrayList<Double>(list.size());

		final Iterator<Double> iter = list.iterator();

		while (iter.hasNext())
		{
			result.add(iter.next() / divisor);
		}

		return result;
	}
}
