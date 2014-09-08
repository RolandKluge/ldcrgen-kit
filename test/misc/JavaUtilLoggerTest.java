package misc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

public class JavaUtilLoggerTest
{
	@Ignore
	@Test
	public void testLogDebugForNoOutput()
	{
		Log log = LogFactory.getLog(JavaUtilLoggerTest.class);
		final long numLogs = (long)1e9;
		long start = System.currentTimeMillis();
		for (int i = 0; i < numLogs ; ++i){
			log.debug("Test msg");
		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken: " + (end - start)/1000.0);
	}
}
