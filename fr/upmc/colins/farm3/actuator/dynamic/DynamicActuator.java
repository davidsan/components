package fr.upmc.colins.farm3.actuator.dynamic;

import java.util.ArrayList;

import fr.upmc.colins.farm3.actuator.Actuator;

public class DynamicActuator extends Actuator{

	public DynamicActuator(Integer id, 
			String actuatorResponseArrivalInboundPortUri,
			ArrayList<String> usedCoreControlInboundPortUris,
			ArrayList<String> outboundCoreControlPortURIs) throws Exception {
		super(id,
				actuatorResponseArrivalInboundPortUri,
				usedCoreControlInboundPortUris,
				outboundCoreControlPortURIs);
	}


}
