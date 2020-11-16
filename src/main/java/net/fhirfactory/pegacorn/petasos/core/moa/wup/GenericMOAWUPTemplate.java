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
package net.fhirfactory.pegacorn.petasos.core.moa.wup;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;

import net.fhirfactory.pegacorn.camel.BaseRouteBuilder;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.datasets.fhir.r4.internal.topics.FHIRElementTopicIDBuilder;
import net.fhirfactory.pegacorn.deployment.names.PegacornLadonComponentNames;
import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.moa.brokers.PetasosMOAServicesBroker;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.model.processingplant.ProcessingPlantServicesInterface;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;

/**
 * Generic Message Orientated Architecture (MOA) Work Unit Processor (WUP) Template
 * 
 * @author Mark A. Hunter
 * @since 2020-07-01
 */

public abstract class GenericMOAWUPTemplate extends BaseRouteBuilder {

    public static final Integer IPC_PACKET_MAXIMUM_FRAME_SIZE = 25 * 1024 * 1024; // 25 MB
    
    abstract protected Logger getLogger();

    private EndpointElement ingresTopologyEndpointElement;
    private EndpointElement egressTopologyEndpointElement;
    private NodeElement wupTopologyNodeElement;
    private NodeElementFunctionToken wupFunctionToken;
    private WUPIdentifier wupIdentifier;
    private String wupInstanceName;
    private WUPJobCard wupInstanceJobCard;
    private RouteElementNames nameSet;
    private String wupEgressPoint = null;
    private String wupIngresPoint = null;
    private WUPArchetypeEnum wupArchetype;
    private Set<TopicToken> topicSubscriptionSet;
    private String version;

    @Inject
    private PetasosMOAServicesBroker servicesBroker;
    
    @Inject
    private DeploymentTopologyIM wupTopologyProxy;

    @Inject
    private PegacornLadonComponentNames subsystemNames;

    @Inject
    private FHIRElementTopicIDBuilder fhirTopicIDBuilder;

    @Inject
    private ProcessingPlantServicesInterface processingPlantServices;

    public GenericMOAWUPTemplate() {
        super();
    }

    /**
     * This function essentially establishes the WUP itself, by first calling all the (abstract classes realised within subclasses)
     * and setting the core attributes of the WUP. Then, it executes the buildWUPFramework() function, which invokes the Petasos
     * framework around this WUP.
     *
     * It is automatically called by the CDI framework following Constructor invocation (see @PostConstruct tag).
     */
    @PostConstruct
    protected void initialise(){
        getLogger().debug(".initialise(): Entry, Default Post Constructor function to setup the WUP");
        getLogger().trace(".initialise(): Setting the WUP Instance Name - an unqualified (but system-module unique name) string to be used as part of the WUP FDN");
        this.wupInstanceName = specifyWUPInstanceName();
        getLogger().trace(".initialise(): wupInstanceName --> {}", this.wupInstanceName);
        getLogger().trace(".initialise(): Setting the WUP Version - this allows us to support multiple variants of the same WUP function");
        this.version = specifyWUPVersion();
        getLogger().trace(".initialise(): Setting if the WUP uses the Petasos generated Ingres/Egress Endpoints");
        getLogger().trace(".initialise(): Setting up the wupTopologyElement (NodeElement) instance, which is the Topology Server's representation of this WUP ");
        buildWUPNodeElement();
        getLogger().trace(".initialise(): Setting the actual WUP ID - this is an instance unique identifier for the WUP within the deployed solution");
        specifyInstanceID();
        getLogger().trace(".initialise(): Setting the WUP Archetype - which is used by the WUP Framework to ascertain what wrapping this WUP needs");
        this.wupArchetype =  specifyWUPArchetype();
        getLogger().trace(".initialise(): Setting the WUP Function Token (Function ID + Version ID) which uniquely defines the functionality of this WUP");
        specifyFunctionToken();
        getLogger().trace(".initialise(): Setting the WUP nameSet, which is the set of Route EndPoints that the WUP Framework will use to link various enablers");
        nameSet = new RouteElementNames(getWUPFunctionToken());
        getLogger().trace(".initialise(): Setting the WUP Ingres Topology Element, which is used to generate the .from() for the contained WUP Camel Route");
        specifyIngresTopologyEndpointElement();
        getLogger().trace(".initialise(): Setting the WUP Ingres Point, which is the .from() for the contained WUP Camel Route");
        this.wupIngresPoint = specifyIngresEndpoint();
        getLogger().trace(".initialise(): IngresPoint --> {}", this.wupIngresPoint);
        getLogger().trace(".initialise(): Setting the WUP Egress Topology Element, which is used to generate the .to() for the contained WUP Camel Route");
        specifyEgressTopologyEndpointElement();
        getLogger().trace(".initialise(): Setting the WUP Egress Point, which is the .to() for the contained WUP Camel Route");
        this.wupEgressPoint = specifyEgressEndpoint();
        getLogger().trace(".initialise(): EgressPoint --> {}", this.wupEgressPoint);

        getLogger().trace(".initialise(): Now invoking subclass initialising function(s)");
        executePostInitialisationActivities();
        getLogger().trace(".initialise(): Setting the Topic Subscription Set (i.e. the list of Data Sets we will process)");
        this.topicSubscriptionSet = specifySubscriptionTopics();
        getLogger().trace(".initialise(): Now call the WUP Framework constructure - which builds the Petasos framework around this WUP");
        buildWUPFramework(this.getContext());
        getLogger().debug(".initialise(): Exit");
    }
    
