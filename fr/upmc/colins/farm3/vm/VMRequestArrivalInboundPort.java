package fr.upmc.colins.farm3.vm;

import fr.upmc.colins.farm3.core.RequestArrivalI;
import fr.upmc.colins.farm3.objects.Request;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>VMRequestArrivalInboundPort</code> implements the inbound port
 * for a component receiving requests from other components.
 *
 * <p><strong>Description</strong></p>
 * 
 * The port implements the <code>RequestArrivalI</code> interface as offered
 * and upon a call, passes it to the owner component that must also implement
 * the method <code>requestArrivalEvent</code>.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2 sept. 2014</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			VMRequestArrivalInboundPort
extends		AbstractInboundPort
implements	RequestArrivalI
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
	public				VMRequestArrivalInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestArrivalI.class, owner) ;

		assert	uri != null && owner != null ;
		assert	owner.isOfferedInterface(RequestArrivalI.class) ;
	}

	/**
	 * @see fr.upmc.alasca.ssqueue.interfaces.RequestArrivalI#acceptRequest(fr.upmc.alasca.ssqueue.objects.Request)
	 */
	@Override
	public void			acceptRequest(Request c) throws Exception
	{
		final VM sp = (VM) this.owner ;
		final Request fc = c ;
		sp.handleRequestAsync(
				new ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						sp.requestArrivalEvent(fc);
						return null;
					}
				}) ;
	}

	@Override
	public void connectResponseConnection(String responseArrivalInboundPortUri) throws Exception
	{
		final VM sp = (VM) this.owner ;
		final String furi = responseArrivalInboundPortUri ;
		sp.handleRequestAsync(
				new ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						sp.connectResponseConnection(furi);
						return null;
					}
				}) ;
	}


}
