package edu.kit.iti.ldcrgen.io.journaling;

public class ClusteringOperation
{

	static enum OpType
	{
		NextStep(0, "NEXT_STEP"), Merge(1, "MERGE"), Split(2, "SPLIT"), MergeDone(3, "MERGE_DONE"), SplitDone(4, "SPLIT_DONE");

		final byte opcode;
		final String description;

		private OpType(final int opcode, final String description)
		{
			this.opcode = (byte)opcode;
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
	public ClusteringOperation.OpType type;

	private static final ClusteringOperation NEXT_STEP = new ClusteringOperation(OpType.NextStep);

	private ClusteringOperation(final OpType op)
	{
		this.type = op;
	}

	private ClusteringOperation(final OpType op, final int arg0, final int arg1, final int arg2)
	{
		this.type = op;
		this.arg0 = arg0;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	public static ClusteringOperation newNextStepOp()
	{
		return NEXT_STEP;
	}

	public static ClusteringOperation newMergeOp(final int arg0, final int arg1, final int arg2)
	{
		return new ClusteringOperation(OpType.Merge, arg0, arg1, arg2);
	}

	public static ClusteringOperation newMergeDoneOp(final int arg0, final int arg1, final int arg2)
	{
		return new ClusteringOperation(OpType.MergeDone, arg0, arg1, arg2);
	}

	public static ClusteringOperation newSplitOp(final int arg0, final int arg1, final int arg2)
	{
		return new ClusteringOperation(OpType.Split, arg0, arg1, arg2);
	}

	public static ClusteringOperation newSplitDoneOp(final int arg0, final int arg1, final int arg2)
	{
		return new ClusteringOperation(OpType.SplitDone, arg0, arg1, arg2);
	}
}
