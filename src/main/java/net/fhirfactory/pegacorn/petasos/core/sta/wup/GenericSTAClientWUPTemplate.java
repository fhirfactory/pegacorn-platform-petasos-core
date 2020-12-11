package net.fhirfactory.pegacorn.petasos.core.sta.wup;

import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.sta.brokers.PetasosSTAServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.processingplant.ProcessingPlantServicesInterface;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class GenericSTAClientWUPTemplate {
    private NodeElementFunctionToken apiClientNodeFunction;
    private WUPIdentifier apiClientWUP;
    private String apiClientName;
    private WUPJobCard apiClientJobCard;
    private NodeElement apiClientNode;
    private String apiClientVersion;
    private boolean isInitialised;

    private IParser parserR4;

    @Inject
    PetasosSTAServicesBroker servicesBroker;

    @Inject
    private DeploymentTopologyIM deploymentTopologyIM;

    @Inject
    private FHIRContextUtility fhirContextUtility;

    public GenericSTAClientWUPTemplate() {
        isInitialised = false;
        this.apiClientName = specifyAPIClientName();
        this.apiClientVersion = specifyAPIClientVersion();
    }

    abstract protected String specifyAPIClientName();
    abstract protected String specifyAPIClientVersion();
    abstract protected Logger getLogger();
    abstract protected ProcessingPlantServicesInterface specifyProcessingPlant();

    @PostConstruct
    protected void initialise() {
        getLogger().debug(".initialise(): Entry");
        if (!isInitialised) {
            getLogger().trace(".initialise(): AccessBase is NOT initialised");
            this.parserR4 = fhirContextUtility.getJsonParser();
            this.isInitialised = true;
            getProcessingPlant().initialisePlant();
            this.apiClientNode = buildPersistenceServiceNode();
            this.apiClientNodeFunction = this.apiClientNode.getNodeFunctionToken();
            this.apiClientWUP = new WUPIdentifier(this.apiClientNode.getNodeInstanceID());
        }
    }

    protected ProcessingPlantServicesInterface getProcessingPlant(){return(specifyProcessingPlant());}

    /**
     * This function builds the Deployment Topology node (a WUP) for the
     * Persistence Service.
     * <p>
     * It uses the Name (specifyPersistenceServiceName()) defined in the subclass as part
     * of the Identifier and then registers with the Topology Services.
     *
     * @return The NodeElement representing the WUP which this code-set is
     * fulfilling.
     */
    private NodeElement buildPersistenceServiceNode() {
        getLogger().debug(".buildPersistenceServiceNode(): Entry");
        NodeElementIdentifier ladonInstanceIdentifier = getProcessingPlant().getProcessingPlantNodeId();
        getLogger().trace(".buildPersistenceServiceNode(): retrieved Ladon-ProcessingPlant Identifier --> {}", ladonInstanceIdentifier);
        if (ladonInstanceIdentifier == null) {
            getLogger().error(".buildPersistenceServiceNode(): Ladon's ProcessingPlant is not Initialised!");
        }
        FDN virtualdbFDN = new FDN(ladonInstanceIdentifier);
        virtualdbFDN.appendRDN(new RDN(NodeElementTypeEnum.WORKSHOP.getNodeElementType(), "VirtualDB"));
        NodeElementIdentifier virtualdbId = new NodeElementIdentifier(virtualdbFDN.getToken());
        getLogger().trace(".buildPersistenceServiceNode(): Retrieving VirtualDB Node");
        NodeElement virtualdb = getDeploymentTopologyIM().getNode(virtualdbId);
        getLogger().trace(".buildPersistenceServiceNode(): virtualdb node (NodeElement) --> {}", virtualdb);
        FDN persistenceServiceInstanceFDN = new FDN(virtualdbFDN);
        persistenceServiceInstanceFDN.appendRDN(new RDN(NodeElementTypeEnum.WUP.getNodeElementType(), "Accessor-" + getApiClientName()));
        NodeElementIdentifier persistenceServiceInstanceIdentifier = new NodeElementIdentifier(persistenceServiceInstanceFDN.getToken());
        getLogger().trace(".buildPersistenceServiceNode(): Now construct the Work Unit Processing Node");
        NodeElement persistenceService = new NodeElement();
        getLogger().trace(".buildPersistenceServiceNode(): Constructing WUP Node, Setting Version Number");
        persistenceService.setVersion(getApiClientVersion());
        getLogger().trace(".buildPersistenceServiceNode(): Constructing WUP Node, Setting Node Instance");
        persistenceService.setNodeInstanceID(virtualdbId);
        getLogger().trace(".buildPersistenceServiceNode(): Constructing WUP Node, Setting Concurrency Mode");
        persistenceService.setConcurrencyMode(virtualdb.getConcurrencyMode());
        getLogger().trace(".buildPersistenceServiceNode(): Constructing WUP Node, Setting Resillience Mode");
        persistenceService.setResilienceMode(virtualdb.getResilienceMode());
        getLogger().trace(".buildPersistenceServiceNode(): Constructing WUP Node, Setting inPlace Status");
        persistenceService.setInstanceInPlace(true);
        getLogger().trace(".buildPersistenceServiceNode(): Constructing WUP Node, Setting Containing Element Identifier");
        persistenceService.setContainingElementID(virtualdb.getNodeInstanceID());
        getLogger().trace(".buildPersistenceServiceNode(): Now registering the Node");
        getDeploymentTopologyIM().registerNode(persistenceService);
        getLogger().debug(".buildPersistenceServiceNode(): Exit, persistenceServiceInstanceIdentifier (NodeElementIdentifier) --> {}", persistenceServiceInstanceIdentifier);
        return (persistenceService);
    }

    public NodeElementFunctionToken getApiClientNodeFunction() {
        return apiClientNodeFunction;
    }

    public WUPIdentifier getApiClientWUP() {
        return apiClientWUP;
    }

    public String getApiClientName() {
        return apiClientName;
    }

    public WUPJobCard getApiClientJobCard() {
        return apiClientJobCard;
    }

    public NodeElement getApiClientNode() {
        return apiClientNode;
    }

    public String getApiClientVersion() {
        return apiClientVersion;
    }

    public DeploymentTopologyIM getDeploymentTopologyIM() {
        return deploymentTopologyIM;
    }
}
