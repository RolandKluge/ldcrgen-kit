package edu.kit.iti.ldcrgen;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.kit.iti.ldcrgen.control.Generator;
import edu.kit.iti.ldcrgen.data_structures.DCRGraph;
import edu.kit.iti.ldcrgen.io.CommandLineArguments;
import edu.kit.iti.ldcrgen.io.GraphJWriter;
import edu.kit.iti.ldcrgen.io.ParseException;
import edu.kit.iti.ldcrgen.io.journaling.ClusteringJournal;
import edu.kit.iti.ldcrgen.io.journaling.GraphJournal;

/**
 * Main class.
 *
 * Responsible of starting the generator.
 *
 * @author Roland Kluge
 *
 */
public class Main
{
	private static final DateFormat DATE_TIME_INSTANCE = DateFormat.getDateTimeInstance(
		DateFormat.SHORT, DateFormat.MEDIUM, Locale.GERMANY);
	private static final DateFormat TIME_INSTANCE = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	private static PrintStream logStream = null;
	private static boolean loggingEnabled = false;
	private static VerbosityLevel logLevel = VerbosityLevel.NO_LOGGING;

	private static Date timeRoundedToSeconds = null;
	private static String preRenderedDateTime = null;
	private static String preRenderedTime = null;


	/**
	 * Starts the generator.
	 *
	 * Call java edu.kit.iti.ldcrgen.Main -h for help.
	 *
	 * @param args
	 *            the command line arguments
	 */
	public static void main(final String[] args)
	{
		final Map<String, String> topLevelArguments = parseTopLevelArguments(args);
		List<CommandLineArguments> parsedArguments = new ArrayList<CommandLineArguments>();
		Main.setLoggingEnabled(topLevelArguments.containsKey("-l"));

		if (Main.isLoggingEnabled())
		{
			try
			{
				final String filename = "ldcr_generator_logfile_"
					+ DATE_TIME_INSTANCE.format(
						new Date()).replaceAll("\\s+", "_") + ".log";
				logStream = new PrintStream(new FileOutputStream(filename));
			}
			catch (final FileNotFoundException e)
			{
				System.err.println("Cannot open logfile");
				e.printStackTrace();
			}
		}

		Main.setVerbosityLevel(VerbosityLevel.NO_LOGGING);
		if (topLevelArguments.containsKey("-v"))
			Main.setVerbosityLevel(VerbosityLevel.LEVEL_1);
		if (topLevelArguments.containsKey("-vv"))
			Main.setVerbosityLevel(VerbosityLevel.LEVEL_2);
		if (topLevelArguments.containsKey("-vvv"))
			Main.setVerbosityLevel(VerbosityLevel.LEVEL_3);

		Main.logAndPrintInfo("Parsing...", VerbosityLevel.LEVEL_1);
		if (topLevelArguments.containsKey("-h"))
		{
			CommandLineArguments.getDefaults().printHelpMessage();

			System.exit(0);
		}
		else if (topLevelArguments.containsKey("-defaults"))
		{
			final CommandLineArguments defaults = CommandLineArguments.getDefaults();

			Main.logAndPrintInfo("Default values:\n" + defaults, VerbosityLevel.NO_LOGGING);
			System.exit(0);
		}
		else if (topLevelArguments.containsKey("-f"))
		{
			final String filename = topLevelArguments.get("-f");
			try
			{
				parsedArguments = CommandLineArguments.parseFile(filename);
			}
			catch (final FileNotFoundException e)
			{
				Main.logAndPrintErr("FAILED. Reason: Unable to find parameter file: " + filename);
			}
			catch (final ParseException e)
			{
				Main.logAndPrintErr("FAILED. Reason: " + e.getMessage());
			}

		}
		else if (topLevelArguments.containsKey("-g"))
		{
			try
			{
				parsedArguments.add(CommandLineArguments.parse(args));
			}
			catch (final ParseException e)
			{
				Main.logAndPrintErr("FAILED. Reason: " + e.getMessage());
			}
		}
		else
		{
			Main.logAndPrintErr("No valid top-level argument given! Try calling with -h");
			System.exit(1);
		}

		Main.logAndPrintInfo("Parsing DONE!", VerbosityLevel.LEVEL_1);

		/*
		 * Parsing finished!
		 * For each set of arguments start the generator, retrieve and
		 * write the resulting time series to the corresponding output
		 * file.
		 */
		int counter = 0;
		for (final CommandLineArguments parsedArgs : parsedArguments)
		{
			for (int i = 0; i < parsedArgs.numRuns; ++i)
			{
				Main.logAndPrintInfo("~~~~~~~~~~~~~~~~~~~~~~~Argument Set No. " + (++counter) + "/"
					+ parsedArguments.size() + "~~~~~~~~~~~~~~~~~~~~~~~~", VerbosityLevel.LEVEL_1);
				Main.logAndPrintInfo("Started at: " + Main.getDateTime(), VerbosityLevel.LEVEL_1);
				Main.logAndPrintInfo("Starting generation process with parameters:",
					VerbosityLevel.LEVEL_1);
				Main.logAndPrintInfo(parsedArgs.toString(), VerbosityLevel.LEVEL_1);

				final Generator generator = new Generator(parsedArgs);
				try
				{

					if (1 < parsedArgs.numRuns)
					{
						System.out.println("TIMING MODE: run number " + i + " out of "
							+ parsedArgs.numRuns);
					}
					generator.run();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}

				final DCRGraph graph = generator.getGraph();
				final GraphJournal gJournal = graph.getGraphJournal();
				final ClusteringJournal clJournal = graph.getClusteringJournal();

				if (parsedArgs.binary)
				{
					BufferedOutputStream stream = null;
					try
					{
						Main.logAndPrintInfo(
							"Writing to file '" + parsedArgs.output.getAbsolutePath()
								+ "'...", VerbosityLevel.LEVEL_1);
						stream = new BufferedOutputStream(new FileOutputStream(parsedArgs.output));
						GraphJWriter.writeGraph(gJournal, stream);
						GraphJWriter.writeClustering(clJournal, stream);
						stream.close();
						Main.logAndPrintInfo("Writing DONE!", VerbosityLevel.LEVEL_1);

					}
					catch (final FileNotFoundException fnfex)
					{
						Main.logAndPrintErr("Writing FAILED!");
						fnfex.printStackTrace();
					}
					catch (final IOException ioex)
					{
						Main.logAndPrintErr("Writing FAILED!");
						ioex.printStackTrace();
					}
					finally
					{
						if (null != stream)
						{
							try
							{
								stream.close();
							}
							catch (final IOException ioex)
							{
								Main.logAndPrintErr("Closing stream failed.");
								ioex.printStackTrace();
							}
						}
					}
				}
				else
				{
					Main.logAndPrintInfo("Creating no output file!", VerbosityLevel.LEVEL_1);
				}

				Main.logAndPrintInfo("Finished at: " + Main.getDateTime(), VerbosityLevel.LEVEL_1);
			}
		}
	}

