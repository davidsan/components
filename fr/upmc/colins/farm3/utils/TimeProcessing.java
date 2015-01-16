package fr.upmc.colins.farm3.utils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * The class <code>TimeProcessing</code> implements utilities for processing
 * time.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 4 sept. 2014</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			TimeProcessing
{
	/**
	 * transform a time in milliseconds into a the more readable format
	 * HH:MM:SS:MS.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	millis >= 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param millis	time in milliseconds from January 1970 to be formatted.
	 * @return	 		a string in the format HH:MM:SS:MS.
	 */
	public static String	toString(long millis)
	{
		assert	millis >= 0 ;

		Calendar cal = (Calendar.getInstance()) ;
		cal.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		cal.setTimeInMillis(millis) ;

		return 	"" + (((cal.get(Calendar.AM_PM) == Calendar.AM) ? 0 : 12) +
													cal.get(Calendar.HOUR)) +
				":" + cal.get(Calendar.MINUTE) +
				":" + cal.get(Calendar.SECOND) +
				":" + cal.get(Calendar.MILLISECOND) ;
	}
}
