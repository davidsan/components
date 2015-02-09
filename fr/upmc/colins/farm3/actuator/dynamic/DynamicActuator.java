package fr.upmc.colins.farm3.actuator.dynamic;

import java.util.ArrayList;

import fr.upmc.colins.farm3.actuator.Actuator;
/**
 * The class <code>DynamicActuator</code> implements the dynamic version of
 * the <code>Actuator</code> component.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : feb. 2015</p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class DynamicActuator extends Actuator {

	public DynamicActuator(
			Integer id, 
			Double boostStep,
			Long targetServiceTime, 
			Long flexServiceTime,
			String actuatorResponseArrivalInboundPortUri,
			ArrayList<String> assignedCoreControlRequestArrivalInboundPortUris
			)
			throws Exception 
	{
		super(
				id, 
				boostStep, 
				targetServiceTime, 
				flexServiceTime,
				actuatorResponseArrivalInboundPortUri,
				assignedCoreControlRequestArrivalInboundPortUris
				);
	}

}
