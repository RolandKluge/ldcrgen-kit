package edu.kit.iti.ldcrgen;

/**
 * Defines how fine-grained the generator shall give feedback.
 *
 * @author Roland Kluge
 *
 */
public enum VerbosityLevel
{
	NO_LOGGING(0),
	LEVEL_1(1),
	LEVEL_2(2),
	LEVEL_3(3);

	final int value;

	private VerbosityLevel(final int value)
	{
		this.value = value;
	}

	/**
	 * Returns the value of this level.
	 *
	 * The higher the value, the more fine-grained is the annotated
	 * information.
	 *
	 * @return the level value of this level
	 */
	public int getLevel()
	{
		return this.value;
	}
}
