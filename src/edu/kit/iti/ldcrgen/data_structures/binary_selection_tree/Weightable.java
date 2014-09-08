package edu.kit.iti.ldcrgen.data_structures.binary_selection_tree;

/**
 * This is a Data Transfer Object representing an object
 * with a certain 'weight'.
 *
 * @author Roland Kluge
 */
public interface Weightable
{
	/**
	 * Returns the weight of the object
	 *
	 * @return the weight
	 */
	double getWeight();

	/**
	 * Returns the appropriate object.
	 *
	 * @return the object
	 */
	Object getObject();
}
