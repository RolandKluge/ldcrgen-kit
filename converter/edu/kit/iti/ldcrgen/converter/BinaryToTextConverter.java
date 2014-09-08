package edu.kit.iti.ldcrgen.converter;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
		for (final GraphOperation op : gJournal)
		{
			System.out.println(op.type + " " + op.arg0 + " " + op.arg1 + " " + op.arg2);
		}

		System.out.println("Clustering Journal:");
		for (final ClusteringOperation op : clJournal)
		{
			System.out.println(op.type + " " + op.arg0 + " " + op.arg1 + " " + op.arg2);
		}
	}
}
