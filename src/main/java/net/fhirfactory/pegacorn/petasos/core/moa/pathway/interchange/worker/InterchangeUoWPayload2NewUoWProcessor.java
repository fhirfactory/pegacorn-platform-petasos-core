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

package net.fhirfactory.pegacorn.petasos.core.moa.pathway.interchange.worker;

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayloadSet;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Dependent
public class InterchangeUoWPayload2NewUoWProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(InterchangeUoWPayload2NewUoWProcessor.class);

    @Inject
    DeploymentTopologyIM topologyProxy;
    
    /**
     * This method performs tree key tasks:
     * 
     * 1. It extracts each UoWPayload from the egressPayloadSet within the incomingUoW and creates a 
     * new UoW (and, subsequently, a new WorkUnitTransportPacket) based on the content of those egress 
     * UoWPayload elements. 
     * 2. As part of the WorkUnitTransportPacket creation, it embeds the current ActivityID.
     * 3. It then returns a List<> of these new WorkUnitTransportPackets for distribution.
     * 
     * It generates the 
     * @param ingresPacket
     * @param camelExchange
     * @param wupInstanceKey
     * @return A List<> of WorkUnitTransportPackets - one for each egress UoWPayload element within the incoming UoW.
     */

    public List<WorkUnitTransportPacket> extractUoWPayloadAndCreateNewUoWSet(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, String wupInstanceKey) {
        LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): Entry, ingresPacket (WorkUnitTransportPacket) --> {}, wupInstanceKey (String) --> {}", ingresPacket, wupInstanceKey);
        // Get my Petasos Context
        NodeElement node = topologyProxy.getNodeByKey(wupInstanceKey);
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
        }
        LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): Exit, new WorkUnitTransportPackets created, number --> {} ", newEgressTransportPacketSet.size());

        return (newEgressTransportPacketSet);
    }
}
