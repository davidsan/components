package fr.upmc.colins.farm3.core;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>Core2CpuOutboundPort</code> implements the outbound
 * port for a given component.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>Core2CpuI</code> interface as required
 * and upon a call, passes it to the connector that must also implement the same
 * interface.
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
public class Core2CpuOutboundPort
extends		AbstractOutboundPort
implements	Core2CpuI
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
	public				Core2CpuOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, Core2CpuI.class, owner) ;

		assert	uri != null ;
		assert	owner.isRequiredInterface(Core2CpuI.class) ;
	}

	/**
	 * send clock speed request to its core
	 */
	@Override
	public Boolean acceptUpdateClockspeedRequest(Double clockspeed,
			Integer coreId) throws Exception {
		return ((Core2CpuI)this.connector).acceptUpdateClockspeedRequest(clockspeed, coreId);
	}
  

}