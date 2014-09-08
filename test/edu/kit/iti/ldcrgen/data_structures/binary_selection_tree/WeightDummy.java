package edu.kit.iti.ldcrgen.data_structures.binary_selection_tree;

import edu.kit.iti.ldcrgen.data_structures.binary_selection_tree.Weightable;


public class WeightDummy implements Weightable
{

	private double weight;

	public WeightDummy(final double weight)
	{
		this.weight = weight;
	}

	@Override
	public double getWeight()
	{
		return this.weight;
	}

	@Override
	public int hashCode()
	{
		if (this.weight == 0.0) // -0.0 and 0.0 have different masks
		{
			return 0;
		}
		else
		{
			final long tmp = Double.doubleToLongBits(this.weight);
			return (int) (tmp ^ (tmp >>> 32));
		}
	}

	@Override
	public boolean equals(final Object obj)
	{
		return obj instanceof WeightDummy && ((WeightDummy) obj).weight == this.weight;
	}

	@Override
	public String toString()
	{
		return "WeightDummy [weight=" + weight + "]";
	}

	@Override
	public Object getObject()
	{
		return null;
	}



}
