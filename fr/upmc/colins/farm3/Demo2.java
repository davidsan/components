package fr.upmc.colins.farm3;

import java.util.ArrayList;

import fr.upmc.colins.farm3.admission.AdmissionControl;
import fr.upmc.colins.farm3.connectors.ApplicationRequestServiceConnector;
import fr.upmc.colins.farm3.cpu.Cpu;
import fr.upmc.colins.farm3.generator.RequestGeneratorLoadFirstApp;
import fr.upmc.colins.farm3.utils.TimeProcessing;
import fr.upmc.components.ComponentI.ComponentTask;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.PortI;

/**
 * The class <code>Demo2</code> contains an example of scenario with 
 * a single CPU of 2 cores and two applications consuming each 1 core
 * with 2 virtual machines.
 * 
 * This main use a specific request generator which will send more 
 * request for the first application.
 * 
 * <p>
 * Created on : feb. 2015
 * </p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */

public class Demo2 extends AbstractCVM {

	protected static final String 		logId = "[    CVM     ]";
	// Settings
	/** the main sleep's duration										*/
	protected static final long 		MAIN_SLEEPING_DURATION = 500000L;
	/** the default clock speed											*/
	protected static final Double 		CLOCK_SPEED = 1.0;
	/** the maximum clock speed											*/
	protected static final Double 		MAX_CLOCK_SPEED = 3.0;
	/** the maximum clock speed	gap										*/
	protected static final Double 		MAX_CLOCK_SPEED_GAP = 0.5;
	/** the number of cpu in the cluster								*/
	protected static final Long 		NROF_CPU = 1L;
	/** the number of cores	in the cluster								*/
	protected static final Long 		NROF_CORES_PER_CPU = 2L;
	/** the number of applications to be submitted by the consumer		*/
	protected static final Long 		NROF_APPS = 2L;
	/** the number of cores allocated per virtual machines				*/
	protected static final int 			NROF_CORES_PER_VM = 1;
	/** the number of virtual machines allocated per dispatcher			*/
	protected static final int 			NROF_VM_PER_DISPATCHER = 1;
	/** the mean inter arrival time										*/
	protected static final double 		MEAN_INTER_ARRIVAL_TIME = 1000.0;
	/** the standard deviation 											*/
	protected static final double 		STANDARD_DEVIATION = 100.0;
	/** the mean number of instructions 								*/
	protected static final double 		MEAN_NROF_INSTRUCTIONS = 1000.0;
	

	/** the step value of frequency when changing the frequency			*/
	protected static final double 		BOOST_STEP = 0.1;
	/** the target service time in milliseconds							*/
	protected static final int 			TARGET_SERVICE_TIME = 800;
	/** the flex time for target service time in milliseconds			*/
	protected static final int 			FLEX_SERVICE_TIME = 50;
	
	
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
	protected RequestGeneratorLoadFirstApp mRequestGenerator;
	
	/**
	 * create a compute cluster (cores, admission control) and a request
	 * generator components, register them and connect them.
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
	 * @see fr.upmc.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void deploy() throws Exception {
		//////////////
		// Provider //
		//////////////
		
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
					MAX_CLOCK_SPEED_GAP,
					CPU_CRAIP_PREFIX + i,
					this
					);
			this.deployedComponents.add(cpu);
			mCpus.add(cpu);
			coreRequestArrivalInboundPortUris.addAll(cpu.getCoresRequestArrivalInboundPortUris());
			coreControlRequestArrivalInboundPortUris.addAll(cpu.getCoresControlRequestArrivalInboundPortUris());
		}

		mAdmissionControl = new AdmissionControl(
				NROF_CPU * NROF_CORES_PER_CPU, 
				NROF_CORES_PER_VM, 
				NROF_VM_PER_DISPATCHER, 
				AC_CRGOP_PREFIX, 
				AC_ARAIP, 
				coreRequestArrivalInboundPortUris,
				coreControlRequestArrivalInboundPortUris
				);
		this.deployedComponents.add(mAdmissionControl);
		
		//////////////
		// Consumer	//	
		//////////////
		this.mRequestGenerator = new RequestGeneratorLoadFirstApp(
				NROF_APPS, 
				MEAN_INTER_ARRIVAL_TIME, 
				MEAN_NROF_INSTRUCTIONS, 
				STANDARD_DEVIATION, 
				BOOST_STEP,
				TARGET_SERVICE_TIME,
				FLEX_SERVICE_TIME,
				RG_RGOP_PREFIX, 
				RG_ARGOP
				);
		
		this.deployedComponents.add(this.mRequestGenerator);

		// connect the request generator to the admission control (for applications)
		PortI argport = this.mRequestGenerator.findPortFromURI(RG_ARGOP);
		argport.doConnection(AC_ARAIP,
				ApplicationRequestServiceConnector.class.getCanonicalName());

		super.deploy();
	}

	/**
	 * disconnect the request generator from the service provider component and
	 * then shut down all of the components.
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
	 * @see fr.upmc.components.cvm.AbstractCVM#shutdown()
	 */
	@Override
	public void shutdown() throws Exception {
		//////////////
		// Consumer	//	
		//////////////
		// disconnect the request generator from the admission control (for applications)
		PortI consumerPort = this.mRequestGenerator.findPortFromURI(RG_ARGOP);
		consumerPort.doDisconnection();
		// disconnect the request generator from the VM (for requests)
		for (int i = 0; i < NROF_APPS; i++) {
			consumerPort = this.mRequestGenerator
					.findPortFromURI(RG_RGOP_PREFIX + i);
			if (consumerPort.connected()) {
				consumerPort.doDisconnection();
			}
		}
		//////////////
		// Provider //
		//////////////

		super.shutdown();
	}

	/**
	 * create the virtual machine, deploy the components, start them, launch the
	 * request generation and then shut down after 15 seconds of execution.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Demo2 a = new Demo2();
		try {
			a.deploy();
			System.out.println(logId + " Starting...");
			a.start();

			final RequestGeneratorLoadFirstApp fcg = a.mRequestGenerator;
			System.out.println(logId + " Kick start request at "
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
			Thread.sleep(MAIN_SLEEPING_DURATION);
			a.shutdown();
			System.out.println(logId + " Ending...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}