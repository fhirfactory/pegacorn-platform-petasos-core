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

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.FDNTokenSet;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;

import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;

/**
 * @author Mark A. Hunter
 */
public class InterchangeTargetWUPTypeRouter {

    private static final Logger LOG = LoggerFactory.getLogger(InterchangeTargetWUPTypeRouter.class);

    private static final String CURRENT_END_POINT_SET = "CurrentEndpointSetFor";

    @Inject
    TopicIM distributionList;

    /**
     * Essentially, we get the set of WUPs subscribing to a particular UoW type,
     * create a property within the CamelExchange and then we use that Property
     * as a mechanism of keeping track of who we have already forwarded the UoW
     * to. Once we've cycled through all the targets (subscribers), we return
     * null.
     *
     * @param incomingPacket Incoming UoW that will be distributed to all
     * Subscribed WUPs
     * @param camelExchange The Apache Camel Exchange instance associated with
     * this route.
     * @return An endpoint (name) for a recipient for the incoming UoW
     */
    String forwardUoW2WUPs(WorkUnitTransportPacket incomingPacket, Exchange camelExchange, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID) {
        LOG.debug(".forwardUoW2WUPs(): Entry, incomingTraffic --> {}, camelExchange --> ###, wupFunctionToken --> {}, wupInstanceID --> {}", incomingPacket, wupFunctionToken, wupInstanceID);
        TopicToken uowTopicID = null;
        if(incomingPacket.hasPayload()){
            uowTopicID = incomingPacket.getPayload().getPayloadTopicID();
        } else {
            LOG.debug(".forwardUoW2WUPs(): Exit, there's no payload (UoW), so return null (and end this route).");
            return(null);
        }
        FDN currentUoWFDN = new FDN(uowTopicID.getIdentifier());
        String propertyName = CURRENT_END_POINT_SET + currentUoWFDN.getToken().toString();
        LOG.trace(".forwardUoW2WUPs(): This instance's Subscribed WUP List is called --> {}", propertyName);
        EndpointNameSet targetWUPSet = camelExchange.getProperty(propertyName, EndpointNameSet.class);
        
        if (LOG.isTraceEnabled()) {tracePrintSubscribedWUPSet(targetWUPSet);}

        boolean alreadyInstalled = true;
        if (targetWUPSet == null) {
            alreadyInstalled = false;
            targetWUPSet = populateNameSet(uowTopicID);
            if (targetWUPSet == null) {
                LOG.debug(".forwardUoW2WUPs(): Exit, nobody was interested in processing this UoW and that's a concern!");
                return (null);
            }
        }
        if (targetWUPSet.isEmpty()) {
            camelExchange.removeProperty(propertyName);
            LOG.debug(".forwardUoW2WUPs(): Exit, finished iterating through interested/registered endpoints");
            return (null);
        }
        if (alreadyInstalled) {
            camelExchange.removeProperty(propertyName);
        }
        String thisOne = targetWUPSet.getNameSet().iterator().next();
        targetWUPSet.removeEndpointName(thisOne);
        camelExchange.setProperty(propertyName, targetWUPSet);
        LOG.debug(".forwardUoW2WUPs(): Exiting, returning another registered/interested endpoint: endpointDetail -->{}", thisOne);
        return (thisOne);
    }

    private EndpointNameSet populateNameSet(TopicToken uowTopicID) {
        Set<NodeElement> nodeSet = distributionList.getSubscriberSet(uowTopicID);
        if(nodeSet == null){
            return(null);
        }
        if(nodeSet.isEmpty()){
            return(null);
        }
        EndpointNameSet nameSet = new EndpointNameSet();
        Iterator<NodeElement> nodeIterator = nodeSet.iterator();
        while(nodeIterator.hasNext()){
            NodeElement currentNode = nodeIterator.next();
            NodeElementFunctionToken currentNodeFunctionToken = currentNode.getNodeFunctionToken();
            RouteElementNames routeName = new RouteElementNames(currentNodeFunctionToken);
            nameSet.addEndpointName(routeName.getEndPointWUPContainerIngresProcessorIngres());
        }
        return(nameSet);
    }

    private void tracePrintSubscribedWUPSet(EndpointNameSet wupSet) {
        LOG.trace(".forwardUoW2WUPs(): We've already commenced publishing this UoW, WUPs remaining --> {}", wupSet.getNameSet().size());
        Iterator<String> tokenIterator = wupSet.getNameSet().iterator();
        while (tokenIterator.hasNext()) {
            LOG.trace(".forwardUoW2WUPs(): Subscribed WUP Ingres Point --> {}", tokenIterator.next());
        }
    }

    private void updateServiceModuleMap(FDNToken associatedWUP, FDNToken targetIngresPoint) {

    }
}
