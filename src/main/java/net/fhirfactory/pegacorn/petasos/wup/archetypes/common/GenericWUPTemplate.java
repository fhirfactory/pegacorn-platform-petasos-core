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

import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.wup.PetasosServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.topology.manager.proxies.ServiceModuleTopologyProxy;

/**
 * 
 * @author Mark A. Hunter
 * @since 2020-07-01
 */

@ApplicationScoped
public abstract class GenericWUPTemplate extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GenericWUPTemplate.class);

    private NodeElementFunctionToken wupFunctionToken;
    private FDNToken wupInstanceID;
    private String wupInstanceName;
    private WUPJobCard wupInstanceJobCard;
    private NodeElement wupTopologyElement;
    private RouteElementNames nameSet;
    private String wupEgressPoint = null;
    private String wupIngresPoint = null;
    private WUPArchetypeEnum wupArchetype;
    private Set<TopicToken> topicSubscriptionSet;
    private String version;
    
    @Inject
    PetasosServicesBroker servicesBroker;
    
    @Inject
    ServiceModuleTopologyProxy wupTopologyProxy;


    public GenericWUPTemplate() {
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
    public void initialise(){
        LOG.debug(".initialise(): Entry, Default Post Constructor function to setup the WUP");
        LOG.trace(".initialise(): Setting the Topic Subscription Set (i.e. the list of Data Sets we will process)");
        this.topicSubscriptionSet = specifySubscriptionTopics();
        LOG.trace(".initialise(): Setting the WUP Instance Name - an unqualified (but system-module unique name) string to be used as part of the WUP FDN");
        this.wupInstanceName = specifyWUPInstanceName();
        LOG.info(".initialise(): wupInstanceName --> {}", this.wupInstanceName);
        LOG.trace(".initialise(): Setting the WUP Version - this allows us to support multiple variants of the same WUP function");
        this.version = specifyWUPVersion();
        LOG.trace(".initialise(): Setting the actual WUP ID - this is an instance unique identifier for the WUP within the deployed solution");
        this.wupInstanceID = specifyInstanceID();
        LOG.trace(".initialise(): Setting up the wupTopologyElement (NodeElement) instance, which is the Topology Server's representation of this WUP ");
        this.wupTopologyElement = specifyNodeElement();
        LOG.trace(".initialise(): Setting the WUP Archetype - which is used by the WUP Framework to ascertain what wrapping this WUP needs");
        this.wupArchetype =  specifyWUPArchetype();
        LOG.trace(".initialise(): Setting the WUP Function Token (Function ID + Version ID) which uniquely defines the functionality of this WUP");
        this.wupFunctionToken =  specifyFunctionToken();
        LOG.trace(".initialise(): Setting the WUP nameSet, which is the set of Route EndPoints that the WUP Framework will use to link various enablers");
        nameSet = new RouteElementNames(getWUPFunctionToken());
        LOG.trace(".initialise(): Setting the WUP Ingres Point, which is the .from() for the contained WUP Camel Route");
        this.wupIngresPoint = specifyIngresEndpoint();
        LOG.info(".initialise(): IngresPoint --> {}", this.wupIngresPoint);
        LOG.trace(".initialise(): Setting the WUP Egress Point, which is the .to() for the contained WUP Camel Route");
        this.wupEgressPoint = specifyEgressEndpoint();
        LOG.info(".initialise(): EgressPoint --> {}", this.wupEgressPoint);
        LOG.trace(".initialise(): Now call the WUP Framework constructure - which builds the Petasos framework around this WUP");
        buildWUPFramework();
    }
    
    // To be implemented methods (in Specialisations)
    
    public abstract Set<TopicToken> specifySubscriptionTopics();
    public abstract String specifyWUPInstanceName();
    public abstract String specifyWUPVersion();
    public abstract WUPArchetypeEnum specifyWUPArchetype();
    public abstract String specifyIngresEndpoint();
    public abstract String specifyEgressEndpoint();


    /**
     * This function goes to the Topology Server and extracts the NodeElementFunctionToken - which is a combination of
     * the Function ID (and FDNToken) and a Version qualifier (as a String).
     *
     * It uses the (previously specified WUP Instance ID) as the parameter for calling the Topology Server.
     *
     * @return The function token representing the exact capability of the WUP
     */
    public NodeElementFunctionToken specifyFunctionToken() {
        LOG.debug(".specifyFunctionToken(): Entry, wupInstanceID --> {}", this.wupInstanceID);
        NodeElementFunctionToken functionToken = wupTopologyProxy.getWUPFunctionToken(this.wupInstanceID);
        LOG.debug(".specifyFunctionToken(): Exit, retrieved functionToken --> {}", functionToken);
        return(functionToken);
    }

    public FDNToken specifyInstanceID() {
        LOG.debug(".specifyInstanceID(): Entry, wupInstanceName --> {}, wupVersion --> {}", this.getWupInstanceName(), this.getVersion());
        FDNToken instanceId = wupTopologyProxy.getWUPInstanceID(this.getWupInstanceName(), this.getVersion());
        LOG.debug(".specifyInstanceID(): Exit, extracted wupInstanceID --> {}", instanceId);
        return(instanceId);
    }

    public NodeElement specifyNodeElement(){
        LOG.debug(".specifyNodeElement(): Entry");
        NodeElement element = wupTopologyProxy.getNode(this.wupInstanceID);
        LOG.debug(".specifyNodeElement(): Exit, retrieved NodeElement --> {}", element);
        return(element);
    }
    
    public void registerTopologyElementInstantiation(){
        LOG.info(".registerTopologyElementInstantiation(): Entry");
        wupTopologyProxy.setInstanceInPlace(wupInstanceID, true);
        LOG.info(".registerTopologyElementInstantiation(): Exit");
    }

    public void buildWUPFramework() {
        LOG.debug(".buildWUPFramework(): Entry");
        servicesBroker.registerWorkUnitProcessor(this.wupTopologyElement, this.getTopicSubscriptionSet(), this.getWupArchetype());
        LOG.debug(".buildWUPFramework(): Exit");
    }
    
    public String getEndpointHostName(){
        FDN wupFDN = new FDN(this.wupInstanceID);
        Map<Integer, RDN> rdnSet = wupFDN.getRDNSet();
        for(int counter = 0; counter < wupFDN.getRDNCount(); counter++ ){
            RDN currentRDN = rdnSet.get(counter);
            if(currentRDN.getNameQualifier().contentEquals(NodeElementTypeEnum.PLATFORM.getNodeElementType())){
               if(currentRDN.getNameValue().contentEquals("Derived")){
                   return("localhost");
               } else {
                   return(currentRDN.getNameValue());
               }
            }
        }
        return("localhost");
    }

    public NodeElementFunctionToken getWUPFunctionToken() {
        return (this.wupFunctionToken);
    }

    public FDNToken getWupInstanceID() {
        return (this.wupInstanceID);
    }

    public String ingresFeed() {
        return (this.wupIngresPoint);
    }

    public String egressFeed() {
        return (this.wupEgressPoint);
    }
    
    public PetasosServicesBroker getServicesBroker(){
        return(this.servicesBroker);
    }
    
    public ServiceModuleTopologyProxy getTopologyServer(){
        return(this.wupTopologyProxy);
    }

    public NodeElement getWupTopologyElement() {
        return wupTopologyElement;
    }

    public void setWupTopologyElement(NodeElement wupTopologyElement) {
        this.wupTopologyElement = wupTopologyElement;
    }

    public RouteElementNames getNameSet() {
        return nameSet;
    }

    public void setNameSet(RouteElementNames nameSet) {
        this.nameSet = nameSet;
    }

    public String getWupEgressPoint() {
        return wupEgressPoint;
    }

    public void setWupEgressPoint(String wupEgressPoint) {
        this.wupEgressPoint = wupEgressPoint;
    }

    public String getWupIngresPoint() {
        return wupIngresPoint;
    }

    public void setWupIngresPoint(String wupIngresPoint) {
        this.wupIngresPoint = wupIngresPoint;
    }

    public NodeElementFunctionToken getWupFunctionToken() {
        return wupFunctionToken;
    }

    public void setWupFunctionToken(NodeElementFunctionToken wupFunctionToken) {
        this.wupFunctionToken = wupFunctionToken;
    }

    public String getWupInstanceName() {
        return wupInstanceName;
    }

    public void setWupInstanceName(String wupInstanceName) {
        this.wupInstanceName = wupInstanceName;
    }

    public WUPArchetypeEnum getWupArchetype() {
        return wupArchetype;
    }

    public void setWupArchetype(WUPArchetypeEnum wupArchetype) {
        this.wupArchetype = wupArchetype;
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

    public void setVersion(String version) {
        this.version = version;
    }
}
