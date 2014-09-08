package edu.kit.iti.ldcrgen.data_structures;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.BooleanUtils;
import edu.kit.iti.ldcrgen.util.Pair;

public class PairTest
{
	@Test
	public void testMisc()
	{
		final Pair<String> pair1 = new Pair<String>("", "test");
		Assert.assertEquals(pair1.getSecond(), pair1.getReversedPair().getFirst());
		Assert.assertEquals(pair1.getFirst(), pair1.getReversedPair().getSecond());
		Assert.assertEquals(pair1, pair1.getReversedPair().getReversedPair());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testOutOfBounds()
	{
		final Pair<Integer> pair1 = new Pair<Integer>(102,103);
		Assert.assertTrue(pair1.find(100) < 0);
		Assert.assertNull(pair1.getOther(100));
		new Pair<Float>(1.0f, 2.5f).get(2);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testEqualsAndHashCode()
	{
		final List<Pair> pairs = new ArrayList<Pair>();
		for (int i = 0; i < 10; ++i)
		{
			pairs.add(new Pair<String>("" + i, "" + i * i));
			pairs.add(new Pair<String>("" + i, "" + i * i));
			pairs.add(new Pair<String>("" + i, "" + i * i + " + 1"));
		}

		for (int i = 0; i < 10; ++i)
		{
			pairs.add(new Pair<Object>(new Object(), new Object()));
		}

		for (int i = 0; i < 10; ++i)
		{
			pairs.add(new Pair<Node>(new Node(), new Node()));
		}

		pairs.add(null);
		pairs.add(null);
		pairs.add(null);

		final String sameString = "aloha";
		pairs.add(new Pair<String>(sameString, "trulla"));
		pairs.add(new Pair<String>(sameString, "tralla"));

		for (int i = 0; i < pairs.size(); ++i)
		{
			// reflexivity
			final Pair fst = pairs.get(i);
			if (null != fst)
			{
				Assert.assertEquals(fst, fst);
				Assert.assertFalse(fst.equals(null));

				for (int j = 0; j < pairs.size(); ++j)
				{
					final Pair snd = pairs.get(j);

					if (null != snd)
					{
						// symmetry
						Assert
							.assertTrue(BooleanUtils.equivalent(fst.equals(snd), snd.equals(fst)));

						//
						Assert.assertTrue(BooleanUtils.implies(fst.equals(snd),
							fst.hashCode() == snd.hashCode()));

						for (int k = 0; k < pairs.size(); ++k)
						{
							final Pair thd = pairs.get(k);

							// transitivity
							Assert.assertTrue(BooleanUtils.implies(
								fst.equals(snd) && snd.equals(thd),
								fst.equals(thd)));
						}
					}
					else
					{
						Assert.assertFalse(fst.equals(snd));
					}
				}
			}
		}

		for (int i = 0; i < pairs.size(); ++i)
		{
			final Pair pair = pairs.get(i);
			if (pair != null)
			{
				Assert.assertFalse(pair.equals(new Object()));
				Assert.assertFalse(pair.equals("Hallo Test!"));
			}
		}
	}
}
