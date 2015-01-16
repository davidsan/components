package fr.upmc.colins.farm3.vm.dynamic;

import java.util.ArrayList;

import fr.upmc.colins.farm3.vm.VM;

/**
 * The class <code>DynamicVM</code> implements the dynamic version of
 * the <code>VM</code> component.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : jan. 2015</p>
 * 
 * @author Colins-Alasca
 * @version $Name$ -- $Revision$ -- $Date$
 */
public class			DynamicVM
extends VM
{

	public DynamicVM(
			Integer id, 
			String inboundPortURI,
			ArrayList<String> outboundPortURIs,
			ArrayList<String> coreRequestArrivalInboundPortUris
			) throws Exception {
		super(id, inboundPortURI, outboundPortURIs, coreRequestArrivalInboundPortUris);

	}

	
	
}
