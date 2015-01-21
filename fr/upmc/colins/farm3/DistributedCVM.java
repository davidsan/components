package fr.upmc.colins.farm3;

import java.util.ArrayList;

import fr.upmc.colins.farm3.admission.AdmissionControl;
import fr.upmc.colins.farm3.connectors.ApplicationRequestServiceConnector;
import fr.upmc.colins.farm3.cpu.Cpu;
import fr.upmc.colins.farm3.generator.RequestGenerator;
import fr.upmc.colins.farm3.utils.TimeProcessing;
import fr.upmc.components.ComponentI.ComponentTask;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.ports.PortI;

/**
 * The class <code>DistributedCVM</code> contains the main used the launch the
 * compute cluster simulation. This main use multiple JVM.
 * 
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * This main supports the following components : core, admission control,
 * request dispatcher, virtual machine.
 * 
 * <p>
 * How to launch : https://i.imgur.com/ruuZ9ws.png
 * </p>
 * 
 * <p>
 * Created on : jan. 2015
 * </p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */

public class DistributedCVM extends AbstractDistributedCVM {
	
	// JVM URIs
	protected static String PROVIDER_JVM_URI = "provider";
	protected static String CONSUMER_JVM_URI = "consumer";
	
	// Settings
	/** the main sleep's duration										*/
	protected static final long 	MAIN_SLEEPING_DURATION = 5000L;
	/** the default clock speed											*/
	protected static final Double CLOCK_SPEED = 1.0;
	/** the maximum clock speed											*/
	protected static final Double MAX_CLOCK_SPEED = 2.0;
	/** the number of cpu in the cluster								*/
	protected static final Long NROF_CPU = 4L;
	/** the number of cores	in the cluster								*/
	protected static final Long NROF_CORES_PER_CPU = 2L;
	/** the number of applications to be submitted by the consumer		*/
	protected static final Long NROF_APPS = 2L;
	/** the number of cores allocated per virtual machines				*/
	protected static final int 		NROF_CORES_PER_VM = 2;
	/** the number of virtual machines allocated per dispatcher			*/
	protected static final int 		NROF_VM_PER_DISPATCHER = 2;
	/** the mean inter arrival time										*/
	protected static final double 	MEAN_INTER_ARRIVAL_TIME = 1000.0;
	/** the standard deviation 											*/
	protected static final double 	STANDARD_DEVIATION = 100.0;
	/** the mean number of instructions 								*/
	protected static final double 	MEAN_NROF_INSTRUCTIONS = 1000.0;

	// Components' URIs
	protected static final String RG_ARGOP = "rg-argop";
	protected static final String RG_RGOP_PREFIX = "rg-rgop-";

	protected static final String AC_ARAIP = "ac-araip";
	protected static final String AC_CRGOP_PREFIX = "ac-crgop-";

	protected static final String CPU_CRAIP_PREFIX = "cpu-craip-";
	
	/** provider */
	protected AdmissionControl mAdmissionControl;
	protected ArrayList<Cpu> mCpus;

	/** consumer */
	protected RequestGenerator mRequestGenerator;

	public DistributedCVM(String[] args) throws Exception {
		super(args);
	}

	/**
	 * do some initialisation before any can go on.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.cvm.AbstractDistributedCVM#initialise()
	 */
	@Override
	public void initialise() throws Exception {
		super.initialise();
		// any other application-specific initialisation must be put here
	}