    // To be implemented methods (in Specialisations)
    
    protected abstract Set<TopicToken> specifySubscriptionTopics();
    protected abstract String specifyWUPInstanceName();
    protected abstract String specifyWUPVersion();
    protected abstract WUPArchetypeEnum specifyWUPArchetype();
    protected abstract String specifyWUPWorkshop();
    protected abstract String specifyIngresEndpoint();
    protected abstract String specifyEgressEndpoint();

    protected abstract String specifyIngresTopologyEndpointName();
    protected abstract String specifyIngresEndpointVersion();
    protected abstract boolean specifyUsesWUPFrameworkGeneratedIngresEndpoint();
    protected abstract String specifyEgressTopologyEndpointName();
    protected abstract String specifyEgressEndpointVersion();
    protected abstract boolean specifyUsesWUPFrameworkGeneratedEgressEndpoint();

    protected void executePostInitialisationActivities(){
        // Subclasses can optionally override
    }

    /**
     * This function goes to the Topology Server and extracts the NodeElementFunctionToken - which is a combination of
     * the Function ID (and FDNToken) and a Version qualifier (as a String).
     *
     * It uses the (previously specified WUP Instance ID) as the parameter for calling the Topology Server.
     *
     * @return The function token representing the exact capability of the WUP
     */
    public void specifyFunctionToken() {
        getLogger().debug(".specifyFunctionToken(): Entry, wupInstanceID --> {}", this.wupIdentifier);
        NodeElementFunctionToken functionToken = wupTopologyProxy.getWUPFunctionToken(this.wupIdentifier);
        this.wupFunctionToken = functionToken;
        getLogger().debug(".specifyFunctionToken(): Exit, retrieved functionToken --> {}", this.getWUPFunctionToken());
    }

    public void specifyInstanceID() {
        getLogger().debug(".specifyInstanceID(): Entry, wupInstanceName --> {}, wupVersion --> {}", this.getWupInstanceName(), this.getVersion());
        WUPIdentifier wupID = wupTopologyProxy.getWUPIdentifier(this.getWupInstanceName(), this.getVersion());
        this.wupIdentifier = wupID;
        getLogger().debug(".specifyInstanceID(): Exit, extracted wupInstanceID --> {}", this.getWUPIdentifier());
    }

    public void registerNodeInstantiation(){
        getLogger().debug(".registerTopologyElementInstantiation(): Entry");
        wupTopologyProxy.setNodeInstantiated(this.wupTopologyNodeElement.getNodeInstanceID(), true);
        getLogger().debug(".registerTopologyElementInstantiation(): Exit");
    }

    public void buildWUPFramework(CamelContext routeContext) {
        getLogger().debug(".buildWUPFramework(): Entry");
        servicesBroker.registerWorkUnitProcessor(this.wupTopologyNodeElement, this.getTopicSubscriptionSet(), this.getWupArchetype());
        getLogger().debug(".buildWUPFramework(): Exit");
    }
    
