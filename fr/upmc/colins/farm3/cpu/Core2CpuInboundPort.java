package fr.upmc.colins.farm3.cpu;

import fr.upmc.colins.farm3.core.Core2CpuI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>Core2CpuInboundPort</code> implements the inbound port
 * for a component receiving requests from the core
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>Core2CpuI</code> interface as offered.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : feb. 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			Core2CpuInboundPort
extends		AbstractInboundPort
implements	Core2CpuI
{
	private static final long serialVersionUID = 1L;

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null && owner != null
	 * pre	owner.isOfferedInterface(RequestArrivalI.class)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			URI of the port.
	 * @param owner			owner component of the port.
	 * @throws Exception
	 */
	public				Core2CpuInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, Core2CpuI.class, owner) ;

		assert	uri != null && owner != null ;
		assert	owner.isOfferedInterface(Core2CpuI.class) ;
	}



	@Override
	public Boolean acceptUpdateClockspeedRequest(Double clockspeed,
			Integer coreId) throws Exception {
		final Cpu sp = (Cpu) this.owner;
		return sp.acceptUpdateClockspeedRequest(clockspeed, coreId);				
	}


}
