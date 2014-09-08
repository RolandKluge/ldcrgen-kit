package edu.kit.iti.ldcrgen.io.journaling;

import java.util.ArrayList;
import java.util.Iterator;

import edu.kit.iti.ldcrgen.io.journaling.ClusteringOperation.OpType;

public class ClusteringJournal implements Iterable<ClusteringOperation>
{
	public ArrayList<Byte> opCodes;
	public ArrayList<Integer> intArgs;

	public ClusteringJournal()
	{
		this.opCodes = new ArrayList<Byte>();
		this.intArgs = new ArrayList<Integer>();
	}

	public void nextStepOp()
	{
		this.opCodes.add(OpType.NextStep.getOpCode());
	}

	/**
	 * Store a cluster merge event operation.
	 *
	 * @param c1
	 *            merge event: first cluster to be merged
	 * @param c2
	 *            merge event: second cluster to be merged
	 * @param c3
	 *            merge event: merge product
	 */
	public void mergeOp(final int c1, final int c2, final int c3)
	{
		this.opCodes.add(OpType.Merge.getOpCode());
		this.intArgs.add(c1);
		this.intArgs.add(c2);
		this.intArgs.add(c3);
	}


	/**
	 * Store a cluster split event operation.
	 *
	 * @param c1
	 *            split event: cluster to be split
	 * @param c2
	 *            split event: first split product
	 * @param c3
	 *            split event: second split product
	 */
	public void splitOp(final int c1, final int c2, final int c3)
	{
		this.opCodes.add(OpType.Split.getOpCode());
		this.intArgs.add(c1);
		this.intArgs.add(c2);
		this.intArgs.add(c3);
	}


	public void mergeDone(final int c1, final int c2, final int c3)
	{
		this.opCodes.add(OpType.MergeDone.getOpCode());
		this.intArgs.add(c1);
		this.intArgs.add(c2);
		this.intArgs.add(c3);
	}

	public void splitDone(final int c1, final int c2, final int c3)
	{
		this.opCodes.add(OpType.SplitDone.getOpCode());
		this.intArgs.add(c1);
		this.intArgs.add(c2);
		this.intArgs.add(c3);
	}

	@Override
	public Iterator<ClusteringOperation> iterator()
	{

		class ClusteringJournalIterator implements Iterator<ClusteringOperation>
		{

			int opIndex;
			int argIndex;
			ArrayList<Byte> opCodes;
			ArrayList<Integer> intArgs;

			public ClusteringJournalIterator(final ClusteringJournal journal)
			{
				this.opIndex = -1;
				this.argIndex = -1;
				this.opCodes = journal.opCodes;
				this.intArgs = journal.intArgs;
			}

			@Override
			public boolean hasNext()
			{
				return (opIndex != opCodes.size() - 1);
			}

			@Override
			public ClusteringOperation next()
			{
				byte opCode = this.opCodes.get(++opIndex);
				ClusteringOperation result = null;
				if (opCode == 0)
				{
					result = ClusteringOperation.newNextStepOp();
				}
				else if (opCode == 1 || opCode == 2 || opCode == 3 || opCode == 4)
				{
					int arg0 = this.intArgs.get(++argIndex);
					int arg1 = this.intArgs.get(++argIndex);
					int arg2 = this.intArgs.get(++argIndex);

					switch (opCode)
					{
						case ((byte) 1):
							result = ClusteringOperation.newMergeOp(arg0, arg1, arg2);
							break;
						case ((byte) 2):
							result = ClusteringOperation.newSplitOp(arg0, arg1, arg2);
							break;
						case ((byte) 3):
							result = ClusteringOperation.newMergeDoneOp(arg0, arg1, arg2);
							break;
						case ((byte) 4):
							result = ClusteringOperation.newSplitDoneOp(arg0, arg1, arg2);
							break;
					}
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

		return new ClusteringJournalIterator(this);
	}
}
