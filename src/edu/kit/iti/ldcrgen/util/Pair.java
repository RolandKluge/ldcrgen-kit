package edu.kit.iti.ldcrgen.util;

/**
 * A pair in this context is a 2-tupel of non-null objects.
 *
 * @author Roland Kluge
 */
public class Pair<T>
{
	private final T first;
	private final T second;

	/**
	 * Initializes a pair with two elements both of which must not be
	 * <code>null</code>.
	 *
	 * @param first
	 *            the first, non-null element
	 * @param second
	 *            the second, non-null element
	 */
	public Pair(final T first, final T second)
	{
		this.first = first;
		this.second = second;
	}

	/**
	 * Returns the first element.
	 *
	 * @return the first element
	 */
	public T getFirst()
	{
		return first;
	}


	/**
	 * Returns the second element.
	 *
	 * @return the second element
	 */
	public T getSecond()
	{
		return second;
	}

	/**
	 * Given one element of the pair, this method returns the other one.
	 *
	 * If the given element is not contained in this pair at all,
	 * the result will be <code>null</code>.
	 *
	 * @param thisOne
	 *            the one object
	 * @return the other object or <code>null</code>
	 */
	public T getOther(final T thisOne)
	{
		assert null != thisOne;
		T result = null;
		if (thisOne.equals(this.getFirst()))
		{
			result = this.getSecond();
		}
		else if (thisOne.equals(this.getSecond()))
		{
			result = this.getFirst();
		}
		return result;
	}

	/**
	 * Returns the index of the given object.
	 *
	 * The first position is indexed with 0 and the second one with 1.
	 * If the object cannot be found, then the result is negative.
	 *
	 * @param toBeFound
	 *            the object to be found
	 * @return the index of the object or a negative value if it cannot be
	 *         found
	 */
	public int find(final T toBeFound)
	{
		int result = -1;
		if (this.first.equals(toBeFound))
		{
			result = 0;
		}
		else if (this.second.equals(toBeFound))
		{
			result = 1;
		}
		return result;
	}

	/**
	 * Returns the object at the given 0-based index.
	 *
	 * Note that only 0 and 1 are valid values.
	 * For other values an exception will be thrown.
	 *
	 * @param index
	 *            the index of the object
	 * @return the object
	 * @throws IndexOutOfBoundsException
	 *             if the index is not 0 or 1
	 */
	public T get(final int index) throws IndexOutOfBoundsException
	{
		T result = null;
		switch (index)
		{
			case 0:
				result = this.first;
				break;
			case 1:
				result = this.second;
				break;
			default:
				throw new IndexOutOfBoundsException("Indices for a pair may only be 0 or 1!");
		}
		return result;
	}

	/**
	 * Returns a new pair where the first element of it is the second
	 * element
	 * of this pair and vice versa.
	 *
	 * @return the reversed pair of this pair
	 */
	public Pair<T> getReversedPair()
	{
		return new Pair<T>(this.getSecond(), this.getFirst());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		final StringBuilder message = new StringBuilder();
		message.append("(");
		message.append(first.toString());
		message.append(",");
		message.append(second.toString());
		message.append(")");
		return message.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 47;
		result = prime * result + first.hashCode();
		result = prime * result + second.hashCode();
		return result;
	}

	/**
	 * Two pairs are equal if their first and second
	 * elements match, accordingly.
	 *
	 * @param obj
	 *            the object to compare to
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (null == obj || getClass() != obj.getClass())
			return false;

		@SuppressWarnings("unchecked")
		final Pair<T> other = (Pair<T>) obj;

		if (!first.equals(other.first))
			return false;

		if (!second.equals(other.second))
			return false;

		return true;
	}

}
