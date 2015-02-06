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
import fr.upmc.colins.farm3.objects.Request;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.colins.farm3.utils.TimeProcessing;
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
	
	/** control outbound port to each port of cores							*/
	
	
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
		ArrayList<String> usedCoreRequestArrivalInboundPortUris
		) throws Exception
	{
		super(true, true) ;

		assert id != null;
		
		this.logId = MessageFormat.format("[ ACTT {0}  ]", String.format("%04d", id));
		this.id = id ;
		this.wantedRate = wantedRate;
		
		this.rgops = new LinkedList<>();
		
		// inbound port for request arrival
		this.addOfferedInterface(ResponseArrivalI.class) ;
		this.respAip = new ActuatorResponseArrivalInboundPort(actuatorResponseArrivalInboundPortUri, this) ;
		this.usedCoreRequestArrivalInboundPortUris = usedCoreRequestArrivalInboundPortUris;
		
		this.addPort(this.respAip) ;
		if (AbstractCVM.isDistributed) {
			this.respAip.publishPort() ;
		} else {
			this.respAip.localPublishPort() ;
		}
		

		// interface is added once.
		this.addRequiredInterface(RequestArrivalI.class) ;
		
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
		// to fast slow down the core clock speed
		if (response.getDuration() < wantedRate) {
			Request request = new Request(-100, -1);
			try {
				System.out.println(logId + " Update core (slow)     "
						+ " at "
						+ TimeProcessing.toString(System.currentTimeMillis())) ;
				RequestGeneratorOutboundPort rgop = this.rgops.poll();		
				rgop.acceptRequest(request);
				this.rgops.add(rgop);
			} catch (Exception e) {
				e.printStackTrace();
			}
		// to slow up the core clock speed
		} else if(response.getDuration() > wantedRate) {
			Request request = new Request(-100, -2);
			try {
				System.out.println(logId + " Update core (fast)    "
						+ " at "
						+ TimeProcessing.toString(System.currentTimeMillis())) ;
				RequestGeneratorOutboundPort rgop = this.rgops.poll();		
				rgop.acceptRequest(request);
				this.rgops.add(rgop);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(logId + " Received a new mean time from his request dispatcher of " + response.getDuration());
		}

		
	}
}
