package fr.upmc.colins.farm3.objects;

import java.io.Serializable;

/**
 * The class <code>Response</code> defines objects representing a response.
 *
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * A response is composed of an uri and the duration time taken by the request
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
 * Created on : jan. 2015
 * </p>
 *
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class			Response
    implements 	Serializable
{
    private static final long serialVersionUID = 1L;

    /** unique identifier of the response, for tracing purposes.			*/
    protected int		uri ;
    /** time at which it has been received by the service consumer.			*/
    protected Double	duration ;

    /**
     * create a new response with given uri and processing time.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true			// no precondition.
     * post	true			// no postcondition.
     * </pre>
     *
     * @param uri				unique identifier of the new request.
     */
    public				Response(
        int uri
    )
    {
        super() ;


        this.uri = uri ;
        this.duration = 0.0 ;

        assert	this.duration >= 0 ;
    }

    /**
     * return the duration
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	true			// no precondition.
     * post	true			// no postcondition.
     * </pre>
     *
     * @return	the duration
     */
    public Double		getDuration()
    {
        return duration;
    }

    /**
     * sets the duration
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	arrivalTime > 0
     * post	true			// no postcondition.
     * </pre>
     *
     * @param duration
     */
    public void			setDuration(Double duration)
    {
        assert	duration > 0 ;

        this.duration = duration;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String		toString()
    {
        return "" + this.uri ;
    }
}
