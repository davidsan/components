package fr.upmc.colins.farm3.core;

import fr.upmc.colins.farm3.objects.Response;
import fr.upmc.components.interfaces.TwoWayI;

public interface ResponseArrivalI 
extends		TwoWayI {

	void acceptResponse(Response response) throws Exception;

}
