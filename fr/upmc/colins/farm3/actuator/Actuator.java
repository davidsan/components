package fr.upmc.colins.farm3.actuator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import fr.upmc.colins.farm3.connectors.RequestServiceConnector;
import fr.upmc.colins.farm3.core.RequestArrivalI;
import fr.upmc.colins.farm3.core.ResponseArrivalI;
import fr.upmc.colins.farm3.generator.RequestGeneratorOutboundPort;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;

/**
 * The class <code>Actuator</code> implements an actuator
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * An actuator is a component that will forward received requests to
 * its dedicated virtual machine. 
 * 
 * <p>
 * Created on : jan. 2015
 * </p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class			Actuator
extends		AbstractComponent
{

	protected String logId;
    
	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------


	/** identifier 															*/
	protected final Integer id;
	
	/** the rate that the center want 										*/
	protected Double wantedRate; 
	
	/** inbound ports for each cores (to obtain the response) 				*/
	protected ActuatorResponseArrivalInboundPort respAip;
	
	/** list of the used uris of the core request arrival inbound port		*/
	protected List<String> actuatorRequestGeneratorOutboundPortUris;
	
	/** list of the used uris of the core request arrival inbound port		*/
	protected List<String> usedCoreRequestArrivalInboundPortUris;
	
	/** outbound ports to the core											*/
	protected Queue<RequestGeneratorOutboundPort> rgops;
	
	/** list of the used uris of the core control inbound port		*/
	protected List<String> usedCoreControlInboundPortUris;
	
	/** outbound ports to the core Control										*/
	protected Queue<ActuatorControlCoreOutboundPort> accops;
;
	
	
	/**
	 * create an actuator
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param id					
	 * 				identifier of the actuator
	 * @throws Exception
	 */
	public				Actuator(
		Integer id,
		Double wantedRate,
		String actuatorResponseArrivalInboundPortUri,
		ArrayList<String> outboundPortURIs,
		ArrayList<String> usedCoreRequestArrivalInboundPortUris,
		ArrayList<String> usedCoreControlInboundPortUris,
		ArrayList<String> outboundCoreControlPortURIs
		) throws Exception
	{
		super(true, true) ;

		assert id != null;
		
		this.logId = MessageFormat.format("[ ACTT {0}  ]", String.format("%04d", id));
		this.id = id ;
		this.wantedRate = wantedRate;
		
		this.rgops = new LinkedList<>();
		this.accops=new LinkedList<>();
		// inbound port for request arrival
		this.addOfferedInterface(ResponseArrivalI.class) ;
		this.respAip = new ActuatorResponseArrivalInboundPort(actuatorResponseArrivalInboundPortUri, this) ;
		this.usedCoreRequestArrivalInboundPortUris = usedCoreRequestArrivalInboundPortUris;
		this.usedCoreControlInboundPortUris=usedCoreControlInboundPortUris;
		this.addPort(this.respAip) ;
		if (AbstractCVM.isDistributed) {
			this.respAip.publishPort() ;
		} else {
			this.respAip.localPublishPort() ;
		}
		

		// interface is added once.
		this.addRequiredInterface(RequestArrivalI.class) ;
		this.addOfferedInterface(ActuatorControlCoreOutboundPort.class) ;
		// connect outbound port to the core
		for (int i = 0; i < outboundPortURIs.size(); i++) {
			String outboundPortURI = outboundPortURIs.get(i);
			// outbound port for request departure (into a core)
			RequestGeneratorOutboundPort rgop = new RequestGeneratorOutboundPort(outboundPortURI, this);
			this.rgops.add(rgop);
			this.addPort(rgop) ;
			if (AbstractCVM.isDistributed) {
				rgop.publishPort() ;
			} else {
				rgop.localPublishPort();
			}
		
			rgop.doConnection(usedCoreRequestArrivalInboundPortUris.get(i),
					RequestServiceConnector.class.getCanonicalName());
			System.out.println(logId + " Connect the actuator to a core (via "
					+ rgop.getPortURI() + ")");
			
			
			// outbound port for control (into a core)
			String outboundCoreControlPortURI = outboundCoreControlPortURIs.get(i);
			ActuatorControlCoreOutboundPort acco = new ActuatorControlCoreOutboundPort(outboundCoreControlPortURI, this);
			System.out.println(acco.getClass().getName());
			this.accops.add(acco);
			this.addPort(acco);
			if (AbstractCVM.isDistributed) {
				acco.publishPort();
			}
			else {
				acco.localPublishPort();
			}
			acco.doConnection(usedCoreControlInboundPortUris.get(i),
					RequestServiceConnector.class.getCanonicalName());
			System.out.println(logId + " Connect the actuator to a core control port (via "
					+ acco.getPortURI() + ")");
					
		}		

		System.out.println(logId + " Actuator (id " + id + ") created for app " + id) ;
		assert	id != null;
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * shut down the component after canceling any pending end request
	 * processing task, and output the average service time of the completed
	 * service requests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		// disconnect rgops
		try {
			for (RequestGeneratorOutboundPort rgop : this.rgops) {
				if (rgop.connected()) {
					rgop.doDisconnection();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal services
	// -------------------------------------------------------------------------
	

	/**
	 * update the mean time of request processing (from the virtual machine)
	 * TODO: and forward the mean of mean time to a controller
	 * @param response the received response
	 */
	public void 			responseArrivalEvent(Response response) {	
		System.out.println(logId + " Received a new mean time from his request dispatcher of " + response.getDuration());
		try {
			ActuatorControlCoreOutboundPort tmp = accops.poll();
			if(tmp!=null){
			tmp.updateClockSpeed(0.4);
			accops.add(tmp);
			System.out.println(accops.poll().getOwner().getClass().getName());
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
