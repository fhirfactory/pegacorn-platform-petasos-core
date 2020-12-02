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

package net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.RecipientList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;

/**
 * @author Mark A. Hunter
 * @since 2020-07-01
 */
@Dependent
public class WUPContainerEgressGatekeeper {
    private static final Logger LOG = LoggerFactory.getLogger(WUPContainerEgressGatekeeper.class);
    private static final String EGRESS_GATEKEEPER_PROCESSED_PROPERTY = "EgressGatekeeperSemaphore";

    @Inject
    DeploymentTopologyIM topologyProxy;

    private String getGatekeeperProperty(FDNToken wupIdentifier) {
        FDN workingFDN = new FDN(wupIdentifier);
        String workingInstanceID = workingFDN.getUnqualifiedRDN().getValue();
        String stringToUse = EGRESS_GATEKEEPER_PROCESSED_PROPERTY + "-" + workingInstanceID;
        return (stringToUse);
    }

    /**
     * This class/method checks the status of the WUPJobCard for the parcel, and ascertains if it is to be
     * discarded (because of some processing error or due to the fact that the processing has occurred already
     * within another WUP). At the moment, it reaches the "discard" decisions purely by checking the
     * WUPJobCard.isToBeDiscarded boolean.
     *
     * @param transportPacket The WorkUnitTransportPacket that is to be forwarded to the Intersection (if all is OK)
     * @param camelExchange   The Apache Camel Exchange object, used to store a Semaphore as we iterate through Dynamic Route options
     * @param wupInstanceKey  The Work Unit Processor Instance Key, used to retrieve the associated NodeElement for the WUP
     * @return Should either return the ingres point into the associated Interchange Payload Transformer or null (if the packet is to be discarded)
     */
    @RecipientList
    public List<String> egressGatekeeper(WorkUnitTransportPacket transportPacket, Exchange camelExchange, String wupInstanceKey) {
        LOG.info(".egressGatekeeper(): Enter, transportPacket (WorkUnitTransportPacket) --> {}, wupInstanceKey (String) --> {}", transportPacket,wupInstanceKey );
        // Get my Petasos Context
        NodeElement node = topologyProxy.getNodeByKey(wupInstanceKey);
        LOG.trace(".egressGatekeeper(): Node Element retrieved --> {}", node);
        NodeElementFunctionToken wupFunctionToken = node.getNodeFunctionToken();
        LOG.trace(".egressGatekeeper(): wupFunctionToken (NodeElementFunctionToken) for this activity --> {}", wupFunctionToken);
        // Now, continue with business logic
        RouteElementNames nameSet = new RouteElementNames(wupFunctionToken);
        ArrayList<String> targetList = new ArrayList<String>();
        if(!transportPacket.hasCurrentJobCard()) {
            LOG.error(".egressGatekeeper(): CurrentJobCard is null!");
        }
        if (transportPacket.getCurrentJobCard().getIsToBeDiscarded()) {
            LOG.trace(".egressGatekeeper(): The isToBeDiscarded attribute is true, so we return null (and discard the packet");
            LOG.debug(".egressGatekeeper(): Returning null, as message is to be discarded (isToBeDiscarded == true)");
            return (targetList);
        } else {
            LOG.trace(".egressGatekeeper(): the isToBeDiscarded attribute is false, so we need to set the Semaphore (so we know we've processed this packet)");
            LOG.trace(".egressGatekeeper(): And we return the ingres point of the associated Interchange Payload Transformer");
            String targetEndpoint = nameSet.getEndPointInterchangePayloadTransformerIngres();
            targetList.add(targetEndpoint);
            LOG.debug(".egressGatekeeper(): Returning route to the Interchange Payload Transformer instance --> {}", targetEndpoint);
            return (targetList);
        }
    }
}
