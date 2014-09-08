package edu.kit.iti.ldcrgen.data_structures;

/**
 * Marker for declaring how to derive the new intra-cluster edge
 * probabilities from the old ones during a cluster operation.
 *
 * @author Roland Kluge
 */
public enum PInSampler
{
	/**
	 * Indicates usage of normal distribution for sampling the p_in value
	 * of the operation' products.
	 */
	GAUSSIAN,
	/**
	 * Indicates usage of the arithmetic mean for sampling the p_in value
	 * of the operation' products.
	 */
	MEAN;
}
