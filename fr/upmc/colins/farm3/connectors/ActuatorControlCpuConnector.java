package fr.upmc.colins.farm3.connectors;

import java.io.Serializable;
import java.util.ArrayList;

import fr.upmc.colins.farm3.core.ControlRequestArrivalI;
import fr.upmc.components.connectors.AbstractConnector;

public class ActuatorControlCpuConnector extends		AbstractConnector
implements	ControlRequestArrivalI,	Serializable
{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean updateClockSpeed(Double clockSpeed) throws Exception {
		return ((ControlRequestArrivalI)this.offering).updateClockSpeed(clockSpeed);
	}

	@Override
	public ArrayList<String> getCoresRequestArrivalInboundPortUris()
			throws Exception {
		return ((ControlRequestArrivalI) this.offering)
				.getCoresRequestArrivalInboundPortUris();
	}

	@Override
	public ArrayList<String> getCoresControlRequestArrivalInboundPortUris()
			throws Exception {
		return ((ControlRequestArrivalI) this.offering)
		.getCoresControlRequestArrivalInboundPortUris();
	}



}