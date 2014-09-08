package edu.kit.iti.ldcrgen.io;

public class ParseException extends Exception
{
	private static final long serialVersionUID = 3058948470247503624L;
	
	public ParseException()
	{
		super("Parse exception!");
	}
	
	public ParseException(final String message)
	{
		super("Parse exception: " + message);
	}
	
	public ParseException(final Throwable cause)
	{
		super(cause);
	}
	
	public ParseException(final Throwable cause, final String message)
	{
		super("Parse exception: " + message, cause);
	}
}
