package fr.upmc.colins.farm3.core;

import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>CoreResponseGeneratorOutboundPort</code> implements the outbound
 * port for a given component.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>ResponseArrivalI</code> interface as required
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
public class			CoreResponseGeneratorOutboundPort
extends		AbstractOutboundPort
implements	ResponseArrivalI
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
	public				CoreResponseGeneratorOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ResponseArrivalI.class, owner) ;

		assert	uri != null ;
		assert	owner.isRequiredInterface(ResponseArrivalI.class) ;
	}
  
	/**
	 * accept a response
	 */
	@Override
	public void acceptResponse(Response response) throws Exception {
		((ResponseArrivalI)this.connector).acceptResponse(response) ;
	}


}
