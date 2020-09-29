/*
 * Copyright (c) 2020 MAHun
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

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import org.apache.camel.Exchange;
import org.apache.camel.RecipientList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark A. Hunter
 * @since 2020-06-01
 */
@Dependent
public class WUPContainerIngresGatekeeper {
    private static final Logger LOG = LoggerFactory.getLogger(WUPContainerIngresGatekeeper.class);
    private static final String INGRES_GATEKEEPER_PROCESSED_PROPERTY = "IngresGatekeeperSemaphore";

    @Inject
    DeploymentTopologyIM topologyProxy;

    /**
     * This class/method checks the status of the WUPJobCard for the parcel, and ascertains if it is to be
     * discarded (because of some processing error or due to the fact that the processing has occurred already
     * within another WUP). At the moment, it reaches the "discard" decisions purely by checking the
     * WUPJobCard.isToBeDiscarded boolean.
     *
     * @param ingresPacket     The WorkUnitTransportPacket that is to be forwarded to the Intersection (if all is OK)
     * @param camelExchange    The Apache Camel Exchange object, used to store a Semaphore as we iterate through Dynamic Route options
     * @param nodeKey    The Work Unit Processor Instance: only to be used for instance debugging (not used at the moment)
     * @return Should either return the ingres point into the associated WUP Ingres Conduit or null (if the packet is to be discarded)
     */
    @RecipientList
    public List<String> ingresGatekeeper(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, String nodeKey) {
        LOG.debug(".ingresGatekeeper(): Enter, ingresPacket --> {}, nodeKey --> {}", ingresPacket, nodeKey);
        // Get my Petasos Context
        NodeElement node = topologyProxy.getNodeByKey(nodeKey);
        LOG.trace(".receiveFromWUP(): Node Element retrieved --> {}", node);
        NodeElementFunctionToken wupFunctionToken = node.getNodeFunctionToken();
        LOG.trace(".receiveFromWUP(): wupFunctionToken (NodeElementFunctionToken) for this activity --> {}", wupFunctionToken);
        // Now, continue with business logic
        RouteElementNames nameSet = new RouteElementNames(wupFunctionToken);
        ArrayList<String> targetList = new ArrayList<String>();
        LOG.trace(".ingresGatekeeper(): So, we will now determine if the Packet should be forwarded or discarded");
        if (ingresPacket.getCurrentJobCard().getIsToBeDiscarded()) {
            LOG.debug(".ingresGatekeeper(): Returning null, as message is to be discarded (isToBeDiscarded == true)");
            return (null);
        } else {
            LOG.trace(".ingresGatekeeper(): And we return the ingres point to the associated WUP Ingres Conduit");
            String targetEndpoint = nameSet.getEndPointWUPIngresConduitIngres();
            targetList.add(targetEndpoint);
            LOG.debug(".ingresGatekeeper(): Returning route to the WUP Ingres Conduit instance --> {}", targetEndpoint);
            return (targetList);
        }
    }
}
