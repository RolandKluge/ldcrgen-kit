package edu.kit.iti.ldcrgen.data_structures;

import edu.kit.iti.ldcrgen.util.Pair;

public interface RandomProvider
{
	public void setSeed(final long seed);
	public int nextInt();
	public int nextInt(final int maxValue);
	public Pair<Integer> nextUnequalInts(final int maxValue);
	public long nextLong();
	public double nextDouble();
	public double nextGaussian();
}
