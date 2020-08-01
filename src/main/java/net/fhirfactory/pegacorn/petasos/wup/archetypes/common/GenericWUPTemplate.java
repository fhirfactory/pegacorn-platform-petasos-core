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
package net.fhirfactory.pegacorn.petasos.wup.archetypes.common;

import java.util.Set;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.wup.PetasosServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.topology.manager.proxies.ServiceModuleTopologyProxy;

public abstract class GenericWUPTemplate extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GenericWUPTemplate.class);

    private FDNToken wupFunctionID;
    private FDNToken wupInstanceID;
    private WUPJobCard wupInstanceJobCard;
    private NodeElement wupTopologyElement;
    private String wupEgressPoint = null;
    private String wupIngresPoint = null;

    @Inject
    protected ServiceModuleTopologyProxy topologyServer;

    @Inject
    protected PetasosServicesBroker servicesBroker;

    public GenericWUPTemplate() {
        super();
        LOG.debug(".GenericWUPTemplate(): Entry, Default constructor");
        getInstanceID();
        getFunctionTypeID();
        buildWUPFramework();
    }

    public void getFunctionTypeID() {
        LOG.debug(".getFunctionTypeID(): Entry");
        this.wupFunctionID = topologyServer.getWUPFunctionID(this.wupInstanceID);
        LOG.debug(".getFunctionTypeID(): Exit, created wupTypeID --> ", this.wupFunctionID);
    }

    private void getInstanceID() {
        LOG.debug(".buildInstanceID(): Entry");
        this.wupInstanceID = topologyServer.getWUPInstanceID(getWUPInstanceName());
        LOG.debug(".buildInstanceID(): Exit, created wupInstanceID --> {}", this.wupInstanceID);
    }
    
    private void registerTopologyElementInstantiation(){
        LOG.debug(".registerTopologyElementInstantiation(): Entry");
        topologyServer.setInstanceInPlace(wupInstanceID, true);
        LOG.debug(".registerTopologyElementInstantiation(): Exit");
    }

    private void buildWUPFramework() {
        LOG.debug(".buildWUPFramework(): Entry");
        servicesBroker.registerWorkUnitProcessor(this.wupTopologyElement, this.getSubscribedTopics(), this.getWUPArchitype());
        LOG.debug(".buildWUPFramework(): Exit");
    }

    public FDNToken getWupFunctionID() {
        return (this.wupFunctionID);
    }

    public FDNToken getWupInstanceID() {
        return (this.wupInstanceID);
    }

    protected String ingresFeed() {
        return (this.wupIngresPoint);
    }

    protected String egressFeed() {
        return (this.wupEgressPoint);
    }

    // To be implemented methods (in Specialisations)
    
    public abstract Set<TopicToken> getSubscribedTopics();
    public abstract String getWUPInstanceName();
    public abstract WUPArchetypeEnum getWUPArchitype();
   
}
