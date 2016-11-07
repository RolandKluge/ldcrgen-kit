package edu.kit.iti.ldcrgen.converter;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import edu.kit.iti.ldcrgen.io.GraphJReader;
import edu.kit.iti.ldcrgen.io.journaling.ClusteringJournal;
import edu.kit.iti.ldcrgen.io.journaling.ClusteringOperation;
import edu.kit.iti.ldcrgen.io.journaling.GraphJournal;
import edu.kit.iti.ldcrgen.io.journaling.GraphOperation;


/**
 * Converts the binary GraphJ format into an informal textual
 * representation.
 *
 * @author Roland Kluge
 *
 */
public class BinaryToTextConverter
{

	/**
	 * Converts the given binary file (first argument) into a textual
	 * representation.
	 *
	 * @param args
	 *            must be of length 1 and contains the filename of the
	 *            binary file
	 */
	public static void main(final String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("First argument: GraphJ file");
			System.exit(1);
		}

		BufferedInputStream fstream = null;
		try
		{
			fstream = new BufferedInputStream(new FileInputStream(args[0]));
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}

		final GraphJournal gJournal = GraphJReader.readGraphJournal(fstream);
		final ClusteringJournal clJournal = GraphJReader.readClusteringJournal(fstream);

		if (null != fstream)
		{
			try
			{
				fstream.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}

		/*
		 * Print journals to console
		 */
		System.out.println("Graph Journal:");
		int graphOpCounter = 1;
		for (final GraphOperation op : gJournal)
		{
			System.out.println("#" + graphOpCounter + ": " + op.type + " " + op.arg0 + " " + op.arg1
				+ " " + op.arg2 + "\t\t" + op.type.documentation);
			graphOpCounter++;
		}

		System.out.println("Clustering Journal:");
		int clusterOpCounter = 1;
		for (final ClusteringOperation op : clJournal)
		{
			System.out.println("#" + clusterOpCounter + ": " + op.type + " " + op.arg0 + " "
				+ op.arg1 + " " + op.arg2 + "\t\t" + op.type.documentation);
			clusterOpCounter++;
		}
	}

	/**
	 * Converts the given binary file (first argument) into a text file.
	 *
	 * @param graphJPath
	 *            the binary input file
	 * @param nodeNamesPath
	 *            A text file which contains names of the nodes
	 * @param networkPath
	 *            A text file which contains the network
	 * @param deltaStreamPath
	 *            A text file which contains the stream of atomic changes
	 */
	public static void convertGraphJToText(final String graphJPath, final String nodeNamesPath,
		final String networkPath, final String deltaStreamPath)
	{


		BufferedInputStream fstream = null;
		try
		{
			fstream = new BufferedInputStream(new FileInputStream(graphJPath));
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}

		final GraphJournal gJournal = GraphJReader.readGraphJournal(fstream);
		// final ClusteringJournal clJournal =
		// GraphJReader.readClusteringJournal(fstream);

		IOUtils.closeQuietly(fstream);


		int nodeId = 1;
		boolean useSimpleFormat = true;

		BufferedWriter nodeNameWriter = null;
		BufferedWriter networkWriter = null;
		BufferedWriter deltaStreamWriter = null;

		try
		{
			nodeNameWriter = new BufferedWriter(
				new FileWriter(new File(nodeNamesPath).getAbsoluteFile()));
			networkWriter = new BufferedWriter(
				new FileWriter(new File(networkPath).getAbsoluteFile()));
			deltaStreamWriter = new BufferedWriter(
				new FileWriter(new File(deltaStreamPath).getAbsoluteFile()));


			for (final GraphOperation op : gJournal)
			{

				switch (op.type)
				{
					case CreateNode:
						if (useSimpleFormat)
						{
							nodeNameWriter.write(nodeId + " " + op.arg0 + "\n");
							nodeId++;
						}
						else
						{
							deltaStreamWriter
								.write("ADD NODE" + "\n" + nodeId + " " + op.arg0 + "\n");
							nodeId++;
						}

						break;


					case CreateEdge:
						if (useSimpleFormat)
						{
							networkWriter.write(op.arg0 + " " + op.arg1 + "\n");
						}
						else
						{
							deltaStreamWriter
								.write("ADD EDGE" + "\n" + op.arg0 + " " + op.arg1 + "\n");
						}
						break;

					case NextStep:
						useSimpleFormat = false;
						deltaStreamWriter.write("NEXT STEP" + "\n");
						break;



					case RemoveEdge:
						deltaStreamWriter
							.write("REMOVE EDGE" + "\n" + op.arg0 + " " + op.arg1 + "\n");
						break;

					case RemoveNode:
						deltaStreamWriter
							.write("REMOVE NODE" + "\n" + op.arg0 + " " + op.arg1 + "\n");
						break;
					default:
						break;

				}

			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(nodeNameWriter);
			IOUtils.closeQuietly(networkWriter);
			IOUtils.closeQuietly(nodeNameWriter);
		}
	}
}
