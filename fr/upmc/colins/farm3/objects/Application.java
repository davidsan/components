package fr.upmc.colins.farm3.objects;

import java.io.Serializable;

/**
 * The class <code>Application</code> defines objects representing application
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * An application has a unique identifier (URI), a mean number of instructions set at
 * creation time and a standard deviation.
 * 
 * As the object can be passed as parameter of a remote method call, the class
 * implements the Java interface <code>Serializable</code>.
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * <pre>
 * invariant	processingTime > 0 && arrivalTime >= 0
 * </pre>
 * 
 * <p>
 * Created on : december 2014
 * </p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class			Application
implements 	Serializable
{
	private static final long serialVersionUID = 1L;

	/** unique identifier of the request, for tracing purposes.				*/
	protected int		uri ;
	/** mean number of instructions											*/
	protected double 	meanNrofInstructions; 
	/** standard deviation 													*/
	protected double 	standardDeviation;
	/** time at which it has been received by the service provider.			*/
	protected long		arrivalTime ;

	/**
	 * create a new application with given uri, a mean number of instructions
	 * and a standard deviation
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	meanNrofInstructions > 0
	 * post	true				// no more postconditions.
	 * </pre>
	 *
	 * @param uri				unique identifier of the nesw request.
	 * @param nrofInstructions	mean number of instructions 
	 */
	public				Application(
		int uri,
		double meanNrofInstructions,
		double standardDeviation
		)
	{
		super() ;

		assert	meanNrofInstructions > 0;

		this.uri = uri ;
		this.meanNrofInstructions = meanNrofInstructions ;
		this.standardDeviation = standardDeviation ;
		this.arrivalTime = 0 ;

		assert	this.meanNrofInstructions >= 0 && this.arrivalTime >= 0 ;
	}
	
	

	/**
	 * return the application uri
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return 	the uri
	 */
	public int getUri() {
		return uri;
	}


	/**
	 * return the mean number of instructions
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @return the mean number of instructions
	 */
	public double getMeanNrofInstructions() {
		return meanNrofInstructions;
	}


	/**
	 * return the standard deviation
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @return the standar deviation
	 */
	public double getStandardDeviation() {
		return standardDeviation;
	}


	/**
	 * return the time at which the application has been received by the service
	 * provider.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the time at which the application has been received by the service provider.
	 */
	public long			getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * sets the time at which the application has been received by the service
	 * provider.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	arrivalTime > 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param arrivalTime
	 */
	public void			setArrivalTime(long arrivalTime)
	{
		assert	arrivalTime > 0 ;

		this.arrivalTime = arrivalTime;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String		toString() {
		return "" + this.uri ;
	}
}
