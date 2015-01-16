package fr.upmc.colins.farm3.dispatcher.dynamic;

import java.util.ArrayList;

import fr.upmc.colins.farm3.dispatcher.RequestDispatcher;
/**
 * The class <code>DynamicRequestDispatcher</code> implements the dynamic version of
 * the <code>RequestDispatcher</code> component.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : december. 2015</p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class DynamicRequestDispatcher extends RequestDispatcher {

	public DynamicRequestDispatcher(
			Integer id, 
			String inboundPortURI,
			ArrayList<String> outboundPortURIs,
			ArrayList<String> vmRequestArrivalInboundPortUris,
			Double meanNrofInstructions, 
			Double standardDeviation)
			throws Exception {
		super(id, inboundPortURI, outboundPortURIs, vmRequestArrivalInboundPortUris,
				meanNrofInstructions, standardDeviation);
	}


}
