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

package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.buildingblocks;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark A. Hunter
 * @since 2020-07-01
 */
public class WUPContainerEgressGatekeeper {
    private static final Logger LOG = LoggerFactory.getLogger(WUPContainerEgressGatekeeper.class);
    private static final String EGRESS_GATEKEEPER_PROCESSED_PROPERTY = "EgressGatekeeperSemaphore";

    /**
     * This class/method checks the status of the WUPJobCard for the parcel, and ascertains if it is to be
     * discarded (because of some processing error or due to the fact that the processing has occurred already
     * within another WUP). At the moment, it reaches the "discard" decisions purely by checking the
     * WUPJobCard.isToBeDiscarded boolean.
     *
     * @param ingresPacket The WorkUnitTransportPacket that is to be forwarded to the Intersection (if all is OK)
     * @param camelExchange The Apache Camel Exchange object, used to store a Semaphore as we iterate through Dynamic Route options
     * @param wupFunctionToken The Work Unit Processor Type: should be unique with the SystemModule and is used to build routes around the core WUP
     * @param wupInstanceID The Work Unit Processor Instance: only to be used for instance debugging (not used at the moment)
     * @return Should either return the ingres point into the associated Interchange Payload Transformer or null (if the packet is to be discarded)
     */
    public String egressGatekeeper(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID) {
        LOG.debug(".egressGatekeeper(): Enter,  ingresPacket --> {}, wupFunctionToken --> {}, wupInstanceID --> {}", ingresPacket, wupFunctionToken, wupInstanceID);
        RouteElementNames nameSet = new RouteElementNames(wupFunctionToken);
        if (camelExchange.getProperty(EGRESS_GATEKEEPER_PROCESSED_PROPERTY) == null) {
            LOG.trace(".egressGatekeeper(): No semaphore exists within the Camel Exchange, so this is the 1st time through the Router logic");
            LOG.trace(".egressGatekeeper(): So, we will now determine if the Packet should be forwarded or discarded");
            if(ingresPacket.getCurrentJobCard().getIsToBeDiscarded()){
                LOG.trace(".egressGatekeeper(): The isToBeDiscarded attribute is true, so we return null (and discard the packet");
                LOG.debug(".egressGatekeeper(): Returning null, as message is to be discarded (isToBeDiscarded == true)");
                return(null);
            } else {
                LOG.trace(".egressGatekeeper(): the isToBeDiscarded attribute is false, so we need to set the Semaphore (so we know we've processed this packet)");
                camelExchange.setProperty(EGRESS_GATEKEEPER_PROCESSED_PROPERTY, true);
                LOG.trace(".egressGatekeeper(): And we return the ingres point of the associated Interchange Payload Transformer");
                String targetEndpoint = nameSet.getEndPointInterchangePayloadTransformerIngres();
                LOG.debug(".egressGatekeeper(): Returning route to the Interchange Payload Transformer instance --> {}", targetEndpoint);
                return (targetEndpoint);
            }
        } else {
            LOG.trace("egressGatekeeper(): the semaphore exists, and the isToBeDiscarded attribute is false, which means we've done our Routing to the Interchange Payload Transformer");
            LOG.trace("egressGatekeeper(): so now we should just remove the semaphore and then return null to indicate that there are no other endpoints to route to");
            camelExchange.removeProperty(EGRESS_GATEKEEPER_PROCESSED_PROPERTY);
            LOG.debug(".egressGatekeeper(): Exit, finished routing, so just return null");
            return (null);
        }
    }
}