	/**
	 * instantiate components and publish their ports.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.cvm.AbstractDistributedCVM#instantiateAndPublish()
	 */
	@Override
	public void instantiateAndPublish() throws Exception {
		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			// Create a list of Request Arrival Inbound Port from the cores
			ArrayList<String> coreRequestArrivalInboundPortUris = new ArrayList<>();
			// Create a list of Control Request Arrival Inbound Port from the cores
			ArrayList<String> coreControlRequestArrivalInboundPortUris = new ArrayList<>();
			
			// Create the cpu
			mCpus = new ArrayList<Cpu>();
			for (int i = 0; i < NROF_CPU; i++) {
				Cpu cpu = new Cpu(
						i, 
						NROF_CORES_PER_CPU, 
						CLOCK_SPEED, 
						MAX_CLOCK_SPEED,
						CPU_CRAIP_PREFIX + i,
						this
						);
				this.deployedComponents.add(cpu);
				mCpus.add(cpu);
				coreRequestArrivalInboundPortUris.addAll(cpu.getCoresRequestArrivalInboundPortUris());
				coreControlRequestArrivalInboundPortUris.addAll(cpu.getCoresControlRequestArrivalInboundPortUris());
			}
			
			// create the provider component (aka ServiceProvider)
			this.mAdmissionControl = new AdmissionControl(
					NROF_CPU * NROF_CORES_PER_CPU, 
					NROF_CORES_PER_VM, 
					NROF_VM_PER_DISPATCHER, 
					AC_CRGOP_PREFIX, 
					AC_ARAIP, 
					coreRequestArrivalInboundPortUris,
					coreControlRequestArrivalInboundPortUris
					);
			this.deployedComponents.add(mAdmissionControl);

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			// create the consumer component
			this.mRequestGenerator = new RequestGenerator(
					NROF_APPS, 
					MEAN_INTER_ARRIVAL_TIME, 
					MEAN_NROF_INSTRUCTIONS, 
					STANDARD_DEVIATION, 
					RG_RGOP_PREFIX, 
					RG_ARGOP);
			// add it to the deployed components
			this.deployedComponents.add(mRequestGenerator);

		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.instantiateAndPublish();
	}

	/**
	 * interconnect the components.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.cvm.AbstractDistributedCVM#interconnect()
	 */
	@Override
	public void interconnect() throws Exception {
		assert this.instantiationAndPublicationDone;

		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {
			// connect the VM to the core (for requests)
			
		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			// connect the request generator to the admission control (for
			// applications)
			PortI argport = this.mRequestGenerator.findPortFromURI(RG_ARGOP);
			argport.doConnection(AC_ARAIP,
					ApplicationRequestServiceConnector.class.getCanonicalName());

		} else {
			System.out.println("Error, wrong JVM URI: " + thisJVMURI);
			System.exit(1);
		}

		super.interconnect();
	}

	/**
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.cvm.AbstractDistributedCVM#start()
	 */
	@Override
	public void start() throws Exception {
		super.start();
		if (thisJVMURI.equals(CONSUMER_JVM_URI)) {
			final RequestGenerator fcg = this.mRequestGenerator;
			System.out.println("Scheduling request at "
					+ TimeProcessing.toString(System.currentTimeMillis()));
			fcg.runTask(new ComponentTask() {
				@Override
				public void run() {
					try {
						fcg.generateNextRequest();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} else if (thisJVMURI.equals(PROVIDER_JVM_URI)) {
			// nothing

		} else {
			System.out.println("Error, wrong JVM URI: " + thisJVMURI);
			System.exit(1);
		}
	}

	/**
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 	
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.cvm.AbstractDistributedCVM#shutdown()
	 */
	@Override
	public void shutdown() throws Exception {
		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {
			// any disconnection not done yet should be performed here


		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {
			// any disconnection not done yet should be performed here

			// disconnect the request generator from the admission control (for
			// applications)
			PortI consumerPort = mRequestGenerator.findPortFromURI(RG_ARGOP);
			consumerPort.doDisconnection();

			// disconnect the request generator from the VM (for requests)
			for (int i = 0; i < NROF_APPS; i++) {
				consumerPort = this.mRequestGenerator
						.findPortFromURI(RG_RGOP_PREFIX + i);
				if (consumerPort.connected()) {
					consumerPort.doDisconnection();
				}
			}
		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.shutdown();
	}

	public static void main(String[] args) {
		System.out.println("Beginning");
		try {
			DistributedCVM da = new DistributedCVM(args);
			da.deploy();
			System.out.println("starting...");
			da.start();
			Thread.sleep(MAIN_SLEEPING_DURATION);
			System.out.println("shutting down...");
			da.shutdown();
			System.out.println("ending...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Main thread ending");
		System.exit(0);
	}
}
