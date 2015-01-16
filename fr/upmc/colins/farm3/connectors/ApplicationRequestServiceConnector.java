package fr.upmc.colins.farm3.connectors;


import java.io.Serializable;

import fr.upmc.colins.farm3.admission.ApplicationRequestArrivalI;
import fr.upmc.colins.farm3.objects.Application;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>ApplicationRequestServiceConnector</code> implements the connector
 * between the outbound port of a component sending application request with the inbound
 * port of another component deploying them
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
 * <p>Created on : december 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			ApplicationRequestServiceConnector
extends		AbstractConnector
implements	ApplicationRequestArrivalI,	Serializable
{
	private static final long serialVersionUID = 1L;

	@Override
	public String acceptApplication(Application a) throws Exception {
		return ((ApplicationRequestArrivalI)this.offering).acceptApplication(a);
	}

}
