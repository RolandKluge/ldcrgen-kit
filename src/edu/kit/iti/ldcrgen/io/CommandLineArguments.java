package edu.kit.iti.ldcrgen.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.kit.iti.ldcrgen.Main;
import edu.kit.iti.ldcrgen.data_structures.PInSampler;
import edu.kit.iti.ldcrgen.util.ListUtils;
import edu.kit.iti.ldcrgen.util.MathUtils;

public class CommandLineArguments
{
	private static final CommandLineArguments DEFAULT_ARGUMENTS = new CommandLineArguments();

	public ArrayList<Integer> cl_sizes;
	public ArrayList<Double> p_in_list;
	public double p_out;
	public PInSampler p_in_new;
	public int n;
	public int t_max;
	public double p_chi;
	public double p_nu;
	public double p_mu;
	public double p_omega;

	public double theta;
	public int eta;

	public int beta;

	public File directory;
	public File output;

	public boolean verbose;

	public boolean binary;

	public int numRuns;

	public String timingsOutputFile;

	public boolean useTreeMapInsteadOfHashMap;

	private CommandLineArguments()
	{
		final int k = 2;
		this.n = 60;

		this.cl_sizes = new ArrayList<Integer>(Collections.nCopies(k, this.n / k));
		final int remainingNodes = n - (this.n / k) * k;
		this.cl_sizes.set(k - 1, this.cl_sizes.get(k - 1) + remainingNodes);

		this.p_in_list = new ArrayList<Double>(Collections.nCopies(k, 0.10));
		this.p_out = 0.01;
		this.theta = 0.25;
		this.directory = new File("./");
		this.output = new File("./", "ldcr_graph_<date>_<time>_<run>.graphj");
		this.binary = true;

		this.p_in_new = PInSampler.MEAN;
		this.p_nu = 0.50;
		this.p_chi = 0.50;
		this.p_mu = 0.50;
		this.p_omega = 0.02;
		this.t_max = 100;
		this.eta = 1;
		this.beta = 1;
		this.numRuns = 1;
	}

	private static Map<String, String> extractArguments(final String[] args)
	{
		final Map<String, String> result = new HashMap<String, String>();

		for (int i = 0; i < args.length; ++i)
		{
			final String str = args[i];
			final int indexOfEquals = str.indexOf('=');

			if (-1 != indexOfEquals && indexOfEquals < str.length() - 1)
			{
				result.put(str.substring(0, indexOfEquals), str.substring(indexOfEquals + 1));
			}
			else if ("-v".equals(str))
			{
				result.put("-v", null);
			}
		}

		return result;
	}

	public static List<CommandLineArguments> parseFile(final String filename)
		throws ParseException, FileNotFoundException
	{
		final File file = new File(filename);
		final Scanner scanner = new Scanner(file);
		final List<CommandLineArguments> result = new ArrayList<CommandLineArguments>();

		// pattern which matches strings not containing quotes/white spaces
		// or
		// strings enclosed in white spaces
		final Pattern regex = Pattern.compile("[^\\s\"]+|\"([^\"]+)\"");

		int currentLine = 0;
		while (scanner.hasNextLine())
		{
			// this modification helps identifying if the output files have
			// to be named differently
			final String modifiedLine = scanner.nextLine() + " positionInFile=" + currentLine;
			final List<String> matchList = new ArrayList<String>();
			final Matcher regexMatcher = regex.matcher(modifiedLine);
			while (regexMatcher.find())
			{
				if (regexMatcher.group(1) != null)
				{
					// Add double-quoted string without the quotes
					matchList.add(regexMatcher.group(1));
				}
				else
				{
					// Add unquoted word
					matchList.add(regexMatcher.group());
				}
			}

			result.add(CommandLineArguments.parse(matchList.toArray(new String[matchList.size()])));

			++currentLine;
		}

		scanner.close();
		return result;
	}

	public static CommandLineArguments parse(final String[] arguments) throws ParseException
	{
		final Map<String, String> args = extractArguments(arguments);
		final CommandLineArguments result = new CommandLineArguments();

		result.verbose = args.containsKey("-v");

		// number of nodes, cluster size and count
		parseClusterSizeAndCount(args, result);

		// intra-cluster edge probability

		parseIntraClusterProbabilities(args, result);

		// parse inter-cluster edge probability

		parseInterClusterProbabilities(args, result);

		// parse process parameters
		parseProcessParameters(args, result);

		// parse files and directories
		parseFiles(args, result);

		// parse special options
		parseSpecial(args, result);

		return result;
	}