	public static boolean isLoggingEnabled()
	{
		return Main.loggingEnabled;
	}

	public static VerbosityLevel getVerbosityLevel()
	{
		return Main.logLevel;
	}

	private static void setLoggingEnabled(final boolean enabled)
	{
		Main.loggingEnabled = enabled;
	}

	private static void setVerbosityLevel(final VerbosityLevel logLevel)
	{
		Main.logLevel = logLevel;
	}

	private static Map<String, String> parseTopLevelArguments(final String[] args)
	{
		final Map<String, String> keyValueMap = new HashMap<String, String>();
		int i = 0;
		while (i < args.length)
		{
			final String str = args[i];
			if (str.indexOf('-') == 0) // top-level-parameter
			{
				if (i + 1 < args.length && args[i + 1].indexOf('-') != 0)
				{
					keyValueMap.put(str, args[i + 1]);
					++i;
				}
				else
				{
					keyValueMap.put(str, null);
				}
			}

			++i;
		}
		return keyValueMap;
	}

	private static String getDateTime()
	{
		updateTime();
		return preRenderedDateTime;
	}

	private static String getTime()
	{
		updateTime();
		return preRenderedTime;
	}

	/*
	 * This method reduces the number of 'new Date()' calls
	 */
	private static void updateTime()
	{
		if (null == timeRoundedToSeconds
			|| timeRoundedToSeconds.getTime() + 1000 < System.currentTimeMillis())
		{
			timeRoundedToSeconds = new Date();
			preRenderedDateTime = DATE_TIME_INSTANCE.format(timeRoundedToSeconds);
			preRenderedTime = TIME_INSTANCE.format(timeRoundedToSeconds);
		}
	}

	public static void logAndPrintInfo(final String message, final VerbosityLevel level)
	{
		Main.printInfo(message, level);
		Main.logInfo(message);
	}

	public static void logInfo(final String message)
	{
		if (loggingEnabled)
		{
			Main.logStream.print(Main.getTime());
			Main.logStream.print(" ");
			Main.logStream.println(message);
		}
	}

	public static void printInfo(final String string, final VerbosityLevel level)
	{
		if (level.getLevel() <= Main.getVerbosityLevel().getLevel())
		{
			System.out.print(Main.getTime());
			System.out.print(" ");
			System.out.println(string);
		}
	}

	public static void logAndPrintErr(final String message)
	{
		System.err.println(message);
		Main.logError(message);
	}

	public static void logError(final String message)
	{
		if (loggingEnabled)
		{
			Main.logStream.print("ERROR");
			Main.logStream.print(Main.getTime());
			Main.logStream.println(" " + message);
		}
	}

}
