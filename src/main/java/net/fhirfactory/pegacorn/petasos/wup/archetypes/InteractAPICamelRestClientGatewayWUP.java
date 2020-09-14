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
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class InteractAPICamelRestClientGatewayWUP extends GenericMOAWUPTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(InteractAPICamelRestClientGatewayWUP.class);

    private Set<EndpointElement> egressEndpointElements;

    public InteractAPICamelRestClientGatewayWUP() {
        super();
//        LOG.debug(".MessagingIngresGatewayWUP(): Entry, Default constructor");
    }

    protected abstract String specifyEgressEndpointName();
    protected abstract String specifyEgressEndpointVersion();
    protected abstract boolean isRemote();

    @Override
    protected WUPArchetypeEnum specifyWUPArchetype(){
        return(WUPArchetypeEnum.WUP_NATURE_MESSAGE_EXTERNAL_EGRESS_POINT);
    }
    
    @Override
    protected String specifyIngresEndpoint(){
        LOG.debug(".specifyIngresEndpoint(): Entry");
        String endpointName = this.getNameSet().getEndPointWUPIngres();
        LOG.debug(".specifyIngresEndpoint(): Exit, ingresEndPoint --> {}", endpointName);
        return(endpointName);
    }

    abstract protected String specifyEgressEndpointRESTProviderComponent();
    abstract protected String specifyEgressEndpointPayloadEncapsulationType();
    abstract protected String specifyEgressEndpointScheme();
    abstract protected String specifyEgressEndpointContextPath();

    @Override
    protected String specifyIngresTopologyEndpointName() {
        return (null);
    }

    @Override
    protected String specifyIngresEndpointVersion() {
        return (null);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedIngresEndpoint() {
        return (true);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedEgressEndpoint() {
        return (false);
    }
}
