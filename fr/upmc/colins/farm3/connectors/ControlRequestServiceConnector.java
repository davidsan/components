package fr.upmc.colins.farm3.connectors;


import java.io.Serializable;
import java.util.ArrayList;

import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>ControlRequestServiceConnector</code> implements the connector
 * between the outbound port of a component sending control requests with the inbound
 * port of another controlled component.
 *
 * <p><strong>Description</strong></p>
 * 
 * Simply pass the method call to the offering inbound port.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 23 nov. 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			ControlRequestServiceConnector
extends		AbstractConnector
implements	ControlRequestArrivalI,	Serializable
{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean updateClockSpeed(Double clockSpeed) throws Exception {
		return ((ControlRequestArrivalI)this.offering).updateClockSpeed(clockSpeed);
	}

	@Override
	public ArrayList<String> getCoresRequestArrivalInboundPortUris()
			throws Exception {
		return ((ControlRequestArrivalI) this.offering)
				.getCoresRequestArrivalInboundPortUris();
	}

	@Override
	public ArrayList<String> getCoresControlRequestArrivalInboundPortUris()
			throws Exception {
		return ((ControlRequestArrivalI) this.offering)
		.getCoresControlRequestArrivalInboundPortUris();
	}

	@Override
	public Double getClockSpeed() throws Exception {
		return ((ControlRequestArrivalI)this.offering).getClockSpeed();
	}


}
