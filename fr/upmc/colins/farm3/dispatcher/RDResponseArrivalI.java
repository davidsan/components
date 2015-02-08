package fr.upmc.colins.farm3.dispatcher;

import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.interfaces.TwoWayI;

public interface RDResponseArrivalI 
extends		TwoWayI {

	void acceptResponse(Response response) throws Exception;

}
