package fr.upmc.colins.farm3.actuator;

import java.text.MessageFormat;
import java.util.ArrayList;

import fr.upmc.colins.farm3.connectors.ControlRequestServiceConnector;
import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.colins.farm3.core.ResponseArrivalI;
import fr.upmc.colins.farm3.cpu.ControlRequestGeneratorOutboundPort;
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

	/** log constant	 													*/
	protected String logId;
    
	/** step value of frequency when changing the frequency					*/
	protected static final double BOOST_STEP = 0.6;

	/** goal time in milliseconds											*/
	protected static final int GOAL_TIME = 1000;

	
	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------


	/** identifier 															*/
	protected final Integer id;
	
	/** inbound ports for each cores (to obtain the response) 				*/
	protected ActuatorResponseArrivalInboundPort respAip;

	protected ArrayList<ControlRequestGeneratorOutboundPort> crgops;
	
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
		String actuatorResponseArrivalInboundPortUri,
		ArrayList<String> assignedCoreControlRequestArrivalInboundPortUris
		) throws Exception
	{
		super(true, true) ;

		assert id != null;
		
		this.logId = MessageFormat.format("[ ACTT {0}  ]", String.format("%04d", id));
		this.id = id ;
		
		// inbound port for request arrival
		this.addOfferedInterface(ResponseArrivalI.class) ;
		this.respAip = new ActuatorResponseArrivalInboundPort(actuatorResponseArrivalInboundPortUri, this) ;
		
		this.addPort(this.respAip) ;
		if (AbstractCVM.isDistributed) {
			this.respAip.publishPort() ;
		} else {
			this.respAip.localPublishPort() ;
		}
		
		this.addRequiredInterface(ControlRequestArrivalI.class);
		this.crgops = new ArrayList<>();
		// outbound port for control request to each cores
		for (int i = 0; i < assignedCoreControlRequestArrivalInboundPortUris.size(); i++) {
			ControlRequestGeneratorOutboundPort crgop = new ControlRequestGeneratorOutboundPort(
					"actuator-" + id + "-" + i, this);
			this.addPort(crgop);
			this.crgops.add(crgop);
			if (AbstractCVM.isDistributed) {
				crgop.publishPort();
			} else {
				crgop.localPublishPort();
			}
			crgop.doConnection(assignedCoreControlRequestArrivalInboundPortUris.get(i),
					ControlRequestServiceConnector.class.getCanonicalName());					
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
		try {
			for (ControlRequestGeneratorOutboundPort controlRequestGeneratorOutboundPort : crgops) {
				if (controlRequestGeneratorOutboundPort.connected()) {
					controlRequestGeneratorOutboundPort.doDisconnection();
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
	 * @throws Exception 
	 */
	public void 			responseArrivalEvent(Response response) throws Exception {	
		System.out.println(logId + " Received a new mean time from his request dispatcher of " + response.getDuration());
		if (response.getDuration() < GOAL_TIME) {
			for (ControlRequestGeneratorOutboundPort port : crgops) {
				port.updateClockSpeedPlease(port.getClockSpeed() + BOOST_STEP);
			}
		}
	}
}
