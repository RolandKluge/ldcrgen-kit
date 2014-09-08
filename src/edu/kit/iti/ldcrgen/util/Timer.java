package edu.kit.iti.ldcrgen.util;

/**
 * Measures time intervals.
 *
 * The timer is started by means of the {@link #start()} method and
 * stopped with the {@link #stop()} method.
 * The measured time can be retrieved via {@link #elapsed()}.
 *
 * As long as the {@link #reset()} method is not called, the time intervals
 * will be added up.
 *
 * @author Roland Kluge
 *
 */
public class Timer
{
	long timeStarted = 0;
	long timeElapsed = 0;

	public void start()
	{
		timeStarted = System.currentTimeMillis();
	}

	public void stop()
	{
		timeElapsed += System.currentTimeMillis() - timeStarted;
	}

	public long elapsed()
	{
		return this.timeElapsed;
	}

	public void reset()
	{
		this.timeElapsed = 0;
	}


}
