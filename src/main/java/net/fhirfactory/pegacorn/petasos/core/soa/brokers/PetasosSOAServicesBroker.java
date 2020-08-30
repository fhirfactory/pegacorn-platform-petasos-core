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

package net.fhirfactory.pegacorn.petasos.core.soa.brokers;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.core.soa.resilience.processingplant.cache.SOAServiceModuleActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.core.soa.resilience.processingplant.manager.SOAResilienceParcelServicesIM;
import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class PetasosSOAServicesBroker {
    private static final Logger LOG = LoggerFactory.getLogger(PetasosSOAServicesBroker.class);

    @Inject
    SOAResilienceParcelServicesIM parcelServicesIM;

    @Inject
    SOAServiceModuleActivityMatrixDM rasController;

    @Inject
    TopicIM topicManager;

    public ParcelStatusElement registerSOAWorkUnitActivity(WUPJobCard jobCard, UoW initialUoW){
        if((jobCard == null) || (initialUoW == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard or initialUoW are null"));
        }
        ResilienceParcel newParcel = parcelServicesIM.registerSOAParcel(jobCard.getCardID(), initialUoW );
        jobCard.getCardID().setPresentParcelIdentifier(newParcel.getIdentifier());
        ParcelStatusElement statusElement = rasController.startTransaction(jobCard.getCardID(), ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE );
        return(statusElement);
    }

    public void notifyFinishOfWorkUnitActivity(WUPJobCard jobCard, UoW finishedUoW){
        if((jobCard == null) || (finishedUoW == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel finishedParcel = parcelServicesIM.notifySOAParcelProcessingFinish(jobCard.getCardID().getPresentParcelIdentifier(), finishedUoW);
        rasController.finishTransaction(jobCard.getCardID(), ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
    }

    public void notifyFailureOfWorkUnitActivity(WUPJobCard jobCard, UoW failedUoW){
        if((jobCard == null) || (failedUoW == null)){
            throw( new IllegalArgumentException(".notifyFailureOfWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel failedParcel = parcelServicesIM.notifySOAParcelProcessingFailure(jobCard.getCardID().getPresentParcelIdentifier(), failedUoW);
        rasController.finishTransaction(jobCard.getCardID(), ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
    }

    public void notifyCancellationOfWorkUnitActivity(WUPJobCard jobCard){
        if(jobCard == null){
            throw( new IllegalArgumentException(".notifyCancellationOfWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel failedParcel = parcelServicesIM.notifySOAParcelProcessingCancellation(jobCard.getCardID().getPresentParcelIdentifier());
        rasController.finishTransaction(jobCard.getCardID(),ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_CANCELLED);
    }

    public void notifyPurgeOfWorkUnitActivity(WUPJobCard jobCard){
        if((jobCard == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard is null"));
        }
        if(!jobCard.hasCardID()) {
            return;
        }
        if(!jobCard.getCardID().hasPresentParcelIdentifier()){
            return;
        }
        parcelServicesIM.notifySOAParcelProcessingPurge(jobCard.getCardID().getPresentParcelIdentifier());
    }

    public ParcelStatusElement getCurrentParcelStatusElement(ResilienceParcelIdentifier parcelInstanceID){
        ParcelStatusElement statusElement = rasController.getTransactionElement(parcelInstanceID);
        return(statusElement);
    }


    public ResilienceParcel getUnprocessedParcel(FDNToken wupTypeID){
        // TODO - this is the mechanism to re-start on failure, not currently implemented.
        return(null);
    }
    
    public void registerWorkUnitProcessor( NodeElement newElement, Set<TopicToken> payloadTopicSet, WUPArchetypeEnum wupNature){
        LOG.debug(".registerWorkUnitProcessor(): Entry, newElement --> {}, payloadTopicSet --> {}", newElement, payloadTopicSet);
        topicManager.initialiseServices();
        // wupFrameworkManager.buildWUPFramework(newElement, payloadTopicSet, wupNature);
        // interchangeManager.buildWUPInterchangeRoutes(newElement);
    }

}
