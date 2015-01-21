package fr.upmc.colins.farm3.actuator;

import fr.upmc.colins.farm3.core.ResponseArrivalI;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;

public class ActuatorResponseArrivalInboundPort 
extends		AbstractInboundPort
implements		ResponseArrivalI
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
	public				ActuatorResponseArrivalInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ResponseArrivalI.class, owner) ;

		assert	uri != null && owner != null ;
		assert	owner.isOfferedInterface(ResponseArrivalI.class) ;
	}

	@Override
	public void acceptResponse(Response resp) throws Exception {
		final Actuator sp = (Actuator) this.owner ;
		final Response fresp = resp ;
		sp.handleRequestAsync(
				new ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						sp.responseArrivalEvent(fresp);
						return null;
					}
				}) ;
	}


}
