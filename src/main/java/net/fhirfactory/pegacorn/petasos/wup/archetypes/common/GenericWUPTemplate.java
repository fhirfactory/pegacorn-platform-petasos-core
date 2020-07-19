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

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.wup.PetasosServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import net.fhirfactory.pegacorn.common.model.FDNTokenSet;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementInstanceTypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;

public abstract class GenericWUPTemplate extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GenericWUPTemplate.class);

    private FDNToken wupTypeID;
    private FDNToken wupInstanceID;
    private WUPJobCard wupInstanceJobCard;
    private NodeElement wupTopologyElement;
    private String wupEgressPoint = null;
    private String wupIngresPoint = null;

    @Inject
    TopologyIM topologyIM;

    @Inject
    public PetasosServicesBroker servicesBroker;

    public GenericWUPTemplate() {
        super();
        LOG.debug(".GenericWUPTemplate(): Entry, Default constructor");
        buildTypeID();
        buildInstanceID();
        registerTopologyElement();
        buildWUPFramework();
    }

    private void buildTypeID() {
        LOG.debug(".buildTypeID(): Entry");
        RDN serviceModuleTypeRDN = getServiceModuleTypeRDN();
        FDNToken serviceModuleTypeFDN = topologyIM.getServiceModuleContext(serviceModuleTypeRDN.getNameValue());
        FDN wupTypeFDN = new FDN(serviceModuleTypeFDN);
        wupTypeFDN.appendRDN(getProcessingPlantRDN());
        wupTypeFDN.appendRDN(getWorkUnitProcessorRDN());
        this.wupTypeID = wupTypeFDN.getToken();
        LOG.debug(".buildTypeID(): Exit, created wupTypeID --> ", this.wupTypeID);
    }

    private void buildInstanceID() {
        LOG.debug(".buildInstanceID(): Entry");
        RDN processingPlantRDN = getProcessingPlantRDN();
        RDN workUnitProcessorRDN = getWorkUnitProcessorRDN();
        RDN applicationServerRDN = getApplicationServerRDN();
        RDN siteRDN = getSiteRDN();
        RDN serviceRDN = getServiceRDN();
        RDN subsystemRDN = getSubSystemRDN();
        RDN serviceModuleTypeRDN = getServiceModuleTypeRDN();
        FDNToken solutionDeploymentID = topologyIM.getSolutionID();
        FDN wupInstanceFDN = new FDN(solutionDeploymentID);
        wupInstanceFDN.appendRDN(subsystemRDN);
        wupInstanceFDN.appendRDN(serviceRDN);
        wupInstanceFDN.appendRDN(siteRDN);
        wupInstanceFDN.appendRDN(applicationServerRDN);
        wupInstanceFDN.appendRDN(serviceModuleTypeRDN);
        wupInstanceFDN.appendRDN(processingPlantRDN);
        wupInstanceFDN.appendRDN(workUnitProcessorRDN);
        RDN instanceQualifier = new RDN("InstanceQualifier", Date.from(Instant.now()).toString());
        wupInstanceFDN.appendRDN(instanceQualifier);
        this.wupInstanceID = wupInstanceFDN.getToken();
        LOG.debug(".buildInstanceID(): Exit, created wupInstanceID --> {}", this.wupInstanceID);
    }
    
    private void registerTopologyElement(){
        LOG.debug(".registerTopologyElement(): Entry");
        NodeElement newElement = new NodeElement();
        newElement.setElementInstanceID(this.wupInstanceID);
        newElement.setElementFunctionTypeID(this.wupTypeID);
        newElement.setElementVersion(getVersion());
        newElement.setTopologyElementType(NodeElementInstanceTypeEnum.WUP_INSTANCE);
        FDN wupInstanceFDN = new FDN(this.wupInstanceID);
        FDN wupInstanceFDNWithoutInstanceQualifier = wupInstanceFDN.getParentFDN();
        FDN parentFDN = wupInstanceFDNWithoutInstanceQualifier.getParentFDN();
        newElement.setContainingElementID(parentFDN.getToken());
        wupTopologyElement = newElement;
        topologyIM.registerNode(newElement);
        LOG.debug(".registerTopologyElement(): Exit, newElement --> {}", newElement);
    }

    private void buildWUPFramework() {
        LOG.debug(".buildWUPFramework(): Entry");
        servicesBroker.registerWorkUnitProcessor(this.wupTopologyElement, this.getSubscribedTopics(), this.getWUPArchetype());
        LOG.debug(".buildWUPFramework(): Exit");
    }

    public abstract RDN getProcessingPlantRDN();

    public abstract RDN getWorkUnitProcessorRDN();

    public abstract RDN getApplicationServerRDN();

    public abstract RDN getSiteRDN();

    public abstract RDN getServiceModuleTypeRDN();

    public abstract RDN getServiceRDN();

    public abstract RDN getSubSystemRDN();

    public abstract String getVersion();
    
    public abstract WUPArchetypeEnum getWUPArchetype();

    public FDNToken getWupTypeID() {
        return (this.wupTypeID);
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

    public abstract FDNTokenSet getSubscribedTopics();

}
