/*
 * Copyright (c) 2020 Mark A. Hunter (ACT Health)
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

package net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks;

import java.time.Instant;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.RouteElementNames;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPActivityStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;

/**
 * @author Mark A. Hunter
 * @since 2020-07-05
 */
@ApplicationScoped
public class WUPEgressConduit {
    private static final Logger LOG = LoggerFactory.getLogger(WUPEgressConduit.class);
    
    @Inject
    DeploymentTopologyIM topologyProxy;
    
    /**
     * This function reconstitutes the WorkUnitTransportPacket by extracting the WUPJobCard and ParcelStatusElement
     * from the Camel Exchange, and injecting them plus the UoW into it.
     *
     * @param incomingUoW   The Unit of Work (UoW) received as output from the actual Work Unit Processor (Business Logic)
     * @param camelExchange The Apache Camel Exchange object, for extracting the WUPJobCard & ParcelStatusElement from
     * @param wupInstanceKey The NodeElement Key Instance - an absolutely unique identifier for the instance of WUP within the entiry deployment.
     * @return A WorkUnitTransportPacket object for relay to the other
     */
    public WorkUnitTransportPacket receiveFromWUP(UoW incomingUoW, Exchange camelExchange, String wupInstanceKey) {
        if(LOG.isDebugEnabled()) {
        	LOG.debug(".receiveFromWUP(): Entry");
    		LOG.debug(".receiveFromWUP(): unitOfWork (UoW).instanceID --> {}", incomingUoW.getInstanceID());
    		LOG.debug(".receiveFromWUP(): unitOfWork (UoW).typeID --> {}", incomingUoW.getTypeID());
    		LOG.debug(".receiveFromWUP(): unitOfWork (UoW).payloadTopicID --> {}", incomingUoW.getPayloadTopicID());
    		LOG.debug(".receiveFromWUP(): unitOfWork (UoW).ingresContent --> {}", incomingUoW.getIngresContent());
    		LOG.debug(".receiveFromWUP(): unitOfWork (UoW).egressContent --> {}", incomingUoW.getEgressContent());
    		LOG.debug(".receiveFromWUP(): unitOfWork (UoW).payloadTopicID --> {}", incomingUoW.getPayloadTopicID());
    		LOG.debug(".receiveFromWUP(): unitOfWork (UoW).processingOutcome --> {}", incomingUoW.getProcessingOutcome());
    		LOG.debug(".receiveFromWUP(): wupInstanceKey (String) --> {}", wupInstanceKey);
        } 
        // Get my Petasos Context
        if( topologyProxy == null ) {
        	LOG.info("Warning Will Robinson!!!");
        }
        NodeElement node = topologyProxy.getNodeByKey(wupInstanceKey);
        LOG.trace(".receiveFromWUP(): Node Element retrieved --> {}", node);
        NodeElementFunctionToken wupFunctionToken = node.getNodeFunctionToken();
        LOG.trace(".receiveFromWUP(): wupFunctionToken (NodeElementFunctionToken) for this activity --> {}", wupFunctionToken); 
        // Now, continue with business logic
        RouteElementNames elementNames = new RouteElementNames(wupFunctionToken);
        // Retrieve the information from the CamelExchange
        String jobcardPropertyKey = "WUPJobCard" + wupInstanceKey; // this value should match the one in WUPIngresConduit.java
        String parcelStatusPropertyKey = "ParcelStatusElement" + wupInstanceKey; // this value should match the one in WUPIngresConduit.java
        WUPJobCard jobCard = camelExchange.getProperty(jobcardPropertyKey, WUPJobCard.class);
        ParcelStatusElement statusElement = camelExchange.getProperty(parcelStatusPropertyKey, ParcelStatusElement.class);
        // Now process incoming content
        WorkUnitTransportPacket transportPacket = new WorkUnitTransportPacket(jobCard.getCardID(), Date.from(Instant.now()), incomingUoW);
        LOG.trace(".receiveFromWUP(): We only want to check if the UoW was successful and modify the JobCard/StatusElement accordingly.");
        LOG.trace(".receiveFromWUP(): All detailed checking of the Cluster/SiteWide details is done in the WUPContainerEgressProcessor");
        switch (incomingUoW.getProcessingOutcome()) {
            case UOW_OUTCOME_SUCCESS:
                LOG.trace(".receiveFromWUP(): UoW was processed successfully - updating JobCard/StatusElement to FINISHED!");
                jobCard.setCurrentStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FINISHED);
                jobCard.setRequestedStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FINISHED);
                statusElement.setParcelStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
                statusElement.setEntryDate(Date.from(Instant.now()));
                break;
            case UOW_OUTCOME_NOTSTARTED:
            case UOW_OUTCOME_INCOMPLETE:
            case UOW_OUTCOME_FAILED:
            default:
                LOG.trace(".receiveFromWUP(): UoW was not processed or processing failed - updating JobCard/StatusElement to FAILED!");
                jobCard.setCurrentStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FAILED);
                jobCard.setRequestedStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FAILED);
                statusElement.setParcelStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
                statusElement.setEntryDate(Date.from(Instant.now()));
                break;
        }
        transportPacket.setCurrentJobCard(jobCard);
        transportPacket.setCurrentParcelStatus(statusElement);
        if(LOG.isDebugEnabled()) {
        	LOG.debug(".receiveFromWUP(): Exit"); 
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousParcelInstance -->{}", transportPacket.getCurrentJobCard().getCardID().getPreviousParcelIdentifier());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousEpisodeIdentifier --> {}", transportPacket.getCurrentJobCard().getCardID().getPreviousEpisodeIdentifier());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).previousWUPFunctionTokan --> {}", transportPacket.getCurrentJobCard().getCardID().getPreviousWUPFunctionToken());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).perviousWUPIdentifier --> {}", transportPacket.getCurrentJobCard().getCardID().getPreviousWUPIdentifier());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentParcelIdentifier -->{}", transportPacket.getCurrentJobCard().getCardID().getPresentParcelIdentifier());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentEpisodeIdentifier --> {}", transportPacket.getCurrentJobCard().getCardID().getPresentEpisodeIdentifier());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPFunctionTokan --> {}", transportPacket.getCurrentJobCard().getCardID().getPresentWUPFunctionToken());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContinuityID).presentWUPIdentifier --> {}", transportPacket.getCurrentJobCard().getCardID().getPresentWUPIdentifier());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).cardID (ContunuityID).createDate --> {}", transportPacket.getCurrentJobCard().getCardID().getCreationDate());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).clusterMode (ConcurrencyModeEnum) -->{}", transportPacket.getCurrentJobCard().getClusterMode());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).currentStatus (WUPActivityStatusEnum) --> {}", transportPacket.getCurrentJobCard().getCurrentStatus());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).grantedStatus (WUPActivityStatusEnum) --> {}", transportPacket.getCurrentJobCard().getGrantedStatus());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).toBeDiscarded (boolean) --> {}", transportPacket.getCurrentJobCard().getIsToBeDiscarded());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).requestedStatus (WUPActivityStatusEnum) --> {}", transportPacket.getCurrentJobCard().getRequestedStatus());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).systemMode (ResilienceModeEnum) --> {}", transportPacket.getCurrentJobCard().getSystemMode());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentJobCard (WUPJobCard).updateDate (Date) --> {}", transportPacket.getCurrentJobCard().getUpdateDate());
        	LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).currentParcelStatus (ParcelStatusElement).parcelStatus (ResilienceParcelProcessingStatusEnum) --> {}", transportPacket.getCurrentParcelStatus().getParcelStatus());
            LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).instanceID --> {}", transportPacket.getPayload().getInstanceID());
            LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).typeID --> {}", transportPacket.getPayload().getTypeID());
            LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", transportPacket.getPayload().getPayloadTopicID());
            LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).ingresContent --> {}", transportPacket.getPayload().getIngresContent());
            LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).egressContent --> {}", transportPacket.getPayload().getEgressContent());
            LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).payloadTopicID --> {}", transportPacket.getPayload().getPayloadTopicID());
            LOG.debug(".receiveFromWUP(): transportPacket (WorkUnitTransportPacket).getPayload (UoW).processingOutcome --> {}", transportPacket.getPayload().getProcessingOutcome());
        }
        
        return (transportPacket); 
    }
}
