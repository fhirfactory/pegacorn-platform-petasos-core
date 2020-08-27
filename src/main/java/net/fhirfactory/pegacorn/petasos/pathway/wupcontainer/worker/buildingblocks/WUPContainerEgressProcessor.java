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

package net.fhirfactory.pegacorn.petasos.pathway.wupcontainer.worker.buildingblocks;

import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.deployment.topology.manager.ServiceModuleTopologyProxy;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.pathway.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.servicemodule.brokers.PetasosServicesBroker;

/**
 * @author Mark A. Hunter
 * @since 2020-07-01
 */
@ApplicationScoped
public class WUPContainerEgressProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(WUPContainerEgressProcessor.class);
    RouteElementNames elementNames = null;

    @Inject
    PetasosServicesBroker petasosServicesBroker;

    @Inject
    ServiceModuleTopologyProxy topologyProxy;


    public WorkUnitTransportPacket egressContentProcessor(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, String wupInstanceKey) {
    	if(LOG.isDebugEnabled()) {
        	LOG.debug(".egressContentProcessor(): Entry"); 
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousParcelInstance -->{}", ingresPacket.getCurrentJobCard().getCardID().getPreviousParcelIdentifier());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUAEpisodeID --> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousEpisodeIdentifier());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUPFunctionTokan --> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousWUPFunctionToken());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).perviousWUPIdentifier--> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousWUPIdentifier());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentParcelIdentifier -->{}", ingresPacket.getCurrentJobCard().getCardID().getPresentParcelIdentifier());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentEpisodeIdentifier --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentEpisodeIdentifier());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPFunctionTokan --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentWUPFunctionToken());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPIdentifier --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentWUPIdentifier());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContunuityID).createDate --> {}", ingresPacket.getCurrentJobCard().getCardID().getCreationDate());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).clusterMode (ConcurrencyModeEnum) -->{}", ingresPacket.getCurrentJobCard().getClusterMode());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).currentStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getCurrentStatus());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).grantedStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getGrantedStatus());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).toBeDiscarded (boolean) --> {}", ingresPacket.getCurrentJobCard().getIsToBeDiscarded());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).requestedStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getRequestedStatus());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).systemMode (ResilienceModeEnum) --> {}", ingresPacket.getCurrentJobCard().getSystemMode());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).updateDate (Date) --> {}", ingresPacket.getCurrentJobCard().getUpdateDate());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).currentParcelStatus (ParcelStatusElement).parcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ingresPacket.getCurrentParcelStatus().getParcelStatus());
        	LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).instanceID --> {}", ingresPacket.getPayload().getInstanceID());
			LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).typeID --> {}", ingresPacket.getPayload().getTypeID());
			LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", ingresPacket.getPayload().getPayloadTopicID());
			LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).ingresContent --> {}", ingresPacket.getPayload().getIngresContent());
			LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).egressContent --> {}", ingresPacket.getPayload().getEgressContent());
			LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", ingresPacket.getPayload().getPayloadTopicID());
			LOG.debug(".egressContentProcessor(): ingresPacket (WorkUnitTransportPacket).getPayload (UoW).processingOutcome --> {}", ingresPacket.getPayload().getProcessingOutcome());
			LOG.debug(".egressContentProcessor(): wupInstanceKey (String) --> {}", wupInstanceKey);
    	}
        // Get my Petasos Context
        NodeElement node = topologyProxy.getNodeByKey(wupInstanceKey);
        if(LOG.isTraceEnabled()) {
        	LOG.trace(".egressContentProcessor{}: Retrieved node from TopologyProxy");
        	Iterator<String> listIterator = node.debugPrint(".egressContentProcessor{}: node").iterator();
        	while(listIterator.hasNext()) {
        		LOG.trace(listIterator.next());
        	}
        }
        NodeElementFunctionToken wupFunctionToken = node.getNodeFunctionToken();
        LOG.trace(".receiveFromWUP(): wupFunctionToken (NodeElementFunctionToken) for this activity --> {}", wupFunctionToken); 
        // Now, continue with business logic
        WorkUnitTransportPacket egressPacket = null;
        switch (node.getResilienceMode()) {
            case RESILIENCE_MODE_MULTISITE:
                LOG.trace(".egressContentProcessor(): Deployment Mode --> PETASOS_MODE_MULTISITE");
            case RESILIENCE_MODE_CLUSTERED:
                LOG.trace(".egressContentProcessor(): Deployment Mode --> PETASOS_MODE_CLUSTERED");
            case RESILIENCE_MODE_STANDALONE:
                LOG.trace(".egressContentProcessor(): Deployment Mode --> PETASOS_MODE_STANDALONE");
                egressPacket = standaloneDeploymentModeECP(ingresPacket, camelExchange,node);
        }
		if(LOG.isDebugEnabled()) {
			LOG.debug(".egressContentProcessor(): Exit");
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousParcelInstance -->{}", egressPacket.getCurrentJobCard().getCardID().getPreviousParcelIdentifier());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUAEpisodeID --> {}", egressPacket.getCurrentJobCard().getCardID().getPreviousEpisodeIdentifier());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUPFunctionTokan --> {}", egressPacket.getCurrentJobCard().getCardID().getPreviousWUPFunctionToken());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).perviousWUPIdentifier--> {}", egressPacket.getCurrentJobCard().getCardID().getPreviousWUPIdentifier());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentParcelIdentifier -->{}", egressPacket.getCurrentJobCard().getCardID().getPresentParcelIdentifier());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentEpisodeIdentifier --> {}", egressPacket.getCurrentJobCard().getCardID().getPresentEpisodeIdentifier());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPFunctionTokan --> {}", egressPacket.getCurrentJobCard().getCardID().getPresentWUPFunctionToken());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPIdentifier --> {}", egressPacket.getCurrentJobCard().getCardID().getPresentWUPIdentifier());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContunuityID).createDate --> {}", egressPacket.getCurrentJobCard().getCardID().getCreationDate());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).clusterMode (ConcurrencyModeEnum) -->{}", egressPacket.getCurrentJobCard().getClusterMode());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).currentStatus (WUPActivityStatusEnum) --> {}", egressPacket.getCurrentJobCard().getCurrentStatus());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).grantedStatus (WUPActivityStatusEnum) --> {}", egressPacket.getCurrentJobCard().getGrantedStatus());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).toBeDiscarded (boolean) --> {}", egressPacket.getCurrentJobCard().getIsToBeDiscarded());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).requestedStatus (WUPActivityStatusEnum) --> {}", egressPacket.getCurrentJobCard().getRequestedStatus());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).systemMode (ResilienceModeEnum) --> {}", egressPacket.getCurrentJobCard().getSystemMode());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).updateDate (Date) --> {}", egressPacket.getCurrentJobCard().getUpdateDate());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).currentParcelStatus (ParcelStatusElement).parcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", egressPacket.getCurrentParcelStatus().getParcelStatus());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).getPayload (UoW).instanceID --> {}", egressPacket.getPayload().getInstanceID());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).getPayload (UoW).typeID --> {}", egressPacket.getPayload().getTypeID());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", egressPacket.getPayload().getPayloadTopicID());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).getPayload (UoW).ingresContent --> {}", egressPacket.getPayload().getIngresContent());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).getPayload (UoW).egressContent --> {}", egressPacket.getPayload().getEgressContent());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", egressPacket.getPayload().getPayloadTopicID());
			LOG.debug(".egressContentProcessor(): egressPacket (WorkUnitTransportPacket).getPayload (UoW).processingOutcome --> {}", egressPacket.getPayload().getProcessingOutcome());
		}
        return (egressPacket);
    }

    private WorkUnitTransportPacket standaloneDeploymentModeECP(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, NodeElement wupNode) {
    	if(LOG.isDebugEnabled()) {
        	LOG.debug(".standaloneDeploymentModeECP(): Entry"); 
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousParcelInstance -->{}", ingresPacket.getCurrentJobCard().getCardID().getPreviousParcelIdentifier());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUAEpisodeID --> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousEpisodeIdentifier());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUPFunctionTokan --> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousWUPFunctionToken());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).perviousWUPInstanceID --> {}", ingresPacket.getCurrentJobCard().getCardID().getPreviousWUPIdentifier());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentParcelInstance -->{}", ingresPacket.getCurrentJobCard().getCardID().getPresentParcelIdentifier());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUAEpisodeID --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentEpisodeIdentifier());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPFunctionTokan --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentWUPFunctionToken());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPInstanceID --> {}", ingresPacket.getCurrentJobCard().getCardID().getPresentWUPIdentifier());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContunuityID).createDate --> {}", ingresPacket.getCurrentJobCard().getCardID().getCreationDate());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).clusterMode (ConcurrencyModeEnum) -->{}", ingresPacket.getCurrentJobCard().getClusterMode());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).currentStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getCurrentStatus());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).grantedStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getGrantedStatus());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).toBeDiscarded (boolean) --> {}", ingresPacket.getCurrentJobCard().getIsToBeDiscarded());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).requestedStatus (WUPActivityStatusEnum) --> {}", ingresPacket.getCurrentJobCard().getRequestedStatus());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).systemMode (ResilienceModeEnum) --> {}", ingresPacket.getCurrentJobCard().getSystemMode());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).updateDate (Date) --> {}", ingresPacket.getCurrentJobCard().getUpdateDate());
        	LOG.debug(".standaloneDeploymentModeECP(): ingresPacket (WorkUnitTransportPacket).currentParcelStatus (ParcelStatusElement).parcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ingresPacket.getCurrentParcelStatus().getParcelStatus());
        	Iterator<String> listIterator = wupNode.debugPrint(".standaloneDeploymentModeECP{}: wupNode").iterator();
        	while(listIterator.hasNext()) {
        		LOG.debug(listIterator.next());
        	}
    	}
        elementNames = new RouteElementNames(wupNode.getNodeFunctionToken());
        LOG.trace(".standaloneDeploymentModeECP(): Now, extract WUPJobCard from ingresPacket (WorkUnitTransportPacket)");
        WUPJobCard jobCard = ingresPacket.getCurrentJobCard();
        LOG.trace(".standaloneDeploymentModeECP(): Now, extract ParcelStatusElement from ingresPacket (WorkUnitTransportPacket)");
        ParcelStatusElement statusElement = ingresPacket.getCurrentParcelStatus();
        LOG.trace(".standaloneDeploymentModeECP(): Now, extract UoW from ingresPacket (WorkUnitTransportPacket)");
        UoW uow = ingresPacket.getPayload();
		if(LOG.isDebugEnabled()) {
			LOG.debug(".standaloneDeploymentModeECP(): uow (UoW).instanceID --> {}", uow.getInstanceID());
			LOG.debug(".standaloneDeploymentModeECP(): uow (UoW).typeID --> {}", uow.getTypeID());
			LOG.debug(".standaloneDeploymentModeECP(): uow (UoW).payloadTopicID --> {}", uow.getPayloadTopicID());
			LOG.debug(".standaloneDeploymentModeECP(): uow (UoW).ingresContent --> {}", uow.getIngresContent());
			LOG.debug(".standaloneDeploymentModeECP(): uow (UoW).egressContent --> {}", uow.getEgressContent());
			LOG.debug(".standaloneDeploymentModeECP(): uow (UoW).payloadTopicID --> {}", uow.getPayloadTopicID());
			LOG.debug(".standaloneDeploymentModeECP(): uow (UoW).processingOutcome --> {}", uow.getProcessingOutcome());
		}
		LOG.trace(".standaloneDeploymentModeECP(): Now, continue processing based on the ParcelStatusElement.getParcelStatus() (ResilienceParcelProcessingStatusEnum)");
        ResilienceParcelProcessingStatusEnum parcelProcessingStatusEnum = statusElement.getParcelStatus();
        switch (parcelProcessingStatusEnum) {
            case PARCEL_STATUS_FINISHED:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
                petasosServicesBroker.notifyFinishOfWorkUnitActivity(jobCard, uow);
                break;
            case PARCEL_STATUS_ACTIVE_ELSEWHERE:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE_ELSEWHERE);
            case PARCEL_STATUS_FINISHED_ELSEWHERE:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED_ELSEWHERE);
            case PARCEL_STATUS_FINALISED_ELSEWHERE:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED_ELSEWHERE);
            case PARCEL_STATUS_REGISTERED:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
            case PARCEL_STATUS_INITIATED:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_INITIATED);
            case PARCEL_STATUS_ACTIVE:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE);
            case PARCEL_STATUS_FINALISED:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED);
            case PARCEL_STATUS_FAILED:
            	LOG.trace(".standaloneDeploymentModeECP(): ParcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
            default:
                petasosServicesBroker.notifyFailureOfWorkUnitActivity(jobCard, uow);
        }
        return (ingresPacket);
    }
}
