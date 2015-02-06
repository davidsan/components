package fr.upmc.colins.farm3.admission;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.colins.farm3.actuator.dynamic.DynamicActuator;
import fr.upmc.colins.farm3.connectors.ControlRequestServiceConnector;
import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.colins.farm3.cpu.ControlRequestGeneratorOutboundPort;
import fr.upmc.colins.farm3.dispatcher.dynamic.DynamicRequestDispatcher;
import fr.upmc.colins.farm3.objects.Application;
import fr.upmc.colins.farm3.vm.dynamic.DynamicVM;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreationOutboundPort;
import fr.upmc.components.cvm.pre.dcc.DynamicallyConnectableComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;

/**
 * The class <code>AdmissionControl</code> implements a component that simulate
 * an admission control. 
 * 
 * <p><strong>Description</strong></p>
 * An admission control take care of serving application
 * upload from a consumer's JVM (eg. from the request generator). 
 * When uploading an application the admission control allocate a predefined number
 * of virtual machines and a dedicated request dispatcher. The admission control then
 * return the uri of the request dispatcher.
 *  
 * <p>Created on : december 2014</p>
 * 
 * @author	Colins-Alasca
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class AdmissionControl extends AbstractComponent {


	/** log constant	 													*/
	private static final String logId = "[ AdmControl ]";
	
	/**	total number of cores												*/
	protected Long nrofCores;
	/**	number of cores allocated per virtual machine						*/
	protected int nrofCoresPerVM;
	/**	number of virtual machines allocated per request dispatcher			*/
	protected int nrofVMPerDispatcher;
	
	/** inbound port to be connected to the request generator 				*/
	protected ApplicationRequestArrivalInboundPort applicationRequestArrivalInboundPort;
	/** outbound ports for each cpu							 				*/
	protected List<ControlRequestGeneratorOutboundPort> controlRequestGeneratorOutboundPorts;

	/** prefix uri of the request arrival inbound port of instanciated vm	*/
	protected static final String VM_RAIP_PREFIX = "vm-raip-";
	/** prefix uri of the request generator outbound port to each core		*/
	protected static final String VM_RGOP_PREFIX = "vm-rgop-";
	/** count of virtual machines instanciated				 				*/
	protected int virtualMachineCount ;
	
	/** prefix uri of the request arrival inbound port of the dispatchers	*/
	protected static final String RD_RAIP_PREFIX = "rd-raip-";
	/** prefix uri of the request generator outbound port to each vm		*/
	protected static final String RD_RGOP_PREFIX = "rd-rgop-";
	
	/** prefix uri of the request generator outbound port to each core 		*/
	protected static final String AT_RGOP_PREFIX = "at-rgop-";

	/** count of request dispatchers instanciated				 			*/
	protected int requestDispatcherCount ;
	
	/** rate for the an app (revoir le commmentaire)						*/
	protected double wantedRate;
	
	/** list of the uris of the core request arrival inbound port 			*/
	protected List<String> coreRequestArrivalInboundPortUris;
	/** list of the used uris of the core request arrival inbound port		*/
	protected List<String> usedCoreRequestArrivalInboundPortUris;
	
	/** dynamic component creation outbound port to the provider's JVM		*/
	protected DynamicComponentCreationOutboundPort portToProviderJVM;

	/** list of the uris of the control request generator outbound port 	*/
	protected List<ControlRequestGeneratorOutboundPort> crgops;




	/**
	 * Constructor
	 * 
	 * @param nrofCores
	 * @param nrofCoresPerVM,
	 * @param nrofVMPerDispatcher
	 * @param wantedRate
	 * @param outboundPortUri
	 * @param inboundPortUri
	 * @param coreRequestArrivalInboundPortUris 
	 * @param isDistributed
	 * @throws Exception
	 */
	public AdmissionControl(
			Long nrofCores, 
			Integer nrofCoresPerVM,
			Integer nrofVMPerDispatcher,
			double wantedRate,
			String outboundPortUri,
			String inboundPortUri, 
			ArrayList<String> coreRequestArrivalInboundPortUris,
			ArrayList<String> coreControlRequestArrivalInboundPortUris
			) throws Exception 
	{
		super(true, true);

		this.nrofCores = nrofCores;		
		this.nrofCoresPerVM = nrofCoresPerVM;
		this.nrofVMPerDispatcher = nrofVMPerDispatcher;
		this.controlRequestGeneratorOutboundPorts = new ArrayList<ControlRequestGeneratorOutboundPort>();
		this.wantedRate = wantedRate;
		
		this.crgops = new ArrayList<ControlRequestGeneratorOutboundPort>();
		// this is for the outbounds port towards each cpu (managed by the
		// admission control)
		this.addRequiredInterface(ControlRequestArrivalI.class);
		for (int i = 0; i < nrofCores; i++) {
			ControlRequestGeneratorOutboundPort crgop = new ControlRequestGeneratorOutboundPort(
					outboundPortUri + i, this);
			this.addPort(crgop);
			this.crgops.add(crgop);
			if (AbstractCVM.isDistributed) {
				crgop.publishPort();
			} else {
				crgop.localPublishPort();
			}
			controlRequestGeneratorOutboundPorts.add(crgop);
			crgop.doConnection(coreControlRequestArrivalInboundPortUris.get(i),
					ControlRequestServiceConnector.class.getCanonicalName());					
		}

		this.addOfferedInterface(ApplicationRequestArrivalI.class);
		this.applicationRequestArrivalInboundPort = new ApplicationRequestArrivalInboundPort(inboundPortUri,
				this);
		this.addPort(this.applicationRequestArrivalInboundPort);
		if (AbstractCVM.isDistributed) {
			this.applicationRequestArrivalInboundPort.publishPort();
		} else {
			this.applicationRequestArrivalInboundPort.localPublishPort();
		}
		
		this.virtualMachineCount = 0;
		this.requestDispatcherCount = 0;
		
		this.coreRequestArrivalInboundPortUris = coreRequestArrivalInboundPortUris;
		this.usedCoreRequestArrivalInboundPortUris = new ArrayList<>();
		
		// for the dynamic stuff below
		this.addRequiredInterface(DynamicComponentCreationI.class) ;
		this.addRequiredInterface(DynamicallyConnectableComponentI.class) ;
		
		System.out.println(logId + " Admission control created");
	}
	
	String acceptApplication(Application a) throws Exception {
		
		System.out.println(logId + " Begin creation of application "
				+ a.getUri());
		
		Integer requestDispatcherId = requestDispatcherCount++;
		ArrayList<String> rdRequestGeneratorOutboundPortUris = new ArrayList<>();
		for (int i = 0; i < nrofVMPerDispatcher; i++) {
			rdRequestGeneratorOutboundPortUris.add(RD_RGOP_PREFIX + requestDispatcherId + i);
		}

		ArrayList<String> vmRequestArrivalInboundPortUris = new ArrayList<>();
		for (int i = 0; i < nrofVMPerDispatcher; i++) {	
			// build the vm
			// FIXME: select only available cores (by pulling their status)
			// TODO: select from cpu instead of cores
			Integer virtualMachineId = virtualMachineCount++;
			ArrayList<String> vmRequestGeneratorOutboundPortUris = new ArrayList<>();
			ArrayList<String> assignedCoreRequestArrivalInboundPortUris = new ArrayList<>();
			for (int j = 0; j < nrofCoresPerVM; j++) {
				vmRequestGeneratorOutboundPortUris.add(VM_RGOP_PREFIX + virtualMachineId + j);
				if(this.coreRequestArrivalInboundPortUris.size() <= 0){
					// we assume the number of cores available is always positive
					// TODO: the case when we run out of free cores is not yet implemented
					// returning the empty string as an uri will throw an exception when
					// the request generator will try to connect to it.
					System.err.println("The cluster ran out of available cores, sorry.");
					return "";
				}
				String uri = this.coreRequestArrivalInboundPortUris.remove(0);
				assignedCoreRequestArrivalInboundPortUris.add(uri);
				this.usedCoreRequestArrivalInboundPortUris.add(uri);
			}
			
			this.portToProviderJVM.createComponent(
				DynamicVM.class.getCanonicalName(),
				new Object[]{ 
					virtualMachineId, 
					VM_RAIP_PREFIX + virtualMachineId, 
					vmRequestGeneratorOutboundPortUris, 
					assignedCoreRequestArrivalInboundPortUris
				}
			);
			vmRequestArrivalInboundPortUris.add(VM_RAIP_PREFIX + virtualMachineId);
			// the connection between the cores and the vm are done in the constructor
			// of the virtual machine
		}
		
		String actuatorResponseArrivalInboundPortUri = "actuator-response-raip-" + a.getUri() ;
		ArrayList<String> actuatorRequestGeneratorOutboundPortUris = new ArrayList();
		
		for(int i = 0; i< nrofCoresPerVM * nrofVMPerDispatcher; i++) {
			String uri = AT_RGOP_PREFIX + a.getUri() + "-" + i;
			actuatorRequestGeneratorOutboundPortUris.add(uri);
		}
		
		// build the actuator
		this.portToProviderJVM.createComponent(
				DynamicActuator.class.getCanonicalName(),
				new Object[]{ 
					a.getUri(),
					wantedRate,
					actuatorResponseArrivalInboundPortUri,
					actuatorRequestGeneratorOutboundPortUris,
					this.usedCoreRequestArrivalInboundPortUris
				}
			);
		
		// build the request dispatcher
		this.portToProviderJVM.createComponent(
				DynamicRequestDispatcher.class.getCanonicalName(),
				new Object[]{ 
					a.getUri(),
					RD_RAIP_PREFIX + requestDispatcherId,
					rdRequestGeneratorOutboundPortUris,
					vmRequestArrivalInboundPortUris,
					a.getMeanNrofInstructions(),
					a.getStandardDeviation(),
					actuatorResponseArrivalInboundPortUri
				}
			);
		System.out.println(logId + " End creation of application " + a.getUri());
		System.out.println(logId + " Deployed application " + a.getUri() + " is available from " + RD_RAIP_PREFIX + requestDispatcherId);

		return RD_RAIP_PREFIX + requestDispatcherId;
	}

	/**
	 * @see fr.upmc.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		try {
			this.portToProviderJVM =
								new DynamicComponentCreationOutboundPort(this) ;
			this.portToProviderJVM.localPublishPort() ;
			this.addPort(this.portToProviderJVM) ;
			String jvmURI = (AbstractCVM.isDistributed) ? "provider" : "";
			this.portToProviderJVM.doConnection(
					jvmURI +
					AbstractCVM.DYNAMIC_COMPONENT_CREATOR_INBOUNDPORT_URI,
					DynamicComponentCreationConnector.class.getCanonicalName()) ;
		} catch (Exception e) {
			e.printStackTrace() ;
			throw new ComponentStartException() ;
		}

		super.start() ;
	}
	
	
	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void 		shutdown() throws ComponentShutdownException {
		try {
			if (this.portToProviderJVM.connected()) {
				this.portToProviderJVM.doDisconnection() ;
			}
			for (ControlRequestGeneratorOutboundPort crgop : this.crgops) {
				if(crgop.connected()){
					crgop.doDisconnection();
				}
			}
			
		} catch (Exception e) {
			throw new ComponentShutdownException() ;
		}		
		super.shutdown();
	}
	
	/**
	 * Build a virtual machine associated with default number of cores and return its inbound port
	 * @return the request arrival inbound port of the VM
	 * @throws Exception
	 */
	public String buildVirtualMachine() throws Exception{
		// build the vm
		// FIXME: select only available cores (by pulling their status)
		// TODO: select from cpu instead of cores
		Integer virtualMachineId = virtualMachineCount++;
		ArrayList<String> vmRequestGeneratorOutboundPortUris = new ArrayList<>();
		ArrayList<String> assignedCoreRequestArrivalInboundPortUris = new ArrayList<>();
		for (int j = 0; j < nrofCoresPerVM; j++) {
			vmRequestGeneratorOutboundPortUris.add(VM_RGOP_PREFIX + virtualMachineId + j);
			if(this.coreRequestArrivalInboundPortUris.size() <= 0 && j == 0){
				// we assume the number of cores available is always positive
				// TODO: the case when we run out of free cores is not yet implemented
				// returning the empty string as an uri will throw an exception when
				// the request generator will try to connect to it.
				System.err.println("The cluster ran out of available cores, sorry.");
				throw new Exception("out of cores");
			}
			String uri = this.coreRequestArrivalInboundPortUris.remove(0);
			assignedCoreRequestArrivalInboundPortUris.add(uri);
			this.usedCoreRequestArrivalInboundPortUris.add(uri);
		}
		
		this.portToProviderJVM.createComponent(
				DynamicVM.class.getCanonicalName(),
				new Object[]{ 
					virtualMachineId, 
					VM_RAIP_PREFIX + virtualMachineId, 
					vmRequestGeneratorOutboundPortUris,
					assignedCoreRequestArrivalInboundPortUris
					}
				);
		
		return VM_RAIP_PREFIX + virtualMachineId;
	}

}
