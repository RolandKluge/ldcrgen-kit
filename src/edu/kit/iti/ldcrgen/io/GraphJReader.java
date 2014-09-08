package edu.kit.iti.ldcrgen.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.kit.iti.ldcrgen.io.journaling.ClusteringJournal;
import edu.kit.iti.ldcrgen.io.journaling.GraphJournal;


/**
 * Reads journals from files.
 *
 * @author Roland Kluge
 *
 */
public class GraphJReader
{

	public static GraphJournal readGraphJournal(final InputStream fstream)
	{
		final GraphJournal result = new GraphJournal();
		parseInto(fstream, result.opCodes, result.arguments);
		return result;
	}

	public static ClusteringJournal readClusteringJournal(final InputStream fstream)
	{
		final ClusteringJournal result = new ClusteringJournal();
		parseInto(fstream, result.opCodes, result.intArgs);
		return result;
	}

	/*
	 * Parses the journal into the two given arrays.
	 */
	private static void parseInto(final InputStream fstream, final ArrayList<Byte> opcodes,
		final ArrayList<Integer> args)
	{
		final DataInputStream dStream = new DataInputStream(fstream);
		try
		{
			final int opLength = dStream.readInt();
			final int argsLength = dStream.readInt();

			opcodes.ensureCapacity(opLength);
			args.ensureCapacity(argsLength);

			for (int i = 0; i < opLength; ++i)
			{
				opcodes.add(dStream.readByte());
			}

			for (int i = 0; i < argsLength; ++i)
			{
				args.add(dStream.readInt());
			}

		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
	}


}
