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
import java.util.Set;

import net.fhirfactory.pegacorn.petasos.core.moa.wup.GenericMOAWUPTemplate;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;

public abstract class EdgeIngresMessagingGatewayWUP extends GenericMOAWUPTemplate {
    
    private static final String DEFAULT_NETTY_PARAMS_POSTFIX = "&sync=true&disconnect=true&keepAlive=false&receiveBufferSize=" + IPC_PACKET_MAXIMUM_FRAME_SIZE;
    
    public EdgeIngresMessagingGatewayWUP() {
        super();
//        getLogger().debug(".MessagingIngresGatewayWUP(): Entry, Default constructor");
    }

    @Override
    protected abstract String specifyIngresTopologyEndpointName();
    @Override
    protected abstract String specifyIngresEndpointVersion();

    @Override
    protected WUPArchetypeEnum specifyWUPArchetype(){
        return(WUPArchetypeEnum.WUP_NATURE_MESSAGE_EXTERNAL_INGRES_POINT);
    }
    
    @Override
    protected String specifyIngresEndpoint(){
        getLogger().debug(".specifyIngresEndpoint(): Entry");
        String ingresEndPoint;
        ingresEndPoint = specifyEndpointComponentDefinition();
        ingresEndPoint = ingresEndPoint + ":";
        ingresEndPoint = ingresEndPoint + this.specifyEndpointProtocol();
        ingresEndPoint = ingresEndPoint + this.specifyEndpointProtocolLeadIn();
        ingresEndPoint = ingresEndPoint + "0.0.0.0"; // TODO need to look into why Wildfly wont bind....
        ingresEndPoint = ingresEndPoint + ":" + this.getIngresTopologyEndpointElement().getInternalPort();
        ingresEndPoint = ingresEndPoint + specifyEndpointProtocolLeadout();
        getLogger().debug(".specifyIngresEndpoint(): Exit, ingresEndPoint --> {}", ingresEndPoint);
        return(ingresEndPoint);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedEgressEndpoint(){
        return(true);
    }

    @Override
    protected String specifyEgressEndpointVersion() {
        return null;
    }

    @Override
    protected String specifyEgressTopologyEndpointName() {
        return null;
    }
    
    @Override
    protected String specifyEgressEndpoint(){
        getLogger().debug(".specifyEgressEndpoint(): Entry");
        String endpoint = this.getNameSet().getEndPointWUPEgress();
        getLogger().debug(".specifyEgressEndpoint(): Exit, egressEndPoint --> {}", endpoint);
        return(endpoint);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedIngresEndpoint() {
        return(false);
    }


    /**
     * The Ingres Message Gateway doesn't subscribe to ANY topics as it receives it's 
     * input from an external system.
     * 
     * @return An empty Set<TopicToken>
     */
    @Override
    protected Set<TopicToken> specifySubscriptionTopics() {
        HashSet<TopicToken> subTopics = new HashSet<TopicToken>();
        return(subTopics);
    }
    
    protected String specifyEndpointComponentDefinition() {
        return ("netty");
    }

    protected String specifyEndpointProtocol() {
        return ("tcp");
    }

    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    protected String specifyEndpointProtocolLeadout() {
        return specifyEndpointProtocolLeadout(specifyServerInitializerFactoryName());
    }

    public static String specifyEndpointProtocolLeadout(String serverInitializerFactoryName) {
        return "?serverInitializerFactory=#" + serverInitializerFactoryName + DEFAULT_NETTY_PARAMS_POSTFIX;
    }
    
    abstract protected String specifyServerInitializerFactoryName();
}
