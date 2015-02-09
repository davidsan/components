package fr.upmc.colins.farm3.core;

import java.text.MessageFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import fr.upmc.colins.farm3.VerboseSettings;
import fr.upmc.colins.farm3.connectors.Core2CpuServiceConnector;
import fr.upmc.colins.farm3.connectors.ResponseServiceConnector;
import fr.upmc.colins.farm3.objects.Request;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.colins.farm3.utils.TimeProcessing;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.ports.PortI;

/**
 * The class <code>Core</code> implements a component that simulate
 * a central processing unit's core.
 *
 * <p><strong>Description</strong></p>
 * 
 * The core component receives requests to be serviced through an
 * inbound port implementing the interface <code>RequestArrivalI</code>.  The
 * discrete-event simulation is based upon three kinds of events:
 * 
 * <ol>
 * <li>request arrival, upon which the request is queued, and then if the
 *   server is idle, a begin request processing event is immediately executed;
 *   </li>
 * <li>begin request processing, upon which a end request processing event is
 *   scheduled after a delay given by the request processing time; and,</li>
 * <li>end request processing, upon which if the queue is not empty a begin
 *   request processing event is immediately executed</li>
 * </ol>
 * 
 * Total service times (waiting + processing) of requests is accumulated in the
 * variable <code>totalServicingTime</code> while the number of serviced
 * requests is accumulated in the variable
 * <code>totalNumberOfServicedRequests</code>.  When the component is shut down,
 * any end processing event already scheduled is cancelled, and the component
 * outputs the average service time of the completely serviced requests.
 * 
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	serverIdle => (servicing == null && nextEndServicingTaskFuture == null)
 * invariant	!serverIdle => (servicing != null && nextEndServicingTaskFuture != null)
 * invariant	totalServicingTime >= 0 && totalNumberOfServicedRequests >= 0
 * </pre>
 * 
 * <p>Created on : 2 sept. 2014</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			Core
extends		AbstractComponent
{

	protected String logId;
    
	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------

	/** core identifier 													*/
	protected int 						coreId;
	/** true if the core is idle otherwise false.							*/
	protected boolean					coreIdle ;
	/** request currently being serviced, null if any.						*/
	protected Request					servicing ;
	/** queue of pending requests.											*/
	protected BlockingQueue<Request>	requestsQueue ;
	/** sum of the service time of all completed requets.					*/
	protected long						totalServicingTime ;
	/** total number of completely serviced requests.						*/
	protected int						totalNumberOfServicedRequests ;

	/** a future pointing to the next end servicing task.					*/
	protected Future<?>					nextEndServicingTaskFuture ;

	/** clock speed of the core 											*/
	// naive unit : 1.0 equals to 1000 instructions per second	 	
	protected double 					clockSpeed;
	/** maximum clock speed of the core 									*/
	protected double 					maxClockSpeed;
	
	// the two fields below are necessary to keep track of metadata on the 
	// currently served task. it let us reschedule the task with the new
	// clock speed
	/** time at which the current servicing sub-request has started			*/
	protected long 						timeStart;
	/** number of instructions of the request remaining to process			*/
	protected long 						remainingInstructions;
	
	/** outbound port of the core to send response to the virtual machine	*/
	protected CoreResponseGeneratorOutboundPort coreResponseGeneratorOutboundPort;

	/** uri of the core inbound port for request 							*/
	protected String 					inboundPortURI;

	/** uri of the core inbound control port								*/
	protected String 					controlInboundPortURI;

	/** outbound port to send request of clock update to the cpu 			*/
	protected Core2CpuOutboundPort 		core2CpuOutboundPort;

	/** uri of the cpu inbound port for control								*/
	private String 						cpuControlInboundPortURI;

	/**
	 * create a service provider.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param coreId				core identifier
	 * @param clockSpeed			clock speed of the core
	 * @param maxClockSpeed 		maxClockSpeed
	 * @param inboundPortURI		URI of the port used to received requests.
	 * @param controlInboundPortURI URI of the port used to received control request
	 * @param cpuControlInboundPortURI URI of the port used to send control request to the CPU
	 * @throws Exception
	 */
	public				Core(
		Integer coreId,
		Double clockSpeed,
		Double maxClockSpeed,
		String inboundPortURI,
		String controlInboundPortURI,
		String cpuControlInboundPortURI
		) throws Exception
	{
		super(true, true) ;
		this.logId = MessageFormat.format("[ Core {0}  ]", String.format("%04d", coreId));
		
		this.coreId = coreId;
		this.clockSpeed = clockSpeed;
		this.maxClockSpeed = maxClockSpeed;
		this.coreIdle = true ;
		this.servicing = null ;
		this.requestsQueue = new LinkedBlockingQueue<Request>() ;
		this.totalServicingTime = 0L ;
		this.totalNumberOfServicedRequests = 0 ;
		this.nextEndServicingTaskFuture = null ;
		this.timeStart = 0;
		this.remainingInstructions = 0;
		
		this.inboundPortURI = inboundPortURI;
		this.controlInboundPortURI = controlInboundPortURI;
		
		// inbound port for request arrival
		this.addOfferedInterface(RequestArrivalI.class) ;
		PortI p = new CoreRequestArrivalInboundPort(this.inboundPortURI, this) ;
		this.addPort(p) ;

		// inbound port for control request arrival
		this.addOfferedInterface(ControlRequestArrivalI.class) ;
		PortI controlPort = new CoreControlRequestArrivalInboundPort(this.controlInboundPortURI, this) ;
		this.addPort(controlPort) ;		

		this.addRequiredInterface(ResponseArrivalI.class);
		
		
		this.addRequiredInterface(Core2CpuI.class);
		this.core2CpuOutboundPort = new Core2CpuOutboundPort("core2cpu-op-"
				+ java.util.UUID.randomUUID(), this);
		this.addPort(core2CpuOutboundPort) ;		

		
		// the URI used here, doesn't really matter, it should just be unique
		this.coreResponseGeneratorOutboundPort = new CoreResponseGeneratorOutboundPort(
				"core-resp-rgop-" + java.util.UUID.randomUUID(), this);
		this.addPort(coreResponseGeneratorOutboundPort);
		if (AbstractCVM.isDistributed) {
			p.publishPort() ;
			controlPort.publishPort() ;
			coreResponseGeneratorOutboundPort.publishPort();
			core2CpuOutboundPort.publishPort();
		} else {
			p.localPublishPort() ;
			controlPort.localPublishPort();
			coreResponseGeneratorOutboundPort.localPublishPort();
			core2CpuOutboundPort.localPublishPort();
		}
		
		this.cpuControlInboundPortURI = cpuControlInboundPortURI;
		
		System.out.println(logId + " Core " + this.clockSpeed + " / "
				+ this.maxClockSpeed + " GHz (id " + coreId + ") created");

		assert	clockSpeed > 0;
		assert	!coreIdle || (servicing == null && nextEndServicingTaskFuture == null) ;
		assert	coreIdle || (servicing != null && nextEndServicingTaskFuture != null) ;
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
		if (this.nextEndServicingTaskFuture != null &&
							!(this.nextEndServicingTaskFuture.isCancelled() ||
							  this.nextEndServicingTaskFuture.isDone())) {
			this.nextEndServicingTaskFuture.cancel(true) ;
		}
		try {
			if (this.coreResponseGeneratorOutboundPort.connected()) {
				this.coreResponseGeneratorOutboundPort.doDisconnection();
			}
			if (this.core2CpuOutboundPort.connected()) {
				this.core2CpuOutboundPort.doDisconnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.shutdown() ;
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

		long t = System.currentTimeMillis() ;
		System.out.println(logId + " Accepting request       " + r + " at " +
												TimeProcessing.toString(t)) ;
		r.setArrivalTime(t) ;
		this.requestsQueue.add(r) ;
		if (!this.coreIdle) {
			System.out.println(logId + " Queueing request        " + r) ;
		} else {
			this.beginServicingEvent() ;
		}
	}

	
	public boolean		updateClockSpeed(Double clockSpeed) throws Exception
	{
		
		if (clockSpeed <= 0 || clockSpeed > maxClockSpeed) {
			return false;
		}

		// keep track of the old clock rate
		double oldClockSpeed = this.clockSpeed;
		if(VerboseSettings.VERBOSE_CORE)
			System.out.println(logId + " Updating clock speed : " + this.clockSpeed + " -> "
					+ clockSpeed);
		// update the clock rate
		this.clockSpeed = clockSpeed;
		if(VerboseSettings.VERBOSE_CORE)
			System.out.println(logId + " Clock speed updated");
		
		if (this.requestsQueue.isEmpty() && this.coreIdle) {
			// nothing to reschedule
			if(VerboseSettings.VERBOSE_CORE)
				System.out.println(logId + " No ongoing task, nothing to reschedule");
		}else{
			// reschedule currently served task
			if(VerboseSettings.VERBOSE_CORE)
				System.out.println(logId + " Reschedule currently served task");
			// time capture
			long timeCancel = System.currentTimeMillis();
			long timeServed = timeCancel - timeStart;
			// suspend the servicing task using his future
			this.nextEndServicingTaskFuture.cancel(true);
			// compute remaining number of instructions to be processed
			this.remainingInstructions =  Math.max(0, this.remainingInstructions - ((long)(oldClockSpeed * timeServed)));
			if(VerboseSettings.VERBOSE_CORE)
				System.out.println(logId + " Remaining instructions : "
						+ this.remainingInstructions);
			// reschedule the servicing task using the new clock speed of the core
			scheduleServicing();
		}

		assert this.clockSpeed > 0;
		return true;
	}
	
	
	public boolean		updateClockSpeedPlease(Double clockSpeed) throws Exception
	{
		// if the core was not connected to the cpu yet, we connect it
		if(!this.core2CpuOutboundPort.connected()){
			this.core2CpuOutboundPort.doConnection(this.cpuControlInboundPortURI, Core2CpuServiceConnector.class.getCanonicalName());
		}
		
		// clockspeed correction
		if (clockSpeed <= 0) {
			clockSpeed = 0.1;
		}	
		if (clockSpeed > this.maxClockSpeed) {
			clockSpeed = maxClockSpeed;
		}

		if(VerboseSettings.VERBOSE_CORE)
			System.out.println(logId + " Ask the cpu to update his clock speed");
		Boolean updated = this.core2CpuOutboundPort.acceptUpdateClockspeedRequest(clockSpeed, this.coreId);
		
		if(VerboseSettings.VERBOSE_CORE){
			if(updated){
				System.out.println(logId + " Core clockspeed's update request granted");
			}else{
				System.out.println(logId + " Core clockspeed's update request revoked");
			}
		}
		assert this.clockSpeed > 0;
		return true;
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
		
		this.remainingInstructions = servicing.getNrofInstructions();
		if(VerboseSettings.VERBOSE_CORE)
			System.out.println(logId + " Begin servicing request " + this.servicing + " at "
							+ TimeProcessing.toString(System.currentTimeMillis())) ;
		scheduleServicing();
	}
	
	/**
	 * schedule the request contained in the servicing field as a task.
	 * called by beginServicingEvent and also bvy 
	 */
	private void		scheduleServicing(){
		this.timeStart = System.currentTimeMillis();
		
		this.coreIdle = false ;
		final Core fcore = (Core) this ;
		final long fnrofInst = this.remainingInstructions;
		final double fclockSpeed = this.clockSpeed;
		// generate the processing time using the clockSpeed
		final long processingTime = (long) (fnrofInst / fclockSpeed);
		final ComponentTask task = new ComponentTask() {
			@Override
			public void run() {
				try {
					fcore.endServicingEvent() ;
				} catch (Exception e) {
					e.printStackTrace() ;
				}
			}};
		this.nextEndServicingTaskFuture = this.scheduleTask(task ,
				processingTime, TimeUnit.MILLISECONDS) ;
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
		
		Response response = new Response(this.servicing.getUri());
		response.setDuration(new Double(st));
		
		if(this.coreResponseGeneratorOutboundPort.connected()){
			this.coreResponseGeneratorOutboundPort.acceptResponse(response);
		}
		if(VerboseSettings.VERBOSE_CORE)
			System.out.println(logId + " End servicing request   " + this.servicing +
									" at " + TimeProcessing.toString(t) +
									" with service time " + st) ;
		this.totalServicingTime += st ;
		this.totalNumberOfServicedRequests++ ;
		
		this.timeStart = 0;
		this.remainingInstructions = 0;
		
		if (this.requestsQueue.isEmpty()) {
			this.servicing = null ;
			this.coreIdle = true ;
			this.nextEndServicingTaskFuture = null ;
		} else {
			this.beginServicingEvent() ;
		}
	}

	/**
	 * Connect the virtual machine for response connection
	 * @param furi	uri of outbound port of the virtual machine
	 * @throws Exception
	 */
	public void connectResponseConnection(String furi) throws Exception 
	{
		if(VerboseSettings.VERBOSE_CORE)
			System.out.println(logId + " Connect the response connection to the VM");
		// do connection
		if (!this.coreResponseGeneratorOutboundPort.connected()) {
			this.coreResponseGeneratorOutboundPort
					.doConnection(furi, ResponseServiceConnector.class.getCanonicalName());
		}
	}
	

	/** getters */
	
	public String getInboundPortURI() {
		return inboundPortURI;
	}

	public String getControlInboundPortURI() {
		return controlInboundPortURI;
	}

	public double getClockSpeed() {
		return clockSpeed;
	}
	
}
