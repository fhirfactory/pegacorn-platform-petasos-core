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

package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.interchange.worker;

import java.time.Instant;
import java.util.Date;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayloadSet;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.fhirfactory.pegacorn.common.model.RDN;

public class InterchangeUoWPayload2NewUoWProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(InterchangeUoWPayload2NewUoWProcessor.class);

    public List<UoW> extractUoWPayloadAndCreateNewUoWSet(UoW incomingUoW, Exchange camelExchange) {
        LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): Entry, incomingUoW --> {}, camelExchange --> ###", incomingUoW);
        LinkedList<UoW> newUoWSet = new LinkedList<UoW>();
        UoWPayloadSet egressPayloadSet = incomingUoW.getEgressContent();
        Iterator<UoWPayload> incomingPayloadIterator = egressPayloadSet.getPayloadElements().iterator();
        while(incomingPayloadIterator.hasNext()){
            UoWPayload currentPayload = incomingPayloadIterator.next();
            FDN newUoWFDN = new FDN(currentPayload.getPayloadTopicID().getIdentifier());
            newUoWFDN.appendRDN(new RDN("Version", currentPayload.getPayloadTopicID().getVersion()));
            newUoWFDN.appendRDN(new RDN("Instance", Date.from(Instant.now()).toString()));
            UoW newUoW = new UoW(newUoWFDN.getToken(), currentPayload);
            newUoWSet.add(newUoW);
            if(LOG.isTraceEnabled()){
                LOG.trace(".extractUoWPayloadAndCreateNewUoWSet(): New UoW Create --> {}", newUoW);
            }
        }
        if(LOG.isDebugEnabled()){
            LOG.debug(".extractUoWPayloadAndCreateNewUoWSet(): Exit, new UoWs created, number --> {} ", newUoWSet.size());
        }
        return(newUoWSet);
    }
}
