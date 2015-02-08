package fr.upmc.colins.farm3.cpu;

import java.util.ArrayList;

import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>ControlRequestGeneratorOutboundPort</code> implements the outbound
 * port for a given component.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>ControlRequestArrivalI</code> interface as required
 * and upon a call, passes it to the connector that must also implement the same
 * interface.
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
public class			ControlRequestGeneratorOutboundPort
extends		AbstractOutboundPort
implements	ControlRequestArrivalI
{
	/**
	 * create the port with its URI and owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null && owner != null &&
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri
	 * @param owner
	 * @throws Exception
	 */
	public				ControlRequestGeneratorOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ControlRequestArrivalI.class, owner) ;

		assert	uri != null ;
		assert	owner.isRequiredInterface(ControlRequestArrivalI.class) ;
	}
 
	/**
	 * pass the new clock speed to the connector
	 */
	@Override
	public boolean updateClockSpeed(Double clockSpeed) throws Exception {
		return ((ControlRequestArrivalI)this.connector).updateClockSpeed(clockSpeed);
	}
	
	/**
	 * ask the core to update his clockspeed, shall the cpu be ok with that
	 */
	@Override
	public boolean updateClockSpeedPlease(Double clockSpeed) throws Exception {
		return ((ControlRequestArrivalI)this.connector).updateClockSpeedPlease(clockSpeed);
	}
	
	/**
	 * return the list of uri of inbound port to each cores
	 */
	@Override
	public ArrayList<String> getCoresRequestArrivalInboundPortUris()
			throws Exception {
		return ((ControlRequestArrivalI)this.connector).getCoresRequestArrivalInboundPortUris();
	}

	@Override
	public ArrayList<String> getCoresControlRequestArrivalInboundPortUris()
			throws Exception {
		return ((ControlRequestArrivalI)this.connector).getCoresControlRequestArrivalInboundPortUris();
	}

	@Override
	public Double getClockSpeed() throws Exception {
		return ((ControlRequestArrivalI)this.connector).getClockSpeed();
	}




}
