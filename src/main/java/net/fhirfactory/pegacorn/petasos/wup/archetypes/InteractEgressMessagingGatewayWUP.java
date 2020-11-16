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

import net.fhirfactory.pegacorn.petasos.core.moa.wup.GenericMOAWUPTemplate;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InteractEgressMessagingGatewayWUP extends GenericMOAWUPTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(InteractEgressMessagingGatewayWUP.class);


    public InteractEgressMessagingGatewayWUP() {
        super();
//        LOG.debug(".MessagingIngresGatewayWUP(): Entry, Default constructor");
    }

    @Override
    protected WUPArchetypeEnum specifyWUPArchetype(){
        return(WUPArchetypeEnum.WUP_NATURE_MESSAGE_EXTERNAL_EGRESS_POINT);
    }
    
    @Override
    protected String specifyEgressEndpoint(){
        LOG.debug(".specifyEgressEndpoint(): Entry");
        String ingresEndPoint;
        ingresEndPoint = specifyEndpointComponentDefinition();
        ingresEndPoint = ingresEndPoint + ":";
        ingresEndPoint = ingresEndPoint + this.specifyEndpointProtocol();
        ingresEndPoint = ingresEndPoint + this.specifyEndpointProtocolLeadIn();
        ingresEndPoint = ingresEndPoint + this.getIngresTopologyEndpointElement().getHostname();
        ingresEndPoint = ingresEndPoint + ":" + this.getIngresTopologyEndpointElement().getExposedPort();
        ingresEndPoint = ingresEndPoint + specifyEndpointProtocolLeadout();
        LOG.debug(".specifyIngresEndpoint(): Exit, ingresEndPoint --> {}", ingresEndPoint);
        return(ingresEndPoint);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedIngresEndpoint(){
        return(true);
    }

    @Override
    protected String specifyIngresEndpointVersion() {
        return null;
    }

    @Override
    protected String specifyIngresTopologyEndpointName() {
        return null;
    }

    @Override
    protected String specifyIngresEndpoint(){
        LOG.debug(".specifyEgressEndpoint(): Entry");
        String endpoint = this.getNameSet().getEndPointWUPEgress();
        LOG.debug(".specifyEgressEndpoint(): Exit, egressEndPoint --> {}", endpoint);
        return(endpoint);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedEgressEndpoint() {
        return(false);
    }

    abstract protected String specifyEndpointComponentDefinition();
    abstract protected String specifyEndpointProtocol();
    abstract protected String specifyEndpointProtocolLeadIn();
    abstract protected String specifyEndpointProtocolLeadout();
    abstract protected String specifyEgressTopologyEndpointName();
    abstract protected String specifyEgressEndpointVersion();
}
