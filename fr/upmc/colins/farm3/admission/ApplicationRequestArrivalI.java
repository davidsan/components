package fr.upmc.colins.farm3.admission;

import fr.upmc.colins.farm3.objects.Application;
import fr.upmc.components.interfaces.TwoWayI;

/**
 * The interface <code>ApplicationRequestArrivalI</code> defines the protocol to send an
 * application upload request 
 *
 * <p><strong>Description</strong></p>
 * 
 * The interface can be both required and offered, and it has only one method
 * <code>acceptApplication</code> passing a Application object as parameter.
 * 
 * <p>Created on : december 2014</p>
 * 
 * @author	Colins
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public interface		ApplicationRequestArrivalI
extends		TwoWayI
{
	/**
	 * accept a new application
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	a != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param a				application to be uploaded.
	 * @throws Exception
	 * @return uri of the service provider for the application	
	 */
	String			acceptApplication(Application a) throws Exception ;
	
}
