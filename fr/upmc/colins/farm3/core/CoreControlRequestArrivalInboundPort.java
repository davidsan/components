package fr.upmc.colins.farm3.core;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>CoreControlRequestArrivalInboundPort</code> implements the inbound port
 * for a component receiving control request from other components.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>ControlRequestArrivalI</code> interface as offered
 * and upon a call, passes it to the owner component that must also implement
 * the method <code>updateClockSpeed</code>.
 * 
 * <p>Created on : 23 nov. 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			CoreControlRequestArrivalInboundPort
extends		AbstractInboundPort
implements	ControlRequestArrivalI
{
	private static final long serialVersionUID = 1L;

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null && owner != null
	 * pre	owner.isOfferedInterface(ControlRequestArrivalI.class)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			URI of the port.
	 * @param owner			owner component of the port.
	 * @throws Exception
	 */
	public				CoreControlRequestArrivalInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ControlRequestArrivalI.class, owner) ;

		assert	uri != null && owner != null ;
		assert	owner.isOfferedInterface(ControlRequestArrivalI.class) ;
	}
	

	@Override
	public boolean updateClockSpeed(Double cs) 
			throws Exception 
	{
		final Core sp = (Core) this.owner ;
		final Double fcs = cs ;
		System.out.println("here#####################################");
		return sp.updateClockSpeed(fcs);
	}


	@Override
	public ArrayList<String> getCoresRequestArrivalInboundPortUris()
			throws Exception {
		final Core sp = (Core) this.owner;
		ArrayList<String> l = new ArrayList<String>();
		l.add(sp.getInboundPortURI());
		return l;
	}

	@Override
	public ArrayList<String> getCoresControlRequestArrivalInboundPortUris()
			throws Exception {
		final Core sp = (Core) this.owner;
		ArrayList<String> l = new ArrayList<String>();
		l.add(sp.getControlInboundPortURI());
		return l;
	}

}
