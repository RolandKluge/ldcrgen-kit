package edu.kit.iti.ldcrgen.io.journaling;

public class GraphOperation
{
	static enum OpType
	{
		CreateNode(1, "CREATE_NODE"), RemoveNode(2, "REMOVE_NODE"), //
		CreateEdge(3, "CREATE_EDGE"), RemoveEdge(4, "REMOVE_EDGE"), //
		SetCluster(5, "SET_CLUSTER"), SetRefCluster(6, "SET_REF_CLUSTER"), //
		NextStep(7, "NEXT_STEP");


		final byte opcode;
		final String description;

		private OpType(final int opcode, final String description)
		{
			this.opcode = (byte) opcode;
			this.description = description;
		}

		byte getOpCode()
		{
			return this.opcode;
		}

		@Override
		public String toString()
		{
			return this.description;
		}
	};

	public int arg0;
	public int arg1;
	public int arg2;
	public OpType type;
	private final static GraphOperation NEXT_STEP = new GraphOperation(OpType.NextStep);

	private GraphOperation(final OpType type, final int arg0, final int arg1)
	{
		this.type = type;
		this.arg0 = arg0;
		this.arg1 = arg1;
	}

	private GraphOperation(final OpType type, final int arg0)
	{
		this.type = type;
		this.arg0 = arg0;
	}

	private GraphOperation(final OpType type)
	{
		this.type = type;
	}

	public static GraphOperation newCreateNodeOp(final int arg0, final int arg1)
	{
		return new GraphOperation(OpType.CreateNode, arg0, arg1);
	}

	public static GraphOperation newCreateRemoveNodeOp(final int arg0)
	{
		return new GraphOperation(OpType.RemoveNode, arg0);
	}

	public static GraphOperation newCreateEdgeOp(final int arg0, final int arg1)
	{
		return new GraphOperation(OpType.CreateEdge, arg0, arg1);
	}

	public static GraphOperation newRemoveEdgeOp(final int arg0, final int arg1)
	{
		return new GraphOperation(OpType.RemoveEdge, arg0, arg1);
	}

	public static GraphOperation newSetClusterOp(final int arg0, final int arg1)
	{
		return new GraphOperation(OpType.SetCluster, arg0, arg1);
	}

	public static GraphOperation newSetRefClusterOp(final int arg0, final int arg1)
	{
		return new GraphOperation(OpType.SetRefCluster, arg0);
	}

	public static GraphOperation newNextStepOp()
	{
		return NEXT_STEP;
	}

}
