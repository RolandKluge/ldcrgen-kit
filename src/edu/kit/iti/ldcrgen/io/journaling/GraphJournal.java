package edu.kit.iti.ldcrgen.io.journaling;

import java.util.ArrayList;
import java.util.Iterator;

import edu.kit.iti.ldcrgen.io.journaling.GraphOperation.OpType;

public class GraphJournal implements Iterable<GraphOperation>
{

	public ArrayList<Byte> opCodes;
	public ArrayList<Integer> arguments;


	public GraphJournal()
	{
		this.opCodes = new ArrayList<Byte>();
		this.arguments = new ArrayList<Integer>();
	}

	// write

	public void createNodeOp(final int clusterId, final int refClusterId)
	{
		this.opCodes.add(OpType.CreateNode.getOpCode());
		this.arguments.add(clusterId);
		this.arguments.add(refClusterId);
	}

	public void removeNodeOp(final int nodeId)
	{
		this.opCodes.add(OpType.RemoveNode.getOpCode());
		this.arguments.add(nodeId);
	}

	public void createEdgeOp(final int sourceId, final int targetId)
	{
		this.opCodes.add(OpType.CreateEdge.getOpCode());
		this.arguments.add(sourceId);
		this.arguments.add(targetId);
	}

	public void removeEdgeOp(final int sourceId, final int targetId)
	{
		this.opCodes.add(OpType.RemoveEdge.getOpCode());
		this.arguments.add(sourceId);
		this.arguments.add(targetId);
	}

	public void setClusterOp(final int nodeId, final int clusterId)
	{
		this.opCodes.add(OpType.SetCluster.getOpCode());
		this.arguments.add(nodeId);
		this.arguments.add(clusterId);
	}

	public void setRefClusterOp(final int nodeId, final int refClusterId)
	{
		this.opCodes.add(OpType.SetRefCluster.getOpCode());
		this.arguments.add(nodeId);
		this.arguments.add(refClusterId);
	}

	public void nextStepOp()
	{
		this.opCodes.add(OpType.NextStep.getOpCode());
	}



	@Override
	public Iterator<GraphOperation> iterator()
	{

		class JournalIterator implements Iterator<GraphOperation>
		{

			int opIndex;
			int argIndex;
			ArrayList<Byte> opCodes;
			ArrayList<Integer> arguments;

			public JournalIterator(final GraphJournal journal)
			{
				this.opIndex = -1;
				this.argIndex = -1;
				this.opCodes = journal.opCodes;
				this.arguments = journal.arguments;
			}

			@Override
			public boolean hasNext()
			{
				return (opIndex != opCodes.size() - 1);
			}

			@Override
			public GraphOperation next()
			{
				final byte opCode = this.opCodes.get(++opIndex);
				GraphOperation result = null;

				if (opCode == 2)
				{
					int arg0 = this.arguments.get(++argIndex);
					return GraphOperation.newCreateRemoveNodeOp(arg0);
				}
				else if (opCode >= 1 && opCode <= 6)
				{
					int arg0 = this.arguments.get(++argIndex);
					int arg1 = this.arguments.get(++argIndex);
					switch (opCode)
					{
						case ((byte) 1):
							result = GraphOperation.newCreateNodeOp(arg0, arg1);
							break;
						case ((byte) 3):
							result = GraphOperation.newCreateEdgeOp(arg0, arg1);
							break;
						case ((byte) 4):
							result = GraphOperation.newRemoveEdgeOp(arg0, arg1);
							break;
						case ((byte) 5):
							result = GraphOperation.newSetClusterOp(arg0, arg1);
							break;
						case ((byte) 6):
							result = GraphOperation.newSetRefClusterOp(arg0, arg1);
							break;
					}
				}
				else if (opCode == 7)
				{
					return GraphOperation.newNextStepOp();
				}

				return result;

			}

			@Override
			@Deprecated
			public void remove()
			{
				throw new UnsupportedOperationException("Read-only iterator");
			}

		}
		;

		return new JournalIterator(this);
	}



}
