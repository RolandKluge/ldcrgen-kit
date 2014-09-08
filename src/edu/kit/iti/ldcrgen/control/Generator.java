package edu.kit.iti.ldcrgen.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.kit.iti.ldcrgen.Main;
import edu.kit.iti.ldcrgen.VerbosityLevel;
import edu.kit.iti.ldcrgen.data_structures.DCRGraph;
import edu.kit.iti.ldcrgen.data_structures.JavaUtilRandomProvider;
import edu.kit.iti.ldcrgen.data_structures.RandomProvider;
import edu.kit.iti.ldcrgen.io.CommandLineArguments;

/**
 * The generator is stears the whole generation process.
 * It contains the logic of the process and builds the graph.
 * The parameters of the process can be set by means of the command line
 * arguments passed to the generator.
 *
 * @author Roland Kluge
 */
public class Generator
{
	// give a progress information every STEPPING permilles
	private static final int STEPPING = 50;
	private int currentProgress = 0;

	private final RandomProvider random;
	private final CommandLineArguments args;
	private DCRGraph graph;

	/**
	 * Configures a new generator with the given command line arguments.
	 *
	 * @param args
	 *            the arguments
	 */
	public Generator(final CommandLineArguments args)
	{
		this.args = args;
		this.graph = null;
		this.random = new JavaUtilRandomProvider();
	}

	/**
	 * Runs the generator.
	 *
	 * Afterwards, the graph is ready for further processing.
	 */
	public void run()
	{

		long timeBeforeStart;
		long timeAfterInit;
		long timeAfterFinish;

		Main.logAndPrintInfo("Generator: initial Instance with " + //
			"cl_sizes: " + args.cl_sizes//
			+ " p_in=" + args.p_in_list //
			+ " p_out" + args.p_out //
			+ " p_in_new: " + args.p_in_new //
			+ " theta: " + args.theta, VerbosityLevel.LEVEL_2);

		timeBeforeStart = System.currentTimeMillis();

		graph = new DCRGraph(args.p_out, args.theta, args.p_in_new, args.useTreeMapInsteadOfHashMap);
		graph.initAsErdosRenyi(args.cl_sizes, args.p_in_list, args.p_out);

		timeAfterInit = System.currentTimeMillis();

		/*
		 * Iteration over time
		 */
		for (int time = 1; time <= args.t_max; ++time)
		{
			this.logAndPrintProgres(time);
			graph.nextTimeStep();

			/*
			 * Large-scale operations
			 */
			if (nextDoubleInUnitRange() < args.p_omega)
			{
				if (nextDoubleInUnitRange() < args.p_mu)
				{
					graph.split();
				}
				else
				{
					graph.merge();
				}

			}

			/*
			 * Small-scale operations
			 */
			int opsDuringStep = 0;
			while (opsDuringStep < args.eta)
			{
				// edge operation
				if (nextDoubleInUnitRange() < args.p_chi)
				{
					++opsDuringStep;

					if (graph.shallDoEdgeInsertion()) // weighted selection
					{
						graph.addEdge();
					}
					else
					{
						graph.removeEdge();
					}
				}
				else
				// node operation
				{
					if (nextDoubleInUnitRange() < args.p_nu)
					// node insertion
					{
						opsDuringStep += graph.addAndConnectNode();
					}
					else
					// node deletion
					{
						opsDuringStep += graph.removeNode();
					}
				}

			}
			graph.checkClusterOperationsForCompleteness();

		} // for: time steps

		final StringBuilder builder = new StringBuilder();
		builder.append("Generation complete!");
		builder.append("\n\tNode/Edge operations:\t" + graph.getSmallScaleOpCount());
		builder.append("\n\tCluster operations:\t" + graph.getLargeScaleOpCount());
		builder.append("\n\tEdge count:\t" + graph.getEdgeCount());
		builder.append("\n\tNode count:\t" + graph.getNodeCount());
		builder.append("\n\tCluster count:\t" + graph.getClusterCount());

		Main.logAndPrintInfo(builder.toString(), VerbosityLevel.LEVEL_2);

		timeAfterFinish = System.currentTimeMillis();

		// TODO rkluge refactor this!
		if (args.numRuns > 1)
		{
			FileOutputStream stream = null;
			try
			{
				double timeForInit = (timeAfterInit - timeBeforeStart) / 1000.0;
				double timeForIterations = (timeAfterFinish - timeAfterInit) / 1000.0;
				double totalTime = timeForInit + timeForIterations;
				System.out.println(
					"Timit for init: " + timeForInit
						+ "sec - Time for iterations: " + timeForIterations + " sec");
				stream = new FileOutputStream(new File(this.args.timingsOutputFile), true);

				String line = args.n + " " + args.p_in_list.get(0) + " " + timeForInit + " "
					+ timeForIterations + " " + totalTime + "\n";

				stream.write(line.getBytes());
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		System.gc();
	}

	private void logAndPrintProgres(final int time)
	{
		final int permilles = 1000 * time / this.args.t_max;
		if (permilles >= currentProgress + STEPPING)
		{
			Main.logAndPrintInfo(permilles / 10.0 + "%", VerbosityLevel.LEVEL_2);
			currentProgress += STEPPING;
		}
	}

	/**
	 * Returns the graph which has been generated.
	 *
	 * The graph will be <code>null</code> if the generator has
	 * not run yet.
	 *
	 * @return the graph
	 */
	public DCRGraph getGraph()
	{
		return this.graph;
	}

	/*
	 * Returns a double in the range of 0 to 1
	 */
	private double nextDoubleInUnitRange()
	{
		return random.nextDouble();
	}
}