	private static void parseClusterSizeAndCount(final Map<String, String> args,
		final CommandLineArguments result) throws ParseException
	{

		if (args.containsKey("n"))
		{
			try
			{
				result.n = Integer.parseInt(args.get("n"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException(//
					"Error parsing node count n: '" + nfex.getMessage() + "'.");
			}
			verifyPositive(result.n, "n");
		}

		if (args.containsKey("t_max"))
		{
			try
			{
				result.t_max = Integer.parseInt(args.get("t_max"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException(//
					"Error parsing step count t_max: '" + nfex.getMessage() + "'.");
			}
			verifyPositive(result.t_max, "t_max");
		}

		/*
		 * First possibility of defining cluster sizes
		 */
		int k = result.cl_sizes.size();
		List<Double> tmpArray = new ArrayList<Double>();
		if (args.containsKey("k"))
		{
			try
			{
				k = Integer.parseInt(args.get("k"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException(//
					"Error parsing cluster count: '" + nfex.getMessage() + "'.");
			}

			verifyPositive(k, "cluster count");
			verify(k <= result.n, "Too few clusters! (k: " + k + ", n:" + result.n + ")");
		}

		if (args.containsKey("beta"))
		{
			try
			{
				result.beta = Integer.parseInt(args.get("beta"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException(//
					"Error parsing skew factor beta: '" + nfex.getMessage() + "'.");
			}

			verifyPositive(result.beta, "skewness factor beta");
		}

		final double invBeta = 1.0 / result.beta;

		tmpArray = new ArrayList<Double>();

		for (int i = 1; i <= k; ++i)
		{
			double first = Math.pow(i / (double) k, invBeta);
			double second = Math.pow((i - 1) / (double) k, invBeta);
			tmpArray.add(MathUtils.roundToDecimalPlaces(first - second, 4));
		}

		/*
		 * Second possibility of defining cluster sizes. Overrides values
		 * from first option.
		 */
		if (args.containsKey("cl_sizes"))
		{
			tmpArray = CommandLineArguments.parseDoubleList(args.get("cl_sizes"));
			k = tmpArray.size();

			for (final double size : tmpArray)
			{
				verifyPositive(size, "cluster size");
			}
		}

		// n has not been given, so calculate the amount of nodes from the
		// cl_sizes list
		if (!args.containsKey("n") && args.containsKey("cl_sizes"))
		{
			result.n = (int) ListUtils.sumUp(ListUtils.mapType(tmpArray, Double.class));
			verify(result.n >= k, "Too few clusters: n=" + result.n + " - k=" + k);

		}

		/*
		 * Normalize cluster sizes to integer node counts
		 */
		result.cl_sizes.clear();
		k = tmpArray.size();
		int numDistributedNodes = 0;
		final double totalSize = ListUtils.sumUp(tmpArray);
		for (int i = 0; i < k; ++i)
		{
			final int clusterSize = (int) Math.floor(tmpArray.get(i) / totalSize
				* result.n);
			numDistributedNodes += clusterSize;
			result.cl_sizes.add(clusterSize);
		}
		/*
		 * As n/k may not be an integer, we have to distribute the
		 * remaining nodes.
		 */
		final int remainingNodes = result.n - numDistributedNodes;
		for (int i = 0; i < remainingNodes; ++i)
		{
			result.cl_sizes.set(i, 1 + result.cl_sizes.get(i));
		}
	}

	private static void parseIntraClusterProbabilities(final Map<String, String> args,
		final CommandLineArguments result) throws ParseException
	{
		int k = result.cl_sizes.size();
		result.p_in_list = new ArrayList<Double>(Collections.nCopies(k, result.p_in_list.get(0)));
		if (args.containsKey("p_in"))
		{
			double p_in;
			try
			{
				p_in = new Double(args.get("p_in"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException("Error parsing p_in: Not a double value! "
					+ args.get("p_in"));
			}
			verifyProbability(p_in, "p_in");

			result.p_in_list = new ArrayList<Double>(Collections.nCopies(k, p_in));
		}
		else if (args.containsKey("p_in_list"))
		{
			result.p_in_list = CommandLineArguments.parseDoubleList(args.get("p_in_list"));

			if (result.p_in_list.size() != result.cl_sizes.size())
				throw new ParseException("List of sizes and pins are of unequal length: "
					+ result.cl_sizes + " <-> " + result.p_in_list);
			for (final double val : result.p_in_list)
			{
				verifyPositive(val, "p_in_list");
			}
		}
		else if (args.containsKey("deg_in"))
		{
			int deg_in;
			try
			{
				deg_in = new Integer(args.get("deg_in"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException("Error parsing deg_in: '" + nfex.getMessage() + "'.");
			}

			for (int i = 0; i < k; ++i)
			{
				verifyPositive(deg_in, "deg_in");
				final double pInValue = Math.min(1.0, deg_in / (result.cl_sizes.get(i) - 1.0));
				result.p_in_list.set(i, pInValue);
			}
		}
		else if (args.containsKey("deg_in_list"))
		{
			final List<Integer> deg_in_list = CommandLineArguments.parseIntegerList(args
				.get("deg_in_list"));

			for (int i = 0; i < k; ++i)
			{
				// verifyPositive(deg_in_list.get(i), "deg_in_list");
				final double pInValue = Math.min(1.0, deg_in_list.get(i)
					/ (result.cl_sizes.get(i) - 1.0));
				result.p_in_list.set(i, pInValue);
			}
		}
	}

	private static void parseInterClusterProbabilities(final Map<String, String> args,
		final CommandLineArguments result) throws ParseException
	{
		int k = result.cl_sizes.size();
		if (args.containsKey("p_out"))
		{
			double p_out;
			try
			{
				p_out = new Double(args.get("p_out"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException("Error parsing p_out: '" + nfex.getMessage() + "'.");
			}
			verifyProbability(p_out, "p_out");
			result.p_out = p_out;
		}
		else if (args.containsKey("deg_out"))
		{
			int deg_out;
			try
			{
				deg_out = new Integer(args.get("deg_out"));
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException("Error parsing deg_out: '" + nfex.getMessage() + "'.");
			}

			double sum = 0;
			for (int i = 0; i < k; ++i)
			{
				sum += result.cl_sizes.get(i) * (result.n - result.cl_sizes.get(i));
			}
			sum /= result.n; // average maximum deg_out

			result.p_out = deg_out / sum;
		}
	}

	private static void parseProcessParameters(final Map<String, String> args,
		final CommandLineArguments result) throws ParseException
	{
		try
		{
			if (args.containsKey("p_nu"))
			{
				result.p_nu = new Double(args.get("p_nu"));
				verifyProbability(result.p_nu, "p_nu");
			}
			if (args.containsKey("p_mu"))
			{
				result.p_mu = new Double(args.get("p_mu"));
				verifyProbability(result.p_mu, "p_mu");
			}
			if (args.containsKey("p_omega"))
			{
				result.p_omega = new Double(args.get("p_omega"));
				verifyProbability(result.p_omega, "p_omega");
			}
			if (args.containsKey("p_chi"))
			{
				result.p_chi = new Double(args.get("p_chi"));
				verifyProbability(result.p_chi, "p_chi");
			}
			if (args.containsKey("theta"))
			{
				result.theta = new Double(args.get("theta"));
				verifyProbability(result.theta, "theta");
			}
			if (args.containsKey("eta"))
			{
				result.eta = new Integer(args.get("eta"));
				verifyPositive(result.eta, "eta");
			}
		}
		catch (final NumberFormatException formEx)
		{
			throw new ParseException("Error parsing event probabilities: '" + formEx.getMessage()
				+ "'.");
		}

		// parse p in sampler
		if (args.containsKey("p_in_new"))
		{
			result.p_in_new = null;
			final String samplerCandidate = args.get("p_in_new");
			for (final PInSampler sampler : PInSampler.values())
			{
				if (sampler.toString().equals(samplerCandidate))
				{
					result.p_in_new = sampler;
				}
			}

			if (null == result.p_in_new)
			{
				throw new ParseException("Error parsing p_in sampler: " + samplerCandidate);
			}
		}
	}

	private static void parseFiles(final Map<String, String> args, final CommandLineArguments result)
		throws ParseException
	{
		if (args.containsKey("binary"))
		{
			result.binary = Boolean.parseBoolean(args.get("binary"));
		}

		if (args.containsKey("dir"))
		{
			final File directory = new File(args.get("dir"));
			if (directory.exists() && directory.isDirectory())
			{
				result.directory = directory;
			}
		}

		final String tmpFilename = "ldcr_graph_"
			+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		result.output = new File(result.directory, tmpFilename);
		if (args.containsKey("output"))
		{
			final File outFile = new File(result.directory, args.get("output"));

			if (outFile.exists())
			{
				throw new ParseException("Output file already exists: " + outFile.getAbsolutePath());
			}

			result.output = outFile;
		}

		/*
		 * If a parameter file is processed, then the output files have to
		 * be numbered.
		 */
		if (args.containsKey("positionInFile"))
		{
			final int pos = Integer.parseInt(args.get("positionInFile"));
			result.output = new File(result.output.getAbsolutePath() + "_"
				+ String.format("%04d", (pos + 1)) + ".graphj");
		}
		else
		{
			result.output = new File(result.output.getAbsolutePath() + ".graphj");
		}
	}

	private static void parseSpecial(final Map<String, String> args,
		final CommandLineArguments result) throws ParseException
	{
		if (args.containsKey("map_type"))
		{
			String map_type = args.get("map_type");
			if ("hashmap".equals(map_type))
			{
				result.useTreeMapInsteadOfHashMap = false;
			}
			else if ("treemap".equals(map_type))
			{
				result.useTreeMapInsteadOfHashMap = true;
			}
			else
			{
				Main.logAndPrintErr("Unknown map type: " + map_type);
				result.useTreeMapInsteadOfHashMap = false;
			}
		}
		else
		{
			result.useTreeMapInsteadOfHashMap = false;
		}

		if (args.containsKey("r"))
		{
			try
			{
				String timingParameters = args.get("r");
				String[] splitParameters = timingParameters.split(":");

				result.numRuns = Integer.parseInt(splitParameters[0]);

				if (splitParameters.length > 1)
				{

					result.timingsOutputFile = splitParameters[1];

				}

				if (splitParameters.length > 2)
				{
					if ("treemap".equals(splitParameters[2]))
					{
						result.useTreeMapInsteadOfHashMap = true;
					}
					else
					{
						result.useTreeMapInsteadOfHashMap = false;
					}
				}
			}
			catch (final NumberFormatException nfex)
			{
				throw new ParseException(//
					"Error parsing repetition count r: '" + nfex.getMessage() + "'.");
			}
			catch (final IndexOutOfBoundsException iobex)
			{
				throw new ParseException(//
					"Error parsing repetition count r: '" + iobex.getMessage() + "'.");
			}
			verifyPositive(result.numRuns, "r");
		}
	}

	private static ArrayList<Double> parseDoubleList(final String str) throws ParseException
	{
		final String list = str.substring(1, str.length() - 1); // skip '['
																// and ']'
		final String[] candidates = list.split(",");
		final ArrayList<Double> result = new ArrayList<Double>(candidates.length);

		try
		{
			for (final String candidate : candidates)
			{
				result.add(new Double(candidate));
			}
		}
		catch (final NumberFormatException formEx)
		{
			throw new ParseException("Error parsing double list '" + str + "' (Reason: "
				+ formEx.getMessage() + ")");
		}

		return result;
	}

	private static List<Integer> parseIntegerList(final String str) throws ParseException
	{
		final String list = str.substring(1, str.length() - 1); // skip '['
																// and ']'
		final String[] candidates = list.split(",");
		final List<Integer> result = new ArrayList<Integer>(candidates.length);

		try
		{
			for (final String candidate : candidates)
			{
				result.add(new Integer(candidate));
			}
		}
		catch (final NumberFormatException formEx)
		{
			throw new ParseException("Error parsing integer list '" + str + "' (Reason: "
				+ formEx.getMessage() + ")");
		}

		return result;
	}

	private static void verifyProbability(final double val, final String description)
		throws ParseException
	{
		if (val < 0.0 || val > 1.0)
		{
			throw new ParseException("Error parsing " + description
				+ ": Not a probability! (value: " + val + ")");
		}
	}

	private static void verifyPositive(final double val, final String description)
		throws ParseException
	{
		if (val <= 0.0)
		{
			throw new ParseException("Error parsing " + description
				+ ": Needs to be positive!");
		}
	}

	private static void verify(final boolean assertValue, final String message)
		throws ParseException
	{
		if (!assertValue)
		{
			throw new ParseException("Error during parsing: Reason " + message);
		}
	}

	public void printHelpMessage()
	{
		Scanner scanner = new Scanner(
			getClass().getClassLoader().getResourceAsStream("help.txt"));
		while (scanner.hasNextLine())
			System.out.println(scanner.nextLine());
		scanner.close();
	}

	public static CommandLineArguments getDefaults()
	{
		return DEFAULT_ARGUMENTS;
	}

	@Override
	public String toString()
	{
		return "\tn=" + n + "\n" + //
			"\tcl_sizes=" + cl_sizes + "\n" + //
			"\n" + //
			"\tp_in_list=" + p_in_list + "\n" + //
			"\tp_out=" + p_out + "\n" + //
			"\tp_in_new=" + p_in_new + "\n" + //
			"\tt_max=" + t_max + "\n" + //
			"\tp_chi=" + p_chi + "\n" + //
			"\tp_nu=" + p_nu + ",\n" + //
			"\tp_mu=" + p_mu + "\n" + //
			"\tp_omega=" + p_omega + "\n" + //
			"\ttheta=" + theta + "\n" + //
			"\n" + //
			"\teta=" + eta + "\n" + //
			"\tbeta=" + beta + "\n" + //
			"\n" + //
			"\tdirectory=" + directory + "\n" + //
			"\toutput=" + output + "\n" + //
			"\tbinary=" + binary + "\n" + //
			"\tuseTreemapInsteadOfHashmap=" + useTreeMapInsteadOfHashMap;
	}

}
