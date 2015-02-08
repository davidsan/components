package fr.upmc.colins.farm3.connectors;

import java.io.Serializable;

import fr.upmc.colins.farm3.dispatcher.RDResponseArrivalI;
import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.connectors.AbstractConnector;

public class RDResponseServiceConnector extends AbstractConnector implements
		RDResponseArrivalI, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public void acceptResponse(Response response) throws Exception {
		((RDResponseArrivalI) this.offering).acceptResponse(response);
	}

}
