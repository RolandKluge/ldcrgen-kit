package edu.kit.iti.ldcrgen.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.kit.iti.ldcrgen.io.journaling.ClusteringJournal;
import edu.kit.iti.ldcrgen.io.journaling.GraphJournal;


/***
 * Writes a GraphJournal to a binary file using DataOutputStream.
 *
 * * @author Christian Staudt
 *
 */
public class GraphJWriter
{

	public static void writeGraph(final GraphJournal journal, final OutputStream fStream)
	{
		final DataOutputStream dStream = new DataOutputStream(fStream);
		try
		{
			final int opLength = journal.opCodes.size();
			final int argLength = journal.arguments.size();

			dStream.writeInt(opLength);
			dStream.writeInt(argLength);

			for (final Byte b : journal.opCodes)
			{
				dStream.writeByte(b);
			}

			for (final Integer i : journal.arguments)
			{
				dStream.writeInt(i);
			}
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}

	}

	public static void writeClustering(final ClusteringJournal journal,
		final OutputStream fstream)
	{
		final DataOutputStream dStream = new DataOutputStream(fstream);
		try
		{
			final int opLength = journal.opCodes.size();
			final int intArgLength = journal.intArgs.size();

			dStream.writeInt(opLength);
			dStream.writeInt(intArgLength);

			for (final Byte b : journal.opCodes)
			{
				dStream.writeByte(b);
			}

			for (final Integer i : journal.intArgs)
			{
				dStream.writeInt(i);
			}

		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
	}


}
