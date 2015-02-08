package fr.upmc.colins.farm3.cpu;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import fr.upmc.colins.farm3.connectors.ControlRequestServiceConnector;
import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.colins.farm3.core.Core;
import fr.upmc.colins.farm3.core.Core2CpuI;
import fr.upmc.colins.farm3.generator.RequestGeneratorOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;

/**
 * The class <code>Cpu</code> implements a component that simulates a central
 * processing unit.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * This component also create in its constructor the cores. 
 * The <code>Cpu</code> component is parameterized with a default clock speed,
 * a maximum clock speed and a number of cores.
 * 
 * 
 * <p>
 * Created on : 23 nov. 2014
 * </p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class Cpu extends AbstractComponent {
	

	protected String logId;
	
	protected static final String CPU_PREFIX = "cpu-";
	protected static final String CPU_CRGOP_PREFIX = "-crgop-";
	protected static final String CORE_RAIP_PREFIX = "-core-raip-";
	protected static final String CORE_CRAIP_PREFIX = "-core-craip-";
	protected static final String CPU_CRAIP_PREFIX = "-cpu-craip-";
	
	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------
	
	/** cpu identifier 															*/
	protected int 			cpuId;
	/**	number of cores 														*/
	protected Long 		nrofCores;
	/**	default clock speed of the cores 										*/
	protected Double 		clockSpeed;
	/**	default maximum clock speed of the cores 								*/
	protected Double 		maxClockSpeed;
	/**	default maximum gap in clock speed for all cores						*/
	protected Double 		maxGapClockSpeed;
	
	/** list of outboundports of the cpu to each cores							*/
	protected ArrayList<RequestGeneratorOutboundPort> cpuRequestGeneratorOutboundPorts;	
	/** inbound port for cpu control request arrival 							*/
	protected CpuControlRequestArrivalInboundPort cpuInboundPort;
	
	/** dynamic component creation outbound port to the provider's JVM			*/
	protected DynamicComponentCreationOutboundPort portToProviderJVM;
	/** list of inbound port uri core request arrival							*/
	protected ArrayList<String> coreRequestArrivalInboundPortUris;
	/** list of inbound port uri for core control request arrival				*/
	protected ArrayList<String> coreControlRequestArrivalInboundPortUris;
	/** list of outbound ports for control request generator					*/
	protected ArrayList<ControlRequestGeneratorOutboundPort> controlRequestGeneratorOutboundPorts;


	
	/**
	 * create a CPU with its cores
	 * @param cpuId
	 * 				unique identifier of the cpu
	 * @param nrofCores 
	 * 				number of cores
	 * @param clockSpeed 
	 * 				clock speed of the cpu's cores
	 * @param maxClockSpeed 
	 * 				maximum clock speed of the cpu's cores
	 * @param controlInboundPortURI 
	 *           	URI of the inbound port to connect to the admission control
	 * @throws Exception
	 */
	public Cpu(
			Integer cpuId, 
			Long nrofCores, 
			Double clockSpeed,
			Double maxClockSpeed,
			Double maxGapClockSpeed,
			String controlInboundPortURI,
			AbstractCVM cvm
			) throws Exception 
	{
		super(true, true);
		this.logId = MessageFormat.format("[  Cpu {0}  ]", String.format("%04d", cpuId));

		assert nrofCores > 0 && clockSpeed > 0.0;
		assert controlInboundPortURI != null;
		
		this.cpuId = cpuId;
		this.nrofCores = nrofCores;
		this.clockSpeed = clockSpeed;
		this.maxClockSpeed = maxClockSpeed;
		this.maxGapClockSpeed = maxGapClockSpeed;
		this.coreRequestArrivalInboundPortUris = new ArrayList<>();
		this.coreControlRequestArrivalInboundPortUris = new ArrayList<>();
		
		this.addRequiredInterface(ControlRequestArrivalI.class);
		this.addOfferedInterface(ControlRequestArrivalI.class);
		this.addOfferedInterface(Core2CpuI.class);
		this.controlRequestGeneratorOutboundPorts = new ArrayList<>();
		for (int i = 0; i < this.nrofCores; i++) {
			String crgopCpuUri = CPU_PREFIX + cpuId + CPU_CRGOP_PREFIX + i;
			String raipCoreUri= CPU_PREFIX + cpuId + CORE_RAIP_PREFIX + i;
			String craipCoreUri = CPU_PREFIX + cpuId + CORE_CRAIP_PREFIX + i;
			String craipCpuUri =  CPU_PREFIX + cpuId + CPU_CRAIP_PREFIX + i ;
			// build the core
			Core core = new Core(
				i,
				clockSpeed,
				maxClockSpeed,
				raipCoreUri,
				craipCoreUri,
				craipCpuUri 
			);
			cvm.addDeployedComponent(core);
			
			coreRequestArrivalInboundPortUris.add(CPU_PREFIX + cpuId + CORE_RAIP_PREFIX + i);
			coreControlRequestArrivalInboundPortUris.add(CPU_PREFIX + cpuId + CORE_CRAIP_PREFIX + i);
			
			ControlRequestGeneratorOutboundPort crgop = new ControlRequestGeneratorOutboundPort(crgopCpuUri, this);
			this.controlRequestGeneratorOutboundPorts.add(crgop);			
			if (AbstractCVM.isDistributed) {
				crgop.publishPort();
			} else {
				crgop.localPublishPort();
			}
			crgop.doConnection(craipCoreUri, 
					ControlRequestServiceConnector.class.getCanonicalName());
			
			Core2CpuInboundPort c2cip = new Core2CpuInboundPort(craipCpuUri, this);
			if (AbstractCVM.isDistributed) {
				c2cip.publishPort();
			} else {
				c2cip.localPublishPort();
			}
			
			
		}
		
		this.cpuInboundPort = new CpuControlRequestArrivalInboundPort(controlInboundPortURI, this);
		this.addPort(cpuInboundPort);
		if (AbstractCVM.isDistributed) {
			this.cpuInboundPort.publishPort();
		} else {
			this.cpuInboundPort.localPublishPort();
		}

		System.out.println(logId + " Central Processing Unit "
				+ this.clockSpeed + " / " + this.maxClockSpeed + " GHz (id "
				+ cpuId + ") created");

		assert this.coreRequestArrivalInboundPortUris.size() == this.nrofCores;
		assert this.coreControlRequestArrivalInboundPortUris.size() == this.nrofCores;
		assert this.nrofCores > 0.0 && this.clockSpeed > 0.0;
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		super.start() ;
	}
	
	
	/**
	 * shut down the component
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
		try {
			for (ControlRequestGeneratorOutboundPort crgop : controlRequestGeneratorOutboundPorts) {

				if (crgop.connected()) {
					crgop.doDisconnection();
				}

			}
		} catch (Exception e) {
			throw new ComponentShutdownException();
		}
		super.shutdown();
	}

	
	// -------------------------------------------------------------------------
	// Component internal services
	// -------------------------------------------------------------------------


	/**
	 * update the clock speed of all cores
	 * @param clockSpeed new clock speed
	 * @return true if all the cores were successfully updated, else false
	 * @throws Exception
	 */
	public boolean updateClockSpeed(Double clockSpeed) throws Exception {
		if(clockSpeed > maxClockSpeed || clockSpeed <= 0){
			return false;
		}
		boolean updated = true;
		for (int i = 0; i < controlRequestGeneratorOutboundPorts.size(); i++) {
			updated = updated
					&& controlRequestGeneratorOutboundPorts.get(i)
							.updateClockSpeed(clockSpeed);
		}
		return updated;
	}


	/**
	 * return the list of inbound port uri to each core
	 * @return the list of inbound port uri to each core
	 */
	public ArrayList<String> getCoresRequestArrivalInboundPortUris() {
		return coreRequestArrivalInboundPortUris;
	}

	public ArrayList<String> getCoresControlRequestArrivalInboundPortUris() {
		return coreControlRequestArrivalInboundPortUris;

	}

	public Boolean acceptUpdateClockspeedRequest(Double newClockSpeed,
			Integer coreId) throws Exception {

		if (newClockSpeed > maxClockSpeed) {
			return false;
		}
		
		
		int coreIndex = coreId;
		ControlRequestGeneratorOutboundPort crgop = this.controlRequestGeneratorOutboundPorts
				.get(coreIndex);


		System.out.println(logId
				+ " Received a request to update clockspeed of core " + coreId
				+ " from " + crgop.getClockSpeed()
				+ " to " + newClockSpeed + " GHz");
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		// this method should not be called asynchronously
		for (int i = 0; i < controlRequestGeneratorOutboundPorts.size(); i++) {
			if (i == coreIndex) {
				continue;
			}
			stats.addValue(controlRequestGeneratorOutboundPorts.get(i)
					.getClockSpeed());
		}

		if (newClockSpeed > stats.getMax()) {
			// allow and make the necessary overclocking
			System.out.println(logId + " Might overclock some cores.");
			for (int i = 0; i < controlRequestGeneratorOutboundPorts.size(); i++) {
				if (i == coreIndex) {
					continue;
				}
				
				if (newClockSpeed - crgop.getClockSpeed() > maxGapClockSpeed) {
					crgop.updateClockSpeed(newClockSpeed);
				}
			}
		} else if (newClockSpeed < stats.getMin()) {
			// allow only if we can make the changes without underclocking the
			// others
			for (Double freq : stats.getValues()) {
				if (freq - newClockSpeed > maxGapClockSpeed) {
					return false;
				}
			}
		}
		this.controlRequestGeneratorOutboundPorts.get(coreIndex)
				.updateClockSpeed(newClockSpeed);
		return true;
	}
	
}
