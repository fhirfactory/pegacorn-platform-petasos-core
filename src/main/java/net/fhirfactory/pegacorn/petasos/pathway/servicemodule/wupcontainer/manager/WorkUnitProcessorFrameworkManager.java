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
package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.manager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.archetypes.ExternalIngresWUPContainerRoute;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.archetypes.StandardWUPContainerRoute;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;
import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;

/**
 *
 * @author Mark A. Hunter
 */

@ApplicationScoped
public class WorkUnitProcessorFrameworkManager {
    private static final Logger LOG = LoggerFactory.getLogger(WorkUnitProcessorFrameworkManager.class);

    @Inject
    TopologyIM topologyServer;

    @Inject
    TopicIM topicServer;
    
    public void buildWUPFramework(NodeElement element, Set<TopicToken> subscribedTopics, WUPArchetypeEnum wupArchetype){
        LOG.debug(".buildWUPFramework(): Entry, element --> {}, subscribedTopics --> {}, wupArchetype --> {}", element, subscribedTopics, wupArchetype);
        CamelContext camel = new DefaultCamelContext();
        try {
            switch (wupArchetype) {
                case WUP_NATURE_MESSAGE_WORKER:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_WORKER route");
                    StandardWUPContainerRoute standardWUPRoute = new StandardWUPContainerRoute(camel, element.getNodeFunctionToken(), element.getNodeInstanceID());
                    camel.addRoutes(standardWUPRoute);
                    LOG.trace(".buildWUPFramework(): Now subscribing this WUP/Route to UoW Content Topics");
                    uowTopicSubscribe(subscribedTopics, element.getNodeFunctionID());
                    break;
                case WUP_NATURE_API_PUSH:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_API_PUSH route");
                    break;
                case WUP_NATURE_API_ANSWER:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_API_ANSWER route");
                    break;
                case WUP_NATURE_API_RECEIVE:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_API_RECEIVE route");
                    break;
                case WUP_NATURE_MESSAGE_EXTERNAL_EGRESS_POINT:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_EXTERNAL_EGRESS_POINT route");
                    break;
                case WUP_NATURE_MESSAGE_EXTERNAL_INGRES_POINT:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_EXTERNAL_INGRES_POINT route");
                    ExternalIngresWUPContainerRoute ingresRoute = new ExternalIngresWUPContainerRoute(camel, element.getNodeFunctionToken(), element.getNodeInstanceID());
                    camel.addRoutes(ingresRoute);
                    LOG.trace(".buildWUPFramework(): Note, this type of WUP/Route does not subscribe to Topics (it is purely a producer)");
                    break;
                case WUP_NATURE_MESSAGE_EXTERNAL_CONCURRENT_INGRES_POINT:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_EXTERNAL_CONCURRENT_INGRES_POINT route");
            }
        } catch(Exception Ex){

        }

    }

    public void uowTopicSubscribe(Set<TopicToken> subscribedTopics, FDNToken wupTypeID){
        LOG.debug(".uowTopicSubscribe(): Entry, subscribedTopics --> {}, wupTypeID --> {}", subscribedTopics, wupTypeID );
        if(subscribedTopics.isEmpty()){
            LOG.debug(".uowTopicSubscribe(): Something's wrong, no Topics are subscribed for this WUP");
            return;
        }
        Iterator<TopicToken> topicIterator = subscribedTopics.iterator();
        while(topicIterator.hasNext()) {
            TopicToken currentTopicID = topicIterator.next();
            LOG.trace(".uowTopicSubscribe(): WUPType --> {} is subscribing to UoW Content Topic --> {}", wupTypeID, currentTopicID);
            topicServer.addTopicSubscriber(currentTopicID, wupTypeID );
        }
        LOG.debug(".uowTopicSubscribe(): Exit");
    }
}
