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

package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.buildingblocks;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.wup.PetasosServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyMode;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.DeploymentResilienceMode;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import net.fhirfactory.pegacorn.petasos.model.pathway.ContinuityID;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPActivityStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import net.fhirfactory.pegacorn.petasos.topology.properties.ServiceModuleProperties;

/**
 * @author Mark A. Hunter
 * @since 2020-06-01
 */
public class WUPContainerIngresProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(WUPContainerIngresProcessor.class);
    private RouteElementNames elementNames;

    @Inject
    PetasosServicesBroker petasosServicesBroker;

    @Inject
    TopologyIM moduleIM;
    
    @Inject
    ServiceModuleProperties propertiesIM;
    
    
    /**
     * This class/method is used as the injection point into the WUP Processing Framework for the specific WUP Type/Instance in question.
     * It registers the following:
     *      - A ResilienceParcel for the UoW (registered with the SystemModule Parcel Cache: via the PetasosServiceBroker)
     *      - A WUPJobCard for the associated Work Unit Activity (registered into the SystemModule Activity Matrix: via the PetasosServiceBroker)
     *      - A ParcelStatusElement for the ResilienceParcel (again, register into the SystemModule Activity Matrix: via the PetasosServiceBroker)
     *
     * The function handles both new UoW or UoW instances that are being re-tried.
     *
     * It performs checks on the Status (WUPJobCard.currentStatus & ParcelStatusElement.hasClusterFocus) to determine if this WUP-Thread should
     * actually perform the Processing of the UoW via the WUP.
     *
     * It also checks on / assigns values to the Status (ParcelStatusElement.parcelStatus) if there are issues with the parcel. If there are, it may also
     * assign a "failed" status to both the WUPJobCard and ParcelStatusElement, and trigger a discard of this Parcel (for a retry) via setting the
     * WUPJobCard.isToBeDiscarded attribute to true.
     *
     * Finally, if all is going OK, but this WUP-Thread does not have the Cluster Focus (or SystemWide Focus), it waits in a sleep/loop until a condition
     * changes.
     *
     * @param ingresPacket The WorkUnitTransportPacket that is to be forwarded to the Intersection (if all is OK)
     * @param camelExchange The Apache Camel Exchange object, used to store a Semaphors and Attributes
     * @param wupTypeID The Work Unit Processor Type: should be unique with the SystemModule and is used to establish context
     * @param wupInstanceID The Work Unit Processor Instance: will be unique is as used to established uniqueness across deployment of Parcel/Activity sets
     * @return Should return a WorkUnitTransportPacket that is forwarding onto the WUP Ingres Gatekeeper.
     */
    public WorkUnitTransportPacket ingresContentProcessor(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, FDNToken wupTypeID, FDNToken wupInstanceID) {
        LOG.debug(".ingresContentProcessor(): Enter, ingresPacket --> {}, wupTypeID --> {}, wupInstanceID -->{}", ingresPacket, wupTypeID, wupInstanceID);
        elementNames = new RouteElementNames(wupTypeID);
        LOG.trace(".ingresContentProcessor(): Now, check if this the 1st time the associated UoW has been (attempted to be) processed");
        WorkUnitTransportPacket newTransportPacket;
        if (ingresPacket.getIsARetry()) {
            LOG.trace(".ingresContentProcessor(): This is a recovery or retry iteration of processing this UoW, so send to .alternativeIngresContentProcessor()");
            newTransportPacket = alternativeIngresContentProcessor(ingresPacket, camelExchange, wupTypeID, wupInstanceID);
        } else {
            LOG.trace(".ingresContentProcessor(): This is the 1st time this UoW is being processed, so send to .standardIngresContentProcessor()");
            newTransportPacket = standardIngresContentProcessor(ingresPacket, camelExchange, wupTypeID, wupInstanceID);
        }
        int waitTime = propertiesIM.getWorkUnitActivitySleepInterval();
        boolean waitState = true;
        WUPJobCard jobCard = newTransportPacket.getCurrentJobCard();
        ParcelStatusElement statusElement = newTransportPacket.getCurrentParcelStatus();
        while (waitState) {
            switch (jobCard.getCurrentStatus()) {
                case WUP_ACTIVITY_STATUS_WAITING:
                    jobCard.setRequestedStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING);
                    petasosServicesBroker.synchroniseJobCard(jobCard);
                    if (!statusElement.getHasClusterFocus()) {
                        jobCard.setCurrentStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_WAITING);
                        waitState = true;
                        break;
                    }
                    if (jobCard.getGrantedStatus() == WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING) {
                        jobCard.setCurrentStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING);
                        waitState = false;
                        break;
                    }
                    break;
                case WUP_ACTIVITY_STATUS_EXECUTING:
                case WUP_ACTIVITY_STATUS_FINISHED:
                case WUP_ACTIVITY_STATUS_FAILED:
                case WUP_ACTIVITY_STATUS_CANCELED:
                default:
                    jobCard.setIsToBeDiscarded(true);
                    waitState = false;
                    jobCard.setCurrentStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_CANCELED);
                    jobCard.setRequestedStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_CANCELED);
            }
            if (waitState) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    LOG.trace(".ingresContentProcessor(): Something interrupted my nap! reason --> {}", e.getMessage());
                }
            }
        }
        if (jobCard.getIsToBeDiscarded()) {
            ParcelStatusElement currentParcelStatus = newTransportPacket.getCurrentParcelStatus();
            currentParcelStatus.setRequiresRetry(true);
            currentParcelStatus.setParcelStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
        }
        LOG.debug(".ingresContentProcessor(): Exit, newTransportPacket --> {}", newTransportPacket);
        return (newTransportPacket);
    }

    public WorkUnitTransportPacket standardIngresContentProcessor(WorkUnitTransportPacket incomingPacket, Exchange camelExchange, FDNToken wupTypeID, FDNToken wupInstanceID) {
        LOG.debug(".ingresContentProcessor(): Enter, incomingPacket --> {}, wupTypeID --> {}, wupInstanceID --> {}", incomingPacket, wupTypeID, wupInstanceID);
        UoW theUoW = incomingPacket.getPayload();
        LOG.trace(".standardIngresContentProcessor(): Creating a new ContinuityID/ActivityID");
        FDNToken localWUPInstanceID = new FDNToken(wupInstanceID);
        FDNToken localWUPTypeID = new FDNToken(wupTypeID);
        ContinuityID oldActivityID = incomingPacket.getCurrentJobCard().getCardID();
        ContinuityID newActivityID = new ContinuityID();
        FDNToken previousPresentParcelInstanceID = oldActivityID.getPresentParcelInstanceID();
        FDNToken previousPresentEpisodeID = oldActivityID.getPresentParcelInstanceID();
        FDNToken previousPresentWUPInstanceID = oldActivityID.getPresentWUPInstanceID();
        FDNToken previousPresentWUPTypeID = oldActivityID.getPresentWUPTypeID();
        newActivityID.setPreviousParcelInstanceID(previousPresentParcelInstanceID);
        newActivityID.setPreviousWUAEpisodeID(previousPresentEpisodeID);
        newActivityID.setPreviousWUPInstanceID(previousPresentWUPInstanceID);
        newActivityID.setPreviousWUPTypeID(previousPresentWUPTypeID);
        newActivityID.setPresentWUPTypeID(localWUPTypeID);
        newActivityID.setPresentWUPInstanceID(localWUPInstanceID);
        LOG.trace(".standardIngresContentProcessor(): Creating new JobCard");
        WUPJobCard activityJobCard = new WUPJobCard(newActivityID, WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_WAITING, WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING, ConcurrencyMode.CONCURRENCY_MODE_STANDALONE, DeploymentResilienceMode.RESILIENCE_MODE_STANDALONE, Date.from(Instant.now()));
        LOG.trace(".standardIngresContentProcessor(): Registering the Work Unit Activity using the ContinuityID --> {} and UoW --> {}", newActivityID, theUoW);
        ParcelStatusElement statusElement = petasosServicesBroker.registerStandardWorkUnitActivity(activityJobCard, theUoW);
        LOG.trace(".standardIngresContentProcessor(): Let's check the status of everything");
        switch (statusElement.getParcelStatus()) {
            case PARCEL_STATUS_REGISTERED:
            case PARCEL_STATUS_ACTIVE_ELSEWHERE:
                LOG.trace(".standardIngresContentProcessor(): The Parcel is either Registered or Active_Elsewhere - both are acceptable at this point");
                break;
            case PARCEL_STATUS_FAILED:
            case PARCEL_STATUS_ACTIVE:
            case PARCEL_STATUS_FINALISED_ELSEWHERE:
            case PARCEL_STATUS_FINALISED:
            case PARCEL_STATUS_FINISHED_ELSEWHERE:
            case PARCEL_STATUS_FINISHED:
            case PARCEL_STATUS_INITIATED:
            default:
                LOG.trace(".standardIngresContentProcessor(): The Parcel is doing something odd, none of the above states should be in-play, so cancel");
                statusElement.setParcelStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
                statusElement.setRequiresRetry(true);
                activityJobCard.setRequestedStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_CANCELED);
                activityJobCard.setIsToBeDiscarded(true);
        }
        WorkUnitTransportPacket newTransportPacket = new WorkUnitTransportPacket(elementNames.getEndPointWUPContainerIngresGatekeeperIngres(),Date.from(Instant.now()),incomingPacket.getPayload());
        newTransportPacket.setCurrentJobCard(activityJobCard);
        newTransportPacket.setCurrentParcelStatus(statusElement);
        LOG.debug(".ingresContentProcessor(): Exit, newTransportPacket --> {}", newTransportPacket);
        return (newTransportPacket);
    }

    public WorkUnitTransportPacket alternativeIngresContentProcessor(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, FDNToken wupTypeID, FDNToken wupInstanceID) {
        LOG.debug(".alternativeIngresContentProcessor(): Enter, ingresPacket --> {}, wupTypeID --> {}, wupInstanceID --> {}", ingresPacket, wupTypeID, wupInstanceID);
        // TODO Implement alternate flow for ingressContentProcessor functionality (retry functionality).
        return (ingresPacket);
    }
}
