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

package net.fhirfactory.pegacorn.petasos.pathway.interchange.worker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.deployment.topology.manager.ServiceModuleTopologyProxy;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayloadSet;

public class InterchangeUoWPayload2NewUoWProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(InterchangeUoWPayload2NewUoWProcessor.class);

    @Inject
    ServiceModuleTopologyProxy topologyProxy;

    public List<WorkUnitTransportPacket> extractUoWPayloadAndCreateNewUoWSet(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, String wupInstanceKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): Entry");
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousParcelInstance -->{}", ingresPacket.getCurrentJobCard().getCardID().getPreviousParcelIdentifier());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUAEpisodeID --> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousEpisodeIdentifier());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUPFunctionTokan --> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousWUPFunctionToken());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).perviousWUPIdentifier--> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousWUPIdentifier());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentParcelIdentifier -->{}", ingresPacket.getCurrentJobCard().getCardID().getPresentParcelIdentifier());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentEpisodeIdentifier --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentEpisodeIdentifier());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPFunctionTokan --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentWUPFunctionToken());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPIdentifier --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentWUPIdentifier());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContunuityID).createDate --> {}", ingresPacket.getCurrentJobCard().getCardID().getCreationDate());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).clusterMode (ConcurrencyModeEnum) -->{}", ingresPacket.getCurrentJobCard().getClusterMode());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).currentStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getCurrentStatus());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).grantedStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getGrantedStatus());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).toBeDiscarded (boolean) --> {}", ingresPacket.getCurrentJobCard().getIsToBeDiscarded());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).requestedStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getRequestedStatus());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).systemMode (ResilienceModeEnum) --> {}", ingresPacket.getCurrentJobCard().getSystemMode());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).updateDate (Date) --> {}", ingresPacket.getCurrentJobCard().getUpdateDate());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).currentParcelStatus (ParcelStatusElement).parcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ingresPacket.getCurrentParcelStatus().getParcelStatus());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): wupInstanceKey (String) --> {}", wupInstanceKey);
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).instanceID --> {}", ingresPacket.getPayload().getInstanceID());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).typeID --> {}", ingresPacket.getPayload().getTypeID());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", ingresPacket.getPayload().getPayloadTopicID());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).ingresContent --> {}", ingresPacket.getPayload().getIngresContent());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).egressContent --> {}", ingresPacket.getPayload().getEgressContent());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", ingresPacket.getPayload().getPayloadTopicID());
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).processingOutcome --> {}", ingresPacket.getPayload().getProcessingOutcome());
        }
        // Get my Petasos Context
        NodeElement node = topologyProxy.getNodeByKey(wupInstanceKey);
        if (LOG.isTraceEnabled()) {
            LOG.trace(".extractUoWPayloadAndCreateNewUoWSet{}: Retrieved node from TopologyProxy");
            Iterator<String> listIterator = node.debugPrint(".extractUoWPayloadAndCreateNewUoWSet(): node").iterator();
            while (listIterator.hasNext()) {
                LOG.trace(listIterator.next());
            }
        }
        UoW incomingUoW = ingresPacket.getPayload();
        if (LOG.isDebugEnabled()) {
            UoWPayloadSet egressContent = incomingUoW.getEgressContent();
            Iterator<UoWPayload> incomingPayloadIterator = egressContent.getPayloadElements().iterator();
            int counter = 0;
            while (incomingPayloadIterator.hasNext()) {
                UoWPayload payload = incomingPayloadIterator.next();
                LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): payload (UoWPayload).PayloadTopic --> [{}] {}", counter, payload.getPayloadTopicID());
                LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): payload (UoWPayload).Payload --> [{}] {}", counter, payload.getPayload());
                counter++;
            }
        }
        ArrayList<WorkUnitTransportPacket> newEgressTransportPacketSet = new ArrayList<WorkUnitTransportPacket>();
        UoWPayloadSet egressPayloadSet = incomingUoW.getEgressContent();
        Iterator<UoWPayload> incomingPayloadIterator = egressPayloadSet.getPayloadElements().iterator();
        while (incomingPayloadIterator.hasNext()) {
            UoWPayload currentPayload = incomingPayloadIterator.next();
            UoW newUoW = new UoW(currentPayload);
            WorkUnitTransportPacket transportPacket = new WorkUnitTransportPacket(ingresPacket.getPacketID(), Date.from(Instant.now()), newUoW);
            newEgressTransportPacketSet.add(transportPacket);
            if (LOG.isTraceEnabled()) {
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): New WorkUnitTransportPacket created: ");
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).previousParcelInstance -->{}", transportPacket.getPacketID().getPreviousParcelIdentifier());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).previousWUAEpisodeID --> {}", transportPacket.getPacketID().getPreviousEpisodeIdentifier());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).previousWUPFunctionTokan --> {}", transportPacket.getPacketID().getPreviousWUPFunctionToken());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).previousWUPIdentifier--> {}", transportPacket.getPacketID().getPreviousWUPIdentifier());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).presentParcelIdentifier -->{}", transportPacket.getPacketID().getPresentParcelIdentifier());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).presentEpisodeIdentifier --> {}", transportPacket.getPacketID().getPresentEpisodeIdentifier());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).presentWUPFunctionTokan --> {}", transportPacket.getPacketID().getPresentWUPFunctionToken());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).packetID (ContinuityID).presentWUPIdentifier --> {}", transportPacket.getPacketID().getPresentWUPIdentifier());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).instanceID --> {}", transportPacket.getPayload().getInstanceID());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).typeID --> {}", transportPacket.getPayload().getTypeID());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", transportPacket.getPayload().getPayloadTopicID());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).ingresContent --> {}", transportPacket.getPayload().getIngresContent());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).egressContent --> {}", transportPacket.getPayload().getEgressContent());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", transportPacket.getPayload().getPayloadTopicID());
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).processingOutcome --> {}", transportPacket.getPayload().getProcessingOutcome());
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): Exit, new WorkUnitTransportPackets created, number --> {} ", newEgressTransportPacketSet.size());
        }
        return (newEgressTransportPacketSet);
    }
}
