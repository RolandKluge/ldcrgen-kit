package edu.kit.iti.ldcrgen.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class CommandLineArgumentsTest
{
	@Test
	public void testRegex()
	{
		Assert.assertArrayEquals(//
			new String[] { "first=bla", "second=tut" },//
			"first=bla second=tut".split("\\s+"));
		Assert.assertArrayEquals(//
			new String[] { "first=bla", "second=tut" },//
			"first=bla             second=tut".split("\\s+"));
		Assert.assertArrayEquals(//
			new String[] { "first=bla", "second=tut" },//
			"first=bla          \t  \n  \t   second=tut".split("\\s+"));
	}

	@Test
	public void testSplittingRespectingSpaces()
	{
		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"]+|\"([^\"]+)\"");
		Matcher regexMatcher = regex.matcher("test=123 \"test=quoted text\" test2=blablub");
		while (regexMatcher.find())
		{
			if (regexMatcher.group(1) != null)
			{
				// Add double-quoted string without the quotes
				matchList.add(regexMatcher.group(1));
			}
			else
			{
				// Add unquoted word
				matchList.add(regexMatcher.group());
			}
		}
//		System.out.println(matchList);
	}
}
