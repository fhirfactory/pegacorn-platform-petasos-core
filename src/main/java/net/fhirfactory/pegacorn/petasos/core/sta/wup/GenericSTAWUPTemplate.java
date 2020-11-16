package net.fhirfactory.pegacorn.petasos.core.sta.wup;

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.sta.brokers.PetasosSTAServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class GenericSTAWUPTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(GenericSTAWUPTemplate.class);

    private WUPFunctionToken wupFunctionToken;
    private WUPIdentifier wupIdentifier;
    private String wupInstanceName;
    private WUPJobCard wupInstanceJobCard;
    private NodeElement wupNode;
    private WUPArchetypeEnum wupArchetype;
     private String version;

    @Inject
    PetasosSTAServicesBroker servicesBroker;

    @Inject
    DeploymentTopologyIM wupTopologyProxy;

    public GenericSTAWUPTemplate(){
        LOG.trace(".initialise(): Setting the WUP Archetype - which is used by the WUP Framework to ascertain what wrapping this WUP needs");
        wupArchetype =  WUPArchetypeEnum.WUP_NATURE_API_ANSWER;
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
        LOG.trace(".initialise(): Setting the WUP Instance Name - an unqualified (but system-module unique name) string to be used as part of the WUP FDN");
        this.wupInstanceName = specifyWUPInstanceName();
        LOG.info(".initialise(): wupInstanceName --> {}", this.wupInstanceName);
        LOG.trace(".initialise(): Setting the WUP Version - this allows us to support multiple variants of the same WUP function");
        this.version = specifyWUPVersion();
        LOG.trace(".initialise(): Setting the actual WUP ID - this is an instance unique identifier for the WUP within the deployed solution");
        this.wupIdentifier = specifyInstanceID();
        LOG.trace(".initialise(): Setting up the wupTopologyElement (NodeElement) instance, which is the Topology Server's representation of this WUP ");
        this.wupNode = specifyNodeElement();
        LOG.trace(".initialise(): Setting the WUP Function Token (Function ID + Version ID) which uniquely defines the functionality of this WUP");
        this.wupFunctionToken =  specifyFunctionToken();
        LOG.trace(".initialise(): Now call the WUP Framework constructure - which builds the Petasos framework around this WUP");
    }

    abstract String specifyWUPVersion();
    abstract String specifyWUPInstanceName();

    public WUPFunctionToken specifyFunctionToken() {
        LOG.debug(".specifyFunctionToken(): Entry, wupInstanceID --> {}", this.wupIdentifier);
        WUPFunctionToken functionToken = wupTopologyProxy.getWUPFunctionToken(this.wupIdentifier);
        LOG.debug(".specifyFunctionToken(): Exit, retrieved functionToken --> {}", functionToken);
        return(functionToken);
    }

    public WUPIdentifier specifyInstanceID() {
        LOG.debug(".specifyInstanceID(): Entry, wupInstanceName --> {}, wupVersion --> {}", this.getWupInstanceName(), this.getVersion());
        WUPIdentifier instanceId = wupTopologyProxy.getWUPIdentifier(this.getWupInstanceName(), this.getVersion());
        LOG.debug(".specifyInstanceID(): Exit, extracted wupInstanceID --> {}", instanceId);
        return(instanceId);
    }

    public NodeElement specifyNodeElement(){
        LOG.debug(".specifyNodeElement(): Entry");
        NodeElementIdentifier nodeID = new NodeElementIdentifier(this.wupIdentifier);
        NodeElement element = wupTopologyProxy.getNode(nodeID);
        LOG.debug(".specifyNodeElement(): Exit, retrieved NodeElement --> {}", element);
        return(element);
    }

    public void registerNodeInstantiation(){
        LOG.info(".registerTopologyElementInstantiation(): Entry");
        wupTopologyProxy.setNodeInstantiated(this.wupNode.getNodeInstanceID(), true);
        LOG.info(".registerTopologyElementInstantiation(): Exit");
    }

    public NodeElementFunctionToken getWUPFunctionToken() {
        return (this.wupFunctionToken);
    }

    public WUPIdentifier getWupIdentifier() {
        return (this.wupIdentifier);
    }

    public PetasosSTAServicesBroker getServicesBroker(){
        return(this.servicesBroker);
    }

    public DeploymentTopologyIM getTopologyServer(){
        return(this.wupTopologyProxy);
    }

    public NodeElement getWupNode() {
        return wupNode;
    }

    public void setWupNode(NodeElement wupNode) {
        this.wupNode = wupNode;
    }

    public NodeElementFunctionToken getWupFunctionToken() {
        return wupFunctionToken;
    }

    public void setWupFunctionToken(NodeElementFunctionToken nodeFunctionToken) {
        this.wupFunctionToken = new WUPFunctionToken(nodeFunctionToken);
    }

    public void setWupFunctionToken(WUPFunctionToken wupFunctionToken) {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
