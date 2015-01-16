package fr.upmc.colins.farm3.core;

import fr.upmc.colins.farm3.objects.Request;
import fr.upmc.components.interfaces.TwoWayI;

/**
 * The interface <code>RequestArrivalI</code> defines the protocol to send a
 * request between an sender and a receiver
 *
 * <p><strong>Description</strong></p>
 * 
 * The interface can be both required and offered, and it has only one method
 * <code>acceptRequest</code> passing a request object as parameter.
 * 
 * <p>Created on : 2 sept. 2014</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public interface		RequestArrivalI
extends		TwoWayI
{
	/**
	 * accept a new request for servicing.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r				request to be serviced.
	 * @throws Exception	
	 */
	void			acceptRequest(Request r) throws Exception ;

	/**
	 * connect the response outbound port to the port pointed by the URI
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	responseArrivalInboundPortUri != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param responseArrivalInboundPortUri	uri of the inbound port
	 * @throws Exception	
	 */
	void 			connectResponseConnection(String responseArrivalInboundPortUri) throws Exception;
	
}
