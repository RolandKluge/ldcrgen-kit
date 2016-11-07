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
		int graphOpCounter = 1;
		for (final GraphOperation op : gJournal)
		{
			System.out.println("#" + graphOpCounter + ": " + op.type + " " + op.arg0 + " " + op.arg1 + " " + op.arg2 + "\t\t" + op.type.documentation);
			graphOpCounter++;
		}

		System.out.println("Clustering Journal:");
		int clusterOpCounter = 1;
		for (final ClusteringOperation op : clJournal)
		{
			System.out.println("#" + clusterOpCounter + ": " + op.type + " " + op.arg0 + " " + op.arg1 + " " + op.arg2 + "\t\t" + op.type.documentation);
			clusterOpCounter++;
		}
	}
	
	 /**
	 * Converts the given binary file (first argument) into a  text file.
	 *  graphj_path: the binary file 
	 *  nodesPath: A text file which contains names of the nodes
	 *  netPath: A text file which contains the network
	 *  changeLog: A text file which contains the stream of atomic changes
	 */
	public static void ConvertGraphjToText(string graphj_path, string nodesPath, string netPath, string changeLog)
	{
		
		
		BufferedInputStream fstream = null;
		try
		{
			fstream = new BufferedInputStream(new FileInputStream(graphj_path));
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


		int graphOpCounter = 1;
		int node_id = 1;
		boolean sw = true;
		try
		{
			File file_nodes = new File(nodesPath);
			FileWriter fw_nodes = new FileWriter(file_nodes.getAbsoluteFile());
			BufferedWriter bw_nodes = new BufferedWriter(fw_nodes);
			
			File file_net = new File(netPath);
			FileWriter fw_net = new FileWriter(file_net.getAbsoluteFile());
			BufferedWriter bw_net = new BufferedWriter(fw_net);
			
			File file_change = new File(changeLog);
			FileWriter fw_change = new FileWriter(file_change.getAbsoluteFile());
			BufferedWriter bw_change = new BufferedWriter(fw_change);
			
			
			for (final GraphOperation op : gJournal)
			{
				
				switch(op.type)
				{
					case CreateNode:
						if(sw){
							bw_nodes.write(node_id + " " + op.arg0 + "\n");
							node_id++;
						}
						else{
							bw_change.write("ADD NODE" + "\n" +node_id + " " + op.arg0 + "\n");
							node_id++;
						}
							
						break;
						
						
					case CreateEdge:
						if(sw) bw_net.write(op.arg0 + " " + op.arg1 + "\n");
						else bw_change.write("ADD EDGE" + "\n" + op.arg0 + " " + op.arg1 + "\n"); 
						break;
						
					case NextStep:
						sw = false;
						bw_change.write("NEXT STEP"+"\n"); 
						break;
					

					
					case RemoveEdge:
						bw_change.write("REMOVE EDGE" + "\n" + op.arg0 + " " + op.arg1 + "\n");
						break;	
						
					case RemoveNode:
						bw_change.write("REMOVE NODE" + "\n" + op.arg0 + " " + op.arg1 + "\n");
						break;
						
				}
				
				graphOpCounter++;
			}
			
			bw_change.close();
			bw_net.close();
			bw_nodes.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
}
