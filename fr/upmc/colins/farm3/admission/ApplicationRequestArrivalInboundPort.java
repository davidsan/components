package fr.upmc.colins.farm3.admission;

import fr.upmc.colins.farm3.objects.Application;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>ApplicationRequestArrivalInboundPort</code> implements the inbound port
 * for a component receiving application from other components.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>ApplicationRequestArrivalI</code> interface as offered
 * and upon a call, passes it to the owner component that must also implement
 * the method <code>acceptApplication</code>.
 * 
 * <p>Created on : december 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class ApplicationRequestArrivalInboundPort extends AbstractInboundPort
		implements ApplicationRequestArrivalI {

	private static final long serialVersionUID = 1L;

	public ApplicationRequestArrivalInboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, ApplicationRequestArrivalI.class, owner);

		assert uri != null && owner != null;
		assert owner.isOfferedInterface(ApplicationRequestArrivalI.class);
	}

	@Override
	public String acceptApplication(Application a) throws Exception {
		AdmissionControl c = (AdmissionControl) this.owner;
		return c.acceptApplication(a);
	}
}
