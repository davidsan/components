package fr.upmc.colins.farm3.generator;

import fr.upmc.colins.farm3.admission.ApplicationRequestArrivalI;
import fr.upmc.colins.farm3.objects.Application;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>ApplicationRequestGeneratorOutboundPort</code> implements the outbound
 * port for a component sending applications to another component.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>ApplicationRequestArrivalI</code> interface as required
 * and upon a call, passes it to the connector that must also implement the same
 * interface.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : december 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class ApplicationRequestGeneratorOutboundPort extends
		AbstractOutboundPort implements ApplicationRequestArrivalI {
	public ApplicationRequestGeneratorOutboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, ApplicationRequestArrivalI.class, owner);

		assert uri != null;
		assert owner.isRequiredInterface(ApplicationRequestArrivalI.class);
	}

	@Override
	public String acceptApplication(Application a) throws Exception {
		return ((ApplicationRequestArrivalI) this.connector).acceptApplication(a);
	}

}