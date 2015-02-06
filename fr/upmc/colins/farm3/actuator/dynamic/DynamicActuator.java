package fr.upmc.colins.farm3.actuator.dynamic;

import java.util.ArrayList;

import fr.upmc.colins.farm3.actuator.Actuator;

public class DynamicActuator extends Actuator{

	public DynamicActuator(Integer id, 
						   Double wantedRate, 
						   String actuatorResponseArrivalInboundPortUri,
						   ArrayList<String> actuatorRequestGeneratorOutboundPortUris,
						   ArrayList<String> usedCoreRequestArrivalInboundPortUris) throws Exception {
		super(id, 
			  wantedRate, 
			  actuatorResponseArrivalInboundPortUri,
			  actuatorRequestGeneratorOutboundPortUris,
			  usedCoreRequestArrivalInboundPortUris);
	}


}
