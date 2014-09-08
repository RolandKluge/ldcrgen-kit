package edu.kit.iti.ldcrgen.io.journaling;

public class GraphOperation
{
	public static enum OpType
	{
		CreateNode(1, "CREATE_NODE", "Arg0: cluster C, Arg1: cluster Cref"), //
		RemoveNode(2, "REMOVE_NODE", "Arg0: node index"), //
		CreateEdge(3, "CREATE_EDGE", "Arg0: node u; Arg1: node v"),  //
		RemoveEdge(4, "REMOVE_EDGE", "Arg0: node u; Arg1: node v"), //
		SetCluster(5, "SET_CLUSTER", "Arg0: node u; Arg1: cluster C"),
		SetRefCluster(6, "SET_REF_CLUSTER", "Arg0: node u; Arg1: cluster C"), //
		NextStep(7, "NEXT_STEP", "");


		public final byte opcode;
		public final String label;
		public final String documentation;

		private OpType(final int opcode, final String label, final String documentation)
		{
			this.opcode = (byte) opcode;
			this.label = label;
			this.documentation = documentation;
		}

		byte getOpCode()
		{
			return this.opcode;
		}

		@Override
		public String toString()
		{
			return this.label;
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
