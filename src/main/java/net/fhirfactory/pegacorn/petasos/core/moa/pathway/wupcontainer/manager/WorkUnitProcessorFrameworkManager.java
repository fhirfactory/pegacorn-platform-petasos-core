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
package net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.manager;

import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.archetypes.ExternalEgressWUPContainerRoute;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.archetypes.ExternalIngresWUPContainerRoute;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.archetypes.StandardWUPContainerRoute;
import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;

/**
 * @author Mark A. Hunter
 */

@ApplicationScoped
public class WorkUnitProcessorFrameworkManager {
    private static final Logger LOG = LoggerFactory.getLogger(WorkUnitProcessorFrameworkManager.class);

    @Inject
    CamelContext camelctx;

    @Inject
    DeploymentTopologyIM topologyServer;

    @Inject
    TopicIM topicServer;

    public void buildWUPFramework(NodeElement wupNode, Set<TopicToken> subscribedTopics, WUPArchetypeEnum wupArchetype) {
        LOG.debug(".buildWUPFramework(): Entry, wupNode --> {}, subscribedTopics --> {}, wupArchetype --> {}", wupNode, subscribedTopics, wupArchetype);
        try {
            switch (wupArchetype) {

                case WUP_NATURE_LAODN_STIMULI_TRIGGERED_BEHAVIOUR: {
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_STIMULI_TRIGGERED_BEHAVIOUR route");
                    StandardWUPContainerRoute standardWUPRoute = new StandardWUPContainerRoute(camelctx, wupNode, true);
                    LOG.trace(".buildWUPFramework(): Route created, now adding it to he CamelContext!");
                    camelctx.addRoutes(standardWUPRoute);
                    LOG.trace(".buildWUPFramework(): Now subscribing this WUP/Route to UoW Content Topics");
                    uowTopicSubscribe(subscribedTopics, wupNode);
                    LOG.trace(".buildWUPFramework(): Subscribed to Topics, work is done!");
                    break;
                }
                case WUP_NATURE_LADON_TIMER_TRIGGERED_BEHAVIOUR: {
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_LADON_TIMER_TRIGGERED_BEHAVIOUR route");
                    ExternalIngresWUPContainerRoute ingresRoute = new ExternalIngresWUPContainerRoute(camelctx, wupNode);
                    camelctx.addRoutes(ingresRoute);
                    LOG.trace(".buildWUPFramework(): Note, this type of WUP/Route does not subscribe to Topics (it is purely a producer)");
                    break;
                }
                case WUP_NATURE_LADON_BEHAVIOUR_WRAPPER:
                case WUP_NATURE_LADON_STANDARD_MOA: {
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_LADON_STANDARD_MOA route");
                    StandardWUPContainerRoute standardWUPRoute = new StandardWUPContainerRoute(camelctx, wupNode, true);
                    LOG.trace(".buildWUPFramework(): Route created, now adding it to he CamelContext!");
                    camelctx.addRoutes(standardWUPRoute);
                    LOG.trace(".buildWUPFramework(): Now subscribing this WUP/Route to UoW Content Topics");
                    uowTopicSubscribe(subscribedTopics, wupNode);
                    LOG.trace(".buildWUPFramework(): Subscribed to Topics, work is done!");
                    break;
                }
                case WUP_NATURE_MESSAGE_WORKER: {
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_WORKER route");
                    StandardWUPContainerRoute standardWUPRoute = new StandardWUPContainerRoute(camelctx, wupNode);
                    LOG.trace(".buildWUPFramework(): Route created, now adding it to he CamelContext!");
                    camelctx.addRoutes(standardWUPRoute);
                    LOG.trace(".buildWUPFramework(): Now subscribing this WUP/Route to UoW Content Topics");
                    uowTopicSubscribe(subscribedTopics, wupNode);
                    LOG.trace(".buildWUPFramework(): Subscribed to Topics, work is done!");
                    break;
                }
                case WUP_NATURE_API_PUSH:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_API_PUSH route");
                    ExternalIngresWUPContainerRoute ingresRouteForAPIPush = new ExternalIngresWUPContainerRoute(camelctx, wupNode);
                    camelctx.addRoutes(ingresRouteForAPIPush);
                    break;
                case WUP_NATURE_API_ANSWER:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_API_ANSWER route");
                    break;
                case WUP_NATURE_API_RECEIVE:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_API_RECEIVE route");
                    break;
                case WUP_NATURE_MESSAGE_EXTERNAL_EGRESS_POINT:
                case WUP_NATURE_API_CLIENT:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_EXTERNAL_EGRESS_POINT route");
                    ExternalEgressWUPContainerRoute egressRoute = new ExternalEgressWUPContainerRoute(camelctx, wupNode);
                    camelctx.addRoutes(egressRoute);
                    LOG.trace(".buildWUPFramework(): Now subscribing this WUP/Route to UoW Content Topics");
                    uowTopicSubscribe(subscribedTopics, wupNode);
                    LOG.trace(".buildWUPFramework(): Subscribed to Topics, work is done!");
                    break;
                case WUP_NATURE_MESSAGE_EXTERNAL_INGRES_POINT:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_EXTERNAL_INGRES_POINT route");
                    ExternalIngresWUPContainerRoute ingresRoute = new ExternalIngresWUPContainerRoute(camelctx, wupNode);
                    camelctx.addRoutes(ingresRoute);
                    LOG.trace(".buildWUPFramework(): Note, this type of WUP/Route does not subscribe to Topics (it is purely a producer)");
                    break;
                case WUP_NATURE_MESSAGE_EXTERNAL_CONCURRENT_INGRES_POINT:
                    LOG.trace(".buildWUPFramework(): Building a WUP_NATURE_MESSAGE_EXTERNAL_CONCURRENT_INGRES_POINT route");
            }
        } catch (Exception Ex) {
            // TODO We really must handle this exception, either by cancelling the whole Processing Plant or, at least, raising an alarm
        }

    }

    public void uowTopicSubscribe(Set<TopicToken> subscribedTopics, NodeElement wupNode) {
        LOG.debug(".uowTopicSubscribe(): Entry, subscribedTopics --> {}, wupNode --> {}", subscribedTopics, wupNode);
        if (subscribedTopics.isEmpty()) {
            LOG.debug(".uowTopicSubscribe(): Something's wrong, no Topics are subscribed for this WUP");
            return;
        }
        NodeElementFunctionToken wupFunctionToken = new NodeElementFunctionToken();
        wupFunctionToken.setFunctionID(wupNode.getNodeFunctionID());
        wupFunctionToken.setVersion(wupNode.getVersion());
        Iterator<TopicToken> topicIterator = subscribedTopics.iterator();
        while (topicIterator.hasNext()) {
            TopicToken currentTopicID = topicIterator.next();
            LOG.trace(".uowTopicSubscribe(): wupNode --> {} is subscribing to UoW Content Topic --> {}", wupNode, currentTopicID);
            topicServer.addTopicSubscriber(currentTopicID, wupNode.getNodeInstanceID());
        }
        LOG.debug(".uowTopicSubscribe(): Exit");
    }
}
