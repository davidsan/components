package fr.upmc.colins.farm3.actuator;

import java.text.MessageFormat;
import java.util.ArrayList;

import fr.upmc.colins.farm3.connectors.ActuatorControlCpuConnector;
import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.colins.farm3.core.ResponseArrivalI;
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
	
	/** inbound ports for each cores (to obtain the response) 				*/
	protected ActuatorResponseArrivalInboundPort respAip;
	

	
	
	/** list of the used uris of the core request arrival inbound port */
	 protected ArrayList<String> actuatorRequestGeneratorOutboundPortUris;
	
	 /** list of the used uris of the core request arrival inbound port */
	 protected ArrayList<String> usedCoreRequestArrivalInboundPortUris;
	

		/** control outbound port to each port of cores							*/
	 protected ArrayList<ActuatorControlCoreOutboundPort> accops;
	
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
		 ArrayList<String> outboundCoreControlPortURIs
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
		accops=new ArrayList<ActuatorControlCoreOutboundPort>();
		// outbound port for control (into a core)
		this.addRequiredInterface(ControlRequestArrivalI.class);
		for (int i = 0; i < outboundCoreControlPortURIs.size(); i++) {
			
			String outboundCoreControlPortURI = outboundCoreControlPortURIs.get(i);
			 ActuatorControlCoreOutboundPort acco = new ActuatorControlCoreOutboundPort(outboundCoreControlPortURI, this);
			 this.accops.add(acco);
			 this.addPort(acco);
			 if (AbstractCVM.isDistributed) {
				 acco.publishPort();
			 }
			 else {
				 acco.localPublishPort();
			 }
			 acco.doConnection(outboundCoreControlPortURIs.get(i),
					 ActuatorControlCpuConnector.class.getCanonicalName());
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
		for(int i=0;i<accops.size();i++){
			try {
				accops.get(i).updateClockSpeed(new Double(2.0));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