    public String getEndpointHostName(){
        FDN wupFDN = new FDN(this.wupIdentifier);
        ArrayList<RDN> rdnSet = wupFDN.getRDNSet();
        for(int counter = 0; counter < wupFDN.getRDNCount(); counter++ ){
            RDN currentRDN = rdnSet.get(counter);
            if(currentRDN.getQualifier().contentEquals(NodeElementTypeEnum.PLATFORM.getNodeElementType())){
               if(currentRDN.getValue().contentEquals("___")){
                   return("localhost");
               } else {
                   return(currentRDN.getValue());
               }
            }
        }
        return("localhost");
    }

    public NodeElementFunctionToken getWUPFunctionToken() {
        return (this.wupFunctionToken);
    }

    public WUPIdentifier getWUPIdentifier() {
        return (this.wupIdentifier);
    }
    protected void setWUPIdentifier(WUPIdentifier newID){
        getLogger().debug(".setWUPIdentifier(): Entry, newID (WUPIdentifier) --> {}", newID);
        this.wupIdentifier = newID;
    }

    public String ingresFeed() {
        return (this.wupIngresPoint);
    }

    public String egressFeed() {
        return (this.wupEgressPoint);
    }
    
    public PetasosMOAServicesBroker getServicesBroker(){
        return(this.servicesBroker);
    }
    
    public DeploymentTopologyIM getTopologyServer(){
        return(this.wupTopologyProxy);
    }

    public NodeElement getWupTopologyNodeElement() {
        return wupTopologyNodeElement;
    }

    public void setWupTopologyNodeElement(NodeElement wupTopologyNodeElement) {
        this.wupTopologyNodeElement = wupTopologyNodeElement;
    }

    public RouteElementNames getNameSet() {
        return nameSet;
    }

    public String getWupEgressPoint() {
        return wupEgressPoint;
    }

    public String getWupIngresPoint() {
        return wupIngresPoint;
    }

    public String getWupInstanceName() {
        return wupInstanceName;
    }

    public WUPArchetypeEnum getWupArchetype() {
        return wupArchetype;
    }

    public Set<TopicToken> getTopicSubscriptionSet() {
        return topicSubscriptionSet;
    }

    public void setTopicSubscriptionSet(Set<TopicToken> topicSubscriptionSet) {
        this.topicSubscriptionSet = topicSubscriptionSet;
    }

    public String getVersion() {
        return version;
    }

    public PegacornLadonComponentNames getSubsystemComponentNamesService(){
        return(this.subsystemNames);
    }

    public FHIRElementTopicIDBuilder getFHIRTopicIDBuilder(){
        return(this.fhirTopicIDBuilder);
    }

    public EndpointElement getIngresTopologyEndpointElement() {
        return ingresTopologyEndpointElement;
    }

    public EndpointElement getEgressTopologyEndpointElement() {
        return this.egressTopologyEndpointElement;
    }

    /**
     * Get's the Egress Topology Endpoint associated with the WUP. The assumption is that
     * there is only ever a single endpoint associated with a WUP. This may
     * break in later releases.
     */
    private void specifyEgressTopologyEndpointElement(){
        getLogger().debug(".specifyEgressTopologyEndpointElement(): Entry");
        if(specifyUsesWUPFrameworkGeneratedEgressEndpoint()){
            getLogger().debug(".specifyEgressTopologyEndpointElement(): using WUPFramework, so nothing to do here!");
            return;
        }
        NodeElementIdentifier nodeID = new NodeElementIdentifier(this.getWUPIdentifier());
        NodeElement node = this.getTopologyServer().getNode(nodeID);
        EndpointElement endpoint = this.getTopologyServer().getEndpoint(node, specifyEgressTopologyEndpointName(), specifyEgressEndpointVersion());
        this.egressTopologyEndpointElement = endpoint;
        getLogger().debug(".specifyEgressTopologyEndpointElement(): Exit, endpoint --> {}", endpoint);
    }

    /**
     * Get's the Ingress Topology Endpoint associated with the WUP. The assumption is that
     * there is only ever a single endpoint associated with a WUP. This may
     * break in later releases.
     *
     */
    private void specifyIngresTopologyEndpointElement(){
        getLogger().debug(".specifyIngresTopologyEndpointElement(): Entry");
        if(specifyUsesWUPFrameworkGeneratedIngresEndpoint()){
            getLogger().debug(".specifyIngresTopologyEndpointElement(): using WUPFramework, so nothing to do here!");
            return;
        }
        NodeElementIdentifier nodeID = new NodeElementIdentifier(this.getWUPIdentifier());
        NodeElement node = this.getTopologyServer().getNode(nodeID);
        EndpointElement endpoint = this.getTopologyServer().getEndpoint(node, specifyIngresTopologyEndpointName(), specifyIngresEndpointVersion());
        this.ingresTopologyEndpointElement = endpoint;
        getLogger().debug(".specifyIngresTopologyEndpointElement(): Exit, endpoint --> {}", endpoint);
    }

