package fr.upmc.colins.farm3.cpu;

import java.util.ArrayList;

import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>CpuControlRequestArrivalInboundPort</code> implements the inbound port
 * for a cpu receiving control request from other components.
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
public class			CpuControlRequestArrivalInboundPort
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
	public				CpuControlRequestArrivalInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ControlRequestArrivalI.class, owner) ;

		assert	uri != null && owner != null ;
		assert	owner.isOfferedInterface(ControlRequestArrivalI.class) ;
	}
	

	@Override
	public boolean updateClockSpeed(Double clockSpeed) throws Exception {
		final Cpu fc = (Cpu) this.owner ;
		final Double fcs = clockSpeed ;
		return fc.updateClockSpeed(fcs);
	}


	@Override
	public ArrayList<String> getCoresRequestArrivalInboundPortUris() throws Exception {
		final Cpu fc = (Cpu) this.owner;
		ArrayList<String> res = fc
				.handleRequestSync(new ComponentService<ArrayList<String>>() {
					@Override
					public ArrayList<String> call() throws Exception {
						return fc.getCoresRequestArrivalInboundPortUris();
					}
				});

		return res;
	}


	@Override
	public ArrayList<String> getCoresControlRequestArrivalInboundPortUris()
			throws Exception {
		final Cpu fc = (Cpu) this.owner;
		ArrayList<String> res = fc
				.handleRequestSync(new ComponentService<ArrayList<String>>() {
					@Override
					public ArrayList<String> call() throws Exception {
						return fc.getCoresControlRequestArrivalInboundPortUris();
					}
				});

		return res;
	}

}
