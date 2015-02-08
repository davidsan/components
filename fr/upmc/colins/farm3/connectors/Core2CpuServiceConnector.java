package fr.upmc.colins.farm3.connectors;

import java.io.Serializable;

import fr.upmc.colins.farm3.core.Core2CpuI;
import fr.upmc.components.connectors.AbstractConnector;
/**
 * The class <code>Core2CpuServiceConnector</code> implements the connector
 * between the outbound port of a component sending requests with the inbound
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
 * <p>Created on : feb. 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class Core2CpuServiceConnector extends AbstractConnector implements
		Core2CpuI, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public Boolean acceptUpdateClockspeedRequest(Double clockspeed,
			Integer coreId) throws Exception {
		return ((Core2CpuI) this.offering).acceptUpdateClockspeedRequest(
				clockspeed, coreId);
	}

}
