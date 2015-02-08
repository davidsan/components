package fr.upmc.colins.farm3.core;

import fr.upmc.components.interfaces.TwoWayI;

/**
 * The interface <code>Core2CpuI</code> defines the protocol between the core and the cpu
 *
 * <p><strong>Description</strong></p>
 * 
 * The interface can be both required and offered.
 * 
 * <p>Created on : feb. 2014</p>
 * 
 * @author	Colins
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public interface		Core2CpuI
extends		TwoWayI
{
	/**
	 * ask the core to update the clockspeed of the core passed in parameter
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	a != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param clockspeed 	the clockspeed
	 * @param coreId 		the core identifier
	 * @throws Exception
	 * @return 				true if all the request was granted
	 */
	Boolean	acceptUpdateClockspeedRequest(Double clockspeed, Integer coreId) throws Exception ;
	
}
