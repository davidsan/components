package fr.upmc.colins.farm3.connectors;


import java.io.Serializable;

import fr.upmc.colins.farm3.core.RequestArrivalI;
import fr.upmc.colins.farm3.objects.Request;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>RequestServiceConnector</code> implements the connector
 * between the outbound port of a component sending requests with the inbound
 * port of another component servicing them.
 *
 * <p><strong>Description</strong></p>
 * 
 * Simply pass the request to the offering inbound port.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 3 sept. 2014</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			RequestServiceConnector
extends		AbstractConnector
implements	RequestArrivalI, Serializable
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see fr.upmc.alasca.ssqueue.interfaces.RequestArrivalI#acceptRequest(fr.upmc.alasca.ssqueue.objects.Request)
	 */
	@Override
	public void			acceptRequest(Request c) throws Exception
	{
		((RequestArrivalI)this.offering).acceptRequest(c) ;
	}

	@Override
	public void connectResponseConnection(String vmResponseArrivalInboundPortUri) throws Exception {
		((RequestArrivalI)this.offering).connectResponseConnection(vmResponseArrivalInboundPortUri) ;
	}

}
