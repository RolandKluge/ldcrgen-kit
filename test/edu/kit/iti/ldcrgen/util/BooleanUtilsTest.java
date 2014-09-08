package edu.kit.iti.ldcrgen.util;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.iti.ldcrgen.util.BooleanUtils;

public class BooleanUtilsTest
{
	@Test
	public void testImplies()
	{
		Assert.assertTrue(BooleanUtils.implies(true, true));
		Assert.assertFalse(BooleanUtils.implies(true, false));
		Assert.assertTrue(BooleanUtils.implies(false, true));
		Assert.assertTrue(BooleanUtils.implies(false, false));
	}

	@Test
	public void testEquivalent()
	{
		Assert.assertTrue(BooleanUtils.equivalent(true, true));
		Assert.assertFalse(BooleanUtils.equivalent(true, false));
		Assert.assertFalse(BooleanUtils.equivalent(false, true));
		Assert.assertTrue(BooleanUtils.equivalent(false, false));
	}

	@Test
	public void testXor()
	{
		Assert.assertFalse(BooleanUtils.xor(true, true));
		Assert.assertTrue(BooleanUtils.xor(true, false));
		Assert.assertTrue(BooleanUtils.xor(false, true));
		Assert.assertFalse(BooleanUtils.xor(false, false));
	}
}
