package fr.upmc.colins.farm3.dispatcher;

import fr.upmc.colins.farm3.core.ResponseArrivalI;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

public class RDResponseGeneratorOutboundPort 
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
	public				RDResponseGeneratorOutboundPort(
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