package fr.upmc.colins.farm3.actuator;

import java.util.ArrayList;

import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.colins.farm3.core.Core;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
/**
 * The class <code>CoreControlRequestArrivalOutboundPort</code> implements the  port
 * for a component receiving control request from other components.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>ControlRequestArrivalI</code> interface as offered
 * and upon a call, passes it to the owner component that must also implement
 * the method <code>updateClockSpeed</code>.
 * 
 * <p>Created on : Janv 2015</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */

public class ActuatorControlCoreOutboundPort extends 
		AbstractOutboundPort 
			implements ControlRequestArrivalI{



	public ActuatorControlCoreOutboundPort(String uri,
		 ComponentI owner) throws Exception {
		super(uri, ControlRequestArrivalI.class,owner);
		assert	uri != null ;
		assert	owner.isRequiredInterface(ControlRequestArrivalI.class) ;
	}

	@Override
	public boolean updateClockSpeed(Double cs) throws Exception {
		//System.out.println("before-------------------");
		Boolean rep=((ControlRequestArrivalI)this).updateClockSpeed(cs);
		System.out.println("here#####################################");
		return rep;


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
