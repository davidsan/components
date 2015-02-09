package fr.upmc.colins.farm3.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;

import fr.upmc.colins.farm3.VerboseSettings;
import fr.upmc.colins.farm3.admission.ApplicationRequestArrivalI;
import fr.upmc.colins.farm3.connectors.RequestServiceConnector;
import fr.upmc.colins.farm3.core.RequestArrivalI;
import fr.upmc.colins.farm3.objects.Application;
import fr.upmc.colins.farm3.objects.Request;
import fr.upmc.colins.farm3.utils.TimeProcessing;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;

/**
 * The class <code>RequestGenerator</code> implements a component that generates
 * requests for a service provider in a discrete-event based simulation.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 *
 * A request has a processing time and an arrival process that both follow an
 * exponential probability distribution. The generation process is started by
 * executing the method <code>generateNextRequest</code> as a component task. It
 * generates an instance of the class <code>Request</code>, with a processing
 * time generated from its exponential distribution, and then schedule its next
 * run after the interarrival time also generated from its exponential
 * distribution. To stop the generation process, the method
 * <code>shutdown</code> uses the future returned when scheduling the next
 * request generation to cancel its execution.
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * <pre>
 * invariant	rng != null && counter >= 0
 * invariant	meanInterArrivalTime > 0.0 && meanProcessingTime > 0.0
 * invariant	rgop != null && rgop instanceof RequestArrivalI
 * </pre>
 * 
 * <p>
 * Created on : 2 sept. 2014
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class RequestGenerator extends AbstractComponent {

	private static final String logId = "[ RequestGen ]";


	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------

	/** a random number generator used to generate processing times. 			*/
	protected RandomDataGenerator rng;
	/** a counter used to generate request URI. 								*/
	protected int counter;
	/** the mean interarrival time of requests in ms. 							*/
	protected double meanInterArrivalTime;
	/** the standard deviation													*/
	protected double standardDeviation;
	/** the mean number of instructions of the deployed apps					*/
	protected double meanNrofInstructions;
	/** the output port used to send requests to the service provider. 			*/
	protected List<RequestGeneratorOutboundPort> rgops;
	/** the output port used to send applications to the service provider. 		*/
	protected ApplicationRequestGeneratorOutboundPort argop;
	/** a future pointing to the next request generation task. 					*/
	protected Future<?> nextRequestTaskFuture;
	/** number of applications 													*/
	protected long nrofApplications;
	/** count of application deployed 											*/
	protected int appCount;

	/** step value of frequency when changing the frequency						*/
	protected double boostStep;
	/** target service time in milliseconds										*/
	protected int targetServiceTime;
	/** flex time for target service time in milliseconds						*/
	protected int flexServiceTime;

	/** starting time															*/
	protected long startTime = System.currentTimeMillis();
	

	/**
	 * create a request generator component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	meanInterArrivalTime > 0.0 && meanProcessingTime > 0.0
	 * pre	outboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
 	 * @param nrofApplications 
	 * 			  number of applications to deploy
	 * @param meanInterArrivalTime
	 *            mean interarrival time of the requests in ms.
	 * @param meanNrofInstructions
	 *            mean number of instructions of the deployed apps.
	 * @param standardDeviation
	 *            standard deviation of the number of instructions
	 * @param requestOutboundPortPrefix
	 * 			  uri of the outbound ports to the dispatchers
	 * @param appRequestOutboundPortURI
	 *            uri of the outbound port to the admission control
	 * @throws Exception
	 */
	public RequestGenerator(
			long nrofApplications,
			double meanInterArrivalTime,
			double meanNrofInstructions, 
			double standardDeviation,
			double boostStep,
			int targetServiceTime,
			int flexServiceTime,
			String requestOutboundPortPrefix, 
			String appRequestOutboundPortURI
			) throws Exception {
		super(true, true);

		assert meanInterArrivalTime > 0.0 && meanNrofInstructions > 0.0;
		assert requestOutboundPortPrefix != null;
		assert appRequestOutboundPortURI != null;

		this.counter = 0;
		this.meanInterArrivalTime = meanInterArrivalTime;
		this.meanNrofInstructions = meanNrofInstructions;
		this.standardDeviation = standardDeviation;
		this.rng = new RandomDataGenerator();
		this.rng.reSeed();
		this.nextRequestTaskFuture = null;
		this.boostStep = boostStep;
		this.targetServiceTime = targetServiceTime;
		this.flexServiceTime = flexServiceTime;

		this.rgops = new ArrayList<>();
		// Component management
		this.addRequiredInterface(RequestArrivalI.class);
		for (int i = 0; i < nrofApplications; i++) {
			RequestGeneratorOutboundPort rgop = new RequestGeneratorOutboundPort(requestOutboundPortPrefix + i, this);
			this.addPort(rgop);
			rgop.localPublishPort();
			this.rgops.add(rgop);
		}
		
		this.addRequiredInterface(ApplicationRequestArrivalI.class);
		this.argop = new ApplicationRequestGeneratorOutboundPort(
				appRequestOutboundPortURI, this);
		this.addPort(this.argop);
		this.argop.localPublishPort();

		this.nrofApplications = nrofApplications;
		this.appCount = 0;

		System.out.println(logId + " Request generator created") ;
		
		assert rng != null && counter >= 0;
		assert meanInterArrivalTime > 0.0 && meanNrofInstructions > 0.0;
		assert rgops.size() == this.nrofApplications;
		assert argop != null && argop instanceof ApplicationRequestArrivalI;
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * shut down the component, first cancelling any future request generation
	 * already scheduled.
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
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		if (this.nextRequestTaskFuture != null
				&& !(this.nextRequestTaskFuture.isCancelled() || this.nextRequestTaskFuture
						.isDone())) {
			this.nextRequestTaskFuture.cancel(true);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal services
	// -------------------------------------------------------------------------

	/**
	 * generate a new request with some processing time following an exponential
	 * distribution and then schedule the next request generation in a delay
	 * also following an exponential distribution.
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
	 * @throws Exception
	 */
	public void generateNextRequest() throws Exception {

		if(appCount < nrofApplications){
			if(VerboseSettings.VERBOSE_GENERATOR)
				System.out.println(logId + " Submit a new app (id "
						+ appCount + ")");
			String rdUri = this.argop.acceptApplication(
					new Application(
							appCount,
							this.meanNrofInstructions,
							this.standardDeviation,
							this.boostStep,
							this.targetServiceTime,
							this.flexServiceTime
							));

			if(VerboseSettings.VERBOSE_GENERATOR)
				System.out
						.println(logId + " Connect to the request dispatcher for the requested app (via " + rdUri+")");
			this.rgops.get(appCount).doConnection(rdUri,
					RequestServiceConnector.class.getCanonicalName());
			appCount++;
		}
		
		// draw a random app from the deployed app
		
		Random rnd = new Random();
		int	requestedApp = rnd.nextInt(appCount); 

		if(this.rgops.get(requestedApp).connected()){
			this.rgops.get(requestedApp).acceptRequest(new Request(this.counter++, requestedApp));
		}
		final RequestGenerator cg = this;
		long interArrivalDelay = (long) this.rng
				.nextExponential(this.meanInterArrivalTime);
//		System.err.println(System.currentTimeMillis()-this.startTime);
//		System.err.println(Math.max(0.1, Math.sin((System.currentTimeMillis()-this.startTime)/10000.0)));
		interArrivalDelay = (long) (interArrivalDelay + interArrivalDelay * Math.max(0.1, Math.sin((System.currentTimeMillis()-this.startTime)/5000.0)));
		if(VerboseSettings.VERBOSE_GENERATOR)
			System.out.println(logId + " Scheduling request app " + requestedApp + "  at "
					+ TimeProcessing.toString(System.currentTimeMillis()
							+ interArrivalDelay));
		this.nextRequestTaskFuture = this.scheduleTask(new ComponentTask() {
			@Override
			public void run() {
				try {
					cg.generateNextRequest();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, interArrivalDelay, TimeUnit.MILLISECONDS);
	}
}