    private void buildWUPNodeElement(){
        getLogger().debug(".buildWUPNodeElement(): Entry, Workshop --> {}", specifyWUPWorkshop());
        NodeElement workshopNode = processingPlantServices.getWorkshop(specifyWUPWorkshop());
        getLogger().trace(".buildWUPNodeElement(): Entry, Workshop NodeElement--> {}", workshopNode);
        NodeElement newWUPNode = new NodeElement();
        getLogger().trace(".buildWUPNodeElement(): Create new FDN/Identifier for WUP");
        FDN newWUPNodeFDN = new FDN(workshopNode.getNodeInstanceID());
        newWUPNodeFDN.appendRDN(new RDN(NodeElementTypeEnum.WUP.getNodeElementType(), specifyWUPInstanceName()));
        NodeElementIdentifier newWUPNodeID = new NodeElementIdentifier(newWUPNodeFDN.getToken());
        getLogger().trace(".buildWUPNodeElement(): WUP NodeIdentifier --> {}", newWUPNodeID);
        newWUPNode.setNodeInstanceID(newWUPNodeID);
        getLogger().trace(".buildWUPNodeElement(): Create new Function Identifier for WUP");
        FDN newWUPNodeFunctionFDN = new FDN(workshopNode.getNodeFunctionID());
        newWUPNodeFunctionFDN.appendRDN(new RDN(NodeElementTypeEnum.WUP.getNodeElementType(), specifyWUPInstanceName()));
        getLogger().trace(".buildWUPNodeElement(): WUP Function Identifier --> {}", newWUPNodeFunctionFDN.getToken());
        newWUPNode.setNodeFunctionID(newWUPNodeFunctionFDN.getToken());
        newWUPNode.setVersion(specifyWUPVersion());
        newWUPNode.setConcurrencyMode(workshopNode.getConcurrencyMode());
        newWUPNode.setResilienceMode(workshopNode.getResilienceMode());
        newWUPNode.setInstanceInPlace(true);
        newWUPNode.setNodeArchetype(NodeElementTypeEnum.WUP);
        getLogger().trace(".buildWUPNodeElement(): Identify and assign associated endpoints");
        EndpointElement associatedEndpoint = null;
        NodeElement processingPlantNode = processingPlantServices.getProcessingPlantNodeElement();
        getLogger().trace(".buildWUPNodeElement(): parent ProcessingPlant --> {}", processingPlantNode);
        if(!specifyUsesWUPFrameworkGeneratedIngresEndpoint()) {
            getLogger().trace(".buildWUPNodeElement(): Capturing Ingres endpoint details");
            associatedEndpoint = getTopologyServer().getEndpoint(processingPlantNode, specifyIngresTopologyEndpointName(), specifyIngresEndpointVersion());
            newWUPNode.getEndpoints().add(associatedEndpoint.getEndpointInstanceID());
            getLogger().trace(".buildWUPNodeElement(): Ingres endpoint --> {}", associatedEndpoint);
        }
        if (!specifyUsesWUPFrameworkGeneratedEgressEndpoint()) {
            getLogger().trace(".buildWUPNodeElement(): Capturing Egress endpoint details");
            associatedEndpoint = getTopologyServer().getEndpoint(processingPlantNode, specifyEgressTopologyEndpointName(), specifyEgressEndpointVersion());
            newWUPNode.getEndpoints().add(associatedEndpoint.getEndpointInstanceID());
            getLogger().trace(".buildWUPNodeElement(): Egress endpoint --> {}", associatedEndpoint);
        }
        this.getTopologyServer().registerNode(newWUPNode);
        getLogger().debug(".buildWUPNodeElement(): Node Registered --> {}", newWUPNode);
        this.getTopologyServer().addContainedNodeToNode(workshopNode.getNodeInstanceID(), newWUPNode);
        this.setWupTopologyNodeElement(newWUPNode);
    }
}
