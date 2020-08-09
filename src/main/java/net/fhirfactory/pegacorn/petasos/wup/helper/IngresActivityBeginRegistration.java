/*
 * Copyright (c) 2020 Mark A. Hunter (ACT Health)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.fhirfactory.pegacorn.petasos.wup.helper;

import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.wup.PetasosServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.pathway.ContinuityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPActivityStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.topology.manager.proxies.ServiceModuleTopologyProxy;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;

public class IngresActivityBeginRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(IngresActivityBeginRegistration.class);

    @Inject
    ServiceModuleTopologyProxy topologyProxy;

    @Inject
    PetasosServicesBroker servicesBroker;

    public UoW registerActivityStart(UoW theUoW, Exchange camelExchange, String wupInstanceKey){
        LOG.debug(".registerActivityStart(): Entry, payload --> {}, wupInstanceKey --> {}", theUoW, wupInstanceKey);
        LOG.trace(".registerActivityStart(): reconstituted token, now attempting to retrieve NodeElement");
        NodeElement node = topologyProxy.getNodeByKey(wupInstanceKey);
        LOG.trace(".registerActivityStart(): Node Element retrieved --> {}", node);
        NodeElementFunctionToken wupFunctionToken = node.getNodeFunctionToken();
        LOG.trace(".registerActivityStart(): wupFunctionToken (NodeElementFunctionToken) for this activity --> {}", wupFunctionToken);        
        LOG.trace(".registerActivityStart(): Building the ActivityID for this activity");
        NodeElementIdentifier wupNodeID = node.getIdentifier();
        ContinuityID newActivityID = new ContinuityID();
        newActivityID.setPresentWUPFunctionToken(wupFunctionToken);
        newActivityID.setPresentWUPIdentifier(new WUPIdentifier(node.getIdentifier()));
        LOG.trace(".registerActivityStart(): newActivityID (ContinuityID) --> {}", newActivityID);
        LOG.trace(".registerActivityStart(): Creating new JobCard");
        WUPJobCard activityJobCard = new WUPJobCard(newActivityID, WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING, WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING, topologyProxy.getConcurrencyMode(wupNodeID), topologyProxy.getDeploymentResilienceMode(wupNodeID),  Date.from(Instant.now()));
        LOG.trace(".registerActivityStart(): Registering the Work Unit Activity using the activityJobCard --> {} and UoW --> {}", activityJobCard, theUoW);
        ParcelStatusElement statusElement = servicesBroker.registerSystemEdgeWUA(activityJobCard, theUoW);
        LOG.trace(".registerActivityStart(): Registration aftermath: statusElement --> {}", statusElement);
        // Now we have to Inject some details into the Exchange so that the WUPEgressConduit can extract them as per standard practice
        LOG.trace(".registerActivityStart(): Injecting Job Card and Status Element into Exchange for extraction by the WUP Egress Conduit");
        String jobcardPropertyKey = "WUPJobCard" + wupInstanceKey; // this value should match the one in WUPIngresConduit.java/WUPEgressConduit.java
        String parcelStatusPropertyKey = "ParcelStatusElement" + wupInstanceKey; // this value should match the one in WUPIngresConduit.java/WUPEgressConduit.java
        camelExchange.setProperty(jobcardPropertyKey, activityJobCard); // <-- Note the "WUPJobCard" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        camelExchange.setProperty(parcelStatusPropertyKey, statusElement); // <-- Note the "ParcelStatusElement" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        LOG.debug(".registerActivityStart(): exit, my work is done!");
        return(theUoW);
    }
}
