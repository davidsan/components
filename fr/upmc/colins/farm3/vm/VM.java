package fr.upmc.colins.farm3.vm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.upmc.colins.farm3.VerboseSettings;
import fr.upmc.colins.farm3.connectors.RequestServiceConnector;
import fr.upmc.colins.farm3.connectors.ResponseServiceConnector;
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
 * The class <code>VM</code> implements a virtual machine
 *
 * <p><strong>Description</strong></p>
 * A virtual machine is a component that will make its cores execute the request.
 * The request are received from the request dispatcher.
 * The virtual machine will chose the least recently used cores to execute the request.
 * 
 * 
 * <p>Created on : jan. 2015</p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class			VM
extends		AbstractComponent
{

	protected String logId;
    
	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------


	/** identifier 															*/
	protected final Integer 				id;
	/** true if the virtual machine is idle otherwise false.				*/
	protected boolean						vmIdle ;
	/** request currently being serviced, null if any.						*/
	protected Request						servicing ;
	/** queue of pending requests.											*/
	protected BlockingQueue<Request>		requestsQueue ;
	/** sum of the service time of all completed requets.					*/
	protected long							totalServicingTime ;
	/** total number of completely serviced requests.						*/
	protected int							totalNumberOfServicedRequests ;
	
	
	/** inbound port for the request generator								*/
	protected VMInboundPort 	raip;
	/** outbound ports to the core											*/
	protected Queue<RequestGeneratorOutboundPort> rgops;

	/** inbound ports for each cores (to obtain the response) 				*/
	protected ArrayList<VMResponseArrivalInboundPort> respAips;

	/** outbound port of the VM to send response to the request dispatcher	*/
	protected VMResponseGeneratorOutboundPort vmResponseGeneratorOutboundPort;

	/** this is true until the first request arrival event					*/
	protected boolean cold = true;


	
	/**
	 * create a virtual machine
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param id					
	 * 				identifier of the VM
	 * @param inboundPortURI		
	 * 				URI of the port used to received requests, linked to the 
	 * 				request dispatcher
	 * @param outboundPortURIs		
	 * 				URI of the ports used to send requests, linked to the core
	 * @param coreRequestArrivalInboundPortUris 
	 * 				URIs of the port to the core
	 * @throws Exception
	 */
	public				VM(
		Integer id,
		String inboundPortURI,
		ArrayList<String> outboundPortURIs,
		ArrayList<String> coreRequestArrivalInboundPortUris
		) throws Exception
	{
		super(true, true) ;

		assert id != null;
		assert inboundPortURI != null;
		assert outboundPortURIs != null;
		
		this.logId = MessageFormat.format("[   VM {0}  ]", String.format("%04d", id));
		this.id = id ;
		this.vmIdle = true ;
		this.servicing = null ;
		this.requestsQueue = new LinkedBlockingQueue<Request>() ;
		this.totalServicingTime = 0L ;
		this.totalNumberOfServicedRequests = 0 ;

		// inbound port for request arrival
		this.addOfferedInterface(RequestArrivalI.class) ;
		this.raip = new VMInboundPort(inboundPortURI, this) ;
		this.addPort(this.raip) ;
		if (AbstractCVM.isDistributed) {
			this.raip.publishPort() ;
		} else {
			this.raip.localPublishPort() ;
		}
		
		this.rgops = new LinkedList<>();
		this.respAips = new ArrayList<>();
		// interface is added once.
		this.addRequiredInterface(RequestArrivalI.class) ;
		// receive response from cores
		this.addOfferedInterface(ResponseArrivalI.class) ;
		
//		this.addOfferedInterface(VirtualMachineI.class) ;
		
		for (int i = 0; i < outboundPortURIs.size(); i++) {
			String outboundPortURI = outboundPortURIs.get(i);
			// outbound port for request departure (into a core)
			RequestGeneratorOutboundPort rgop = new RequestGeneratorOutboundPort(outboundPortURI, this);
			this.rgops.add(rgop) ;
			this.addPort(rgop) ;
			if (AbstractCVM.isDistributed) {
				rgop.publishPort() ;
			} else {
				rgop.localPublishPort();
			}
			rgop.doConnection(coreRequestArrivalInboundPortUris.get(i),
					RequestServiceConnector.class.getCanonicalName());

			// the URI used here, doesn't really matter, it should just be unique
			String vmResponseArrivalInboundPortUri = "vm-resp-raip-" + java.util.UUID.randomUUID();
			
			// create an inbound port for response from this core
			VMResponseArrivalInboundPort respAip = new VMResponseArrivalInboundPort(vmResponseArrivalInboundPortUri, this);
			this.respAips.add(respAip);
			this.addPort(respAip);
			if(AbstractCVM.isDistributed){
				respAip.publishPort();
			}else{
				respAip.localPublishPort();
			}
			
			System.out.println(logId + " Connect the virtual machine to the core (via "
					+ rgop.getPortURI() + ")");
	
		}
		
		// outbound port to send response to request dispatcher
		this.addRequiredInterface(ResponseArrivalI.class);
		this.vmResponseGeneratorOutboundPort = new VMResponseGeneratorOutboundPort(inboundPortURI + "response", this);
		this.addPort(vmResponseGeneratorOutboundPort);
		
		if(AbstractCVM.isDistributed){
			vmResponseGeneratorOutboundPort.publishPort();
		}else{
			vmResponseGeneratorOutboundPort.localPublishPort();
		}
		
		System.out.println(logId + " Virtual machine (id " + id + ") created") ;
		
		assert	id != null;
		assert	!vmIdle || (servicing == null) ;
		assert	vmIdle || (servicing != null) ;
		assert	totalServicingTime >= 0 && totalNumberOfServicedRequests >= 0 ;
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
			for (RequestGeneratorOutboundPort rgop : rgops) {
				if (rgop.connected()) {
					rgop.doDisconnection();
				}
			}
			if (this.vmResponseGeneratorOutboundPort.connected()) {
				this.vmResponseGeneratorOutboundPort.doDisconnection();
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
	 * process a request arrival event, queueing the request and the processing
	 * a begin sericing event if the server is currently idle.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r
	 * @throws Exception
	 */
	public void			requestArrivalEvent(Request r) throws Exception
	{
		assert	r != null ;

		// ask the core to do the connection to the inbound port of the VM (for response)
		if(cold){
			RequestGeneratorOutboundPort[] rgopsArray = rgops.toArray(new RequestGeneratorOutboundPort[0]);
			for (int i = 0; i < respAips.size(); i++) {
				VMResponseArrivalInboundPort vmRespAip = respAips.get(i);
				rgopsArray[i].connectResponseConnection(vmRespAip.getPortURI());
			}
			cold = false;
		}
		
		long t = System.currentTimeMillis() ;

		r.setArrivalTime(t) ;
		this.requestsQueue.add(r) ;
		if (!this.vmIdle) {
			if(VerboseSettings.VERBOSE_VM)
				System.out.println(logId + " Queueing request " + r) ;
		} else {
			this.beginServicingEvent() ;
		}
	}

	
	/**
	 * process a begin servicing event, e.g. schedule a end servicing event
	 * after a delay of the processing time of the request.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			beginServicingEvent()
	{
		this.servicing = this.requestsQueue.remove() ; 
		scheduleServicing(this.servicing);
	}
	
	/**
	 * schedule the request contained in the servicing field as a task.
	 * called by beginServicingEvent and also by endServicingEvent
	 * @param request the request to pass to a core
	 */
	private void		scheduleServicing(Request request){
		
		this.vmIdle = false ;
		try {

			if(VerboseSettings.VERBOSE_VM)
				System.out.println(logId + " Dispatching request     "
						+ this.servicing + " at "
						+ TimeProcessing.toString(System.currentTimeMillis())) ;
			RequestGeneratorOutboundPort rgop = this.rgops.poll();		
			rgop.acceptRequest(request);
			this.rgops.add(rgop);
			this.endServicingEvent() ;
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * process a end servicing event, e.g. update the statistics for the average
	 * service time, and then if the queue is not empty execute a begin
	 * servicing event immediately.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception
	 */
	public void			endServicingEvent() throws Exception
	{
		long t = System.currentTimeMillis() ;
		long st = t - this.servicing.getArrivalTime() ; 
		this.totalServicingTime += st ;
		this.totalNumberOfServicedRequests++ ;
				
		if (this.requestsQueue.isEmpty()) {
			this.servicing = null ;
			this.vmIdle = true ;
		} else {
			this.beginServicingEvent() ;
		}
	}

	/**
	 * update the mean time of request processing (inside the virtual machine)
	 * and forward the new mean time to the request dispatcher
	 * @param response the received response
	 */
	public void 			responseArrivalEvent(Response response) throws Exception {
		//		System.out.println(logId + " Received a response from core");
		// forward the response to the request dispatcher

		if (this.vmResponseGeneratorOutboundPort.connected()) {
			this.vmResponseGeneratorOutboundPort.acceptResponse(response);
		}
	}
	
	/**
	 * Connect the virtual machine for response connection
	 * @param furi	uri of outbound port of the virtual machine
	 * @throws Exception
	 */
	public void connectResponseConnection(String furi) throws Exception 
	{

		if(VerboseSettings.VERBOSE_VM)
			System.out.println(logId + " Connect the response connection to the request dispatcher");
		// do connection
		if (!this.vmResponseGeneratorOutboundPort.connected()) {
			this.vmResponseGeneratorOutboundPort
					.doConnection(furi, ResponseServiceConnector.class.getCanonicalName());
		}
	}
}
