package fr.upmc.colins.farm3.objects;

import java.io.Serializable;

/**
 * The class <code>Request</code> defines objects representing requests.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * A request has a unique identifier (URI) and a number of instructions set at
 * creation time. When it arrives at a service provider, the arrival time can be
 * set. The arrival time can then be used later on when the execution of the
 * request finishes to compute the service time (waiting + processing) of the
 * request.
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
 * Created on : 2 sept. 2014
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class			Request
implements 	Serializable
{
	private static final long serialVersionUID = 1L;

	/** unique identifier of the request, for tracing purposes.				*/
	protected int		uri ;
	/** application identifier												*/
	protected int 		appId ;
	/** the number of instructions											*/
	protected long 		nrofInstructions ;
	/** time at which it has been received by the service provider.			*/
	protected long		arrivalTime ;

	/**
	 * create a new request with given uri and processing time.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri				unique identifier of the new request.
	 * @param appId				application identifier
	 */
	public Request(int uri, int appId)
	{
		super() ;

		this.uri = uri ;
		this.appId = appId ;
		this.nrofInstructions = 0;
		this.arrivalTime = 0 ;

		assert this.arrivalTime >= 0 ;
	}

	/**
	 * return the application identifier
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the application identifier.
	 */
	public long			getAppId() {
		return this.appId;
	}
	
	/**
	 * return the number of instructions
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the number of instructions
	 */
	public long			getNrofInstructions() {
		return this.nrofInstructions;
	}

	/**
	 * sets the number of instructions
	 * @param nrofInstructions the number of instructions
	 */
	public void setNrofInstructions(long nrofInstructions) {
		this.nrofInstructions = nrofInstructions;
	}

	/**
	 * return the time at which the request has been received by the service
	 * provider.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the time at which the request has been received by the service provider.
	 */
	public long			getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * sets the time at which the request has been received by the service
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

	public int getUri() {
		return uri;
	}

}
