package fr.upmc.colins.farm3.connectors;


import java.io.Serializable;

import fr.upmc.colins.farm3.core.ResponseArrivalI;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>ResponseServiceConnector</code> implements the connector
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
public class			ResponseServiceConnector
extends		AbstractConnector
implements	ResponseArrivalI, Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Override
	public void acceptResponse(Response response) throws Exception {
		((ResponseArrivalI) this.offering).acceptResponse(response);
	}

}
