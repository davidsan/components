package fr.upmc.colins.farm3.vm;

import fr.upmc.colins.farm3.core.ResponseArrivalI;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>VMResponseArrivalInboundPort</code> implements the inbound port
 * for a component receiving response from other components.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>ResponseArrivalI</code> interface as offered
 * and upon a call, passes it to the owner component that must also implement
 * the method <code>responseArrivalEvent</code>.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : jan 2015</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			VMResponseArrivalInboundPort
extends		AbstractInboundPort
implements	ResponseArrivalI
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
	public				VMResponseArrivalInboundPort(
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
		final VM sp = (VM) this.owner ;
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
