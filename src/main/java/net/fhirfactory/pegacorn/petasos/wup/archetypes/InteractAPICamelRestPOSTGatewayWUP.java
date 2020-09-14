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
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public abstract class InteractAPICamelRestPOSTGatewayWUP extends GenericMOAWUPTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(InteractAPICamelRestPOSTGatewayWUP.class);

    public InteractAPICamelRestPOSTGatewayWUP() {
        super();
    }

    abstract protected String specifyIngresEndpointRESTProviderComponent();
    abstract protected String specifyIngresEndpointPayloadEncapsulationType();
    abstract protected String specifyIngresEndpointScheme();
    abstract protected String specifyIngresEndpointContextPath();

    @Override
    protected WUPArchetypeEnum specifyWUPArchetype(){
        return(WUPArchetypeEnum.WUP_NATURE_MESSAGE_EXTERNAL_INGRES_POINT);
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
        LOG.debug(".specifyEgressEndpoint(): Entry");
        String endpoint = this.getNameSet().getEndPointWUPEgress();
        LOG.debug(".specifyEgressEndpoint(): Exit, egressEndPoint --> {}", endpoint);
        return(endpoint);
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

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedIngresEndpoint() {
        return(false);
    }
}
