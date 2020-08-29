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

package net.fhirfactory.pegacorn.petasos.wup.archetypes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.GenericMOAWUPTemplate;

public abstract class InteractIngresMessagingGatewayWUP extends GenericMOAWUPTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(InteractIngresMessagingGatewayWUP.class);
    
    private EndpointElement ingresEndpointElement;
    
    public InteractIngresMessagingGatewayWUP() {
        super();
//        LOG.debug(".MessagingIngresGatewayWUP(): Entry, Default constructor");
    }

    @Override
    public WUPArchetypeEnum specifyWUPArchetype(){
        return(WUPArchetypeEnum.WUP_NATURE_MESSAGE_EXTERNAL_INGRES_POINT);
    }
    
    @Override
    public String specifyIngresEndpoint(){
        LOG.debug(".specifyIngresEndpoint(): Entry");
        this.specifyIngresEndpointElement();
        LOG.trace(".specifyIngresEndpoint(): Retrieved EndpointElement --> {}", this.ingresEndpointElement);
        String ingresEndPoint;
        ingresEndPoint = getEndpointComponentDefinition();
        ingresEndPoint = ingresEndPoint + ":";
        ingresEndPoint = ingresEndPoint + this.getEndpointProtocol();
        ingresEndPoint = ingresEndPoint + this.getEndpointProtocolLeadIn();
        ingresEndPoint = ingresEndPoint + this.ingresEndpointElement.getHostname();
        ingresEndPoint = ingresEndPoint + ":" + this.ingresEndpointElement.getInternalPort();
        ingresEndPoint = ingresEndPoint + getEndpointProtocolLeadout();
        LOG.debug(".specifyIngresEndpoint(): Exit, ingresEndPoint --> {}", ingresEndPoint);
        return(ingresEndPoint);
    } 
    
    @Override
    public String specifyEgressEndpoint(){
        LOG.debug(".specifyEgressEndpoint(): Entry");
        String endpointName = this.getNameSet().getEndPointWUPEgress();
        LOG.debug(".specifyEgressEndpoint(): Exit, egressEndPoint --> {}", endpointName);
        return(endpointName);
    }

    /**
     * Get's the Endpoint associated with the WUP. The assumption is that
     * there is only ever a single endpoint associated with a WUP. This may 
     * break in later releases.
     * 
     * @return EndpointElement for the associated Endpoint to the WUP. 
     */
    protected void specifyIngresEndpointElement(){
        LOG.debug(".getEndpoint(): Entry");
        NodeElementIdentifier nodeID = new NodeElementIdentifier(this.getWupIdentifier());
        NodeElement node = this.getTopologyServer().getNode(nodeID);
        Set<EndpointElementIdentifier> endpoints = node.getEndpoints();
        if (endpoints.size() > 1) {
            throw new RuntimeException("Not yet implemented.  The current code only supports a single node and needs to be updated to handle multiple endpoints");
        }
        Iterator<EndpointElementIdentifier> endpointIterator = endpoints.iterator();
        // we are still being brave
        EndpointElementIdentifier endpointToken = endpointIterator.next();
        // we are ludicrously brave
        EndpointElement endpoint = getTopologyServer().getEndpoint(endpointToken);
        // the stars align!!!!!
        LOG.debug(".getEndpoint(): Exit, endpoint --> {}", endpoint);
        this.ingresEndpointElement = endpoint;
    }
    
 
    /**
     * The Ingres Message Gateway doesn't subscribe to ANY topics as it receives it's 
     * input from an external system.
     * 
     * @return An empty Set<TopicToken>
     */
    @Override
    public Set<TopicToken> specifySubscriptionTopics() {
        HashSet<TopicToken> subTopics = new HashSet<TopicToken>();
        return(subTopics);
    }
    
    abstract protected String getEndpointComponentDefinition();
    abstract protected String getEndpointProtocol();
    abstract protected String getEndpointProtocolLeadIn();
    abstract protected String getEndpointProtocolLeadout();
}
