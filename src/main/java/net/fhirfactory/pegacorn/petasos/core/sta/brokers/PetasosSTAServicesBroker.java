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

package net.fhirfactory.pegacorn.petasos.core.sta.brokers;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.core.sta.resilience.processingplant.cache.STAServiceModuleActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.core.sta.resilience.processingplant.manager.STAResilienceParcelServicesIM;
import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.sta.STATransaction;
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
public class PetasosSTAServicesBroker {
    private static final Logger LOG = LoggerFactory.getLogger(PetasosSTAServicesBroker.class);

    @Inject
    STAResilienceParcelServicesIM parcelServicesIM;

    @Inject
    STAServiceModuleActivityMatrixDM activityMatrixDM;

    @Inject
    TopicIM topicManager;

    public STATransaction registerSTAWorkUnitActivity(WUPJobCard jobCard, UoW initialUoW){
        if((jobCard == null) || (initialUoW == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard or initialUoW are null"));
        }
        ResilienceParcel newParcel = parcelServicesIM.registerSOAParcel(jobCard.getActivityID(), initialUoW );
        jobCard.getActivityID().setPresentParcelIdentifier(newParcel.getIdentifier());
        ParcelStatusElement statusElement = activityMatrixDM.startTransaction(jobCard.getActivityID(), ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE );
        STATransaction transaction = new STATransaction();
        transaction.setUnitOfWork(initialUoW);
        transaction.setStatusElement(statusElement);
        transaction.setJobCard(jobCard);
        return(transaction);
    }

    public void notifyFinishOfWorkUnitActivity(STATransaction transaction){
        if((transaction == null)){
            throw( new IllegalArgumentException(".notifyFinishOfWorkUnitActivity(): transaction is null"));
        }
        ResilienceParcel finishedParcel = parcelServicesIM.notifySOAParcelProcessingFinish(transaction.getJobCard().getActivityID().getPresentParcelIdentifier(), transaction.getUnitOfWork());
        activityMatrixDM.finishTransaction(transaction.getJobCard().getActivityID(), ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
    }

    public void notifyFailureOfWorkUnitActivity(STATransaction transaction){
        if(transaction == null){
            throw( new IllegalArgumentException(".notifyFailureOfWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel failedParcel = parcelServicesIM.notifySOAParcelProcessingFailure(transaction.getJobCard().getActivityID().getPresentParcelIdentifier(), transaction.getUnitOfWork());
        activityMatrixDM.finishTransaction(transaction.getJobCard().getActivityID(), ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
    }

    public void notifyCancellationOfWorkUnitActivity(STATransaction transaction){
        if(transaction == null){
            throw( new IllegalArgumentException(".notifyCancellationOfWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel failedParcel = parcelServicesIM.notifySOAParcelProcessingCancellation(transaction.getJobCard().getActivityID().getPresentParcelIdentifier());
        activityMatrixDM.finishTransaction(transaction.getJobCard().getActivityID(),ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_CANCELLED);
    }

    public void notifyPurgeOfWorkUnitActivity(STATransaction transaction){
        if((transaction == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard is null"));
        }
        if(!transaction.getJobCard().hasActivityID()) {
            return;
        }
        if(!transaction.getJobCard().getActivityID().hasPresentParcelIdentifier()){
            return;
        }
        parcelServicesIM.notifySOAParcelProcessingPurge(transaction.getJobCard().getActivityID().getPresentParcelIdentifier());
    }

    public ParcelStatusElement getCurrentParcelStatusElement(ResilienceParcelIdentifier parcelInstanceID){
        ParcelStatusElement statusElement = activityMatrixDM.getTransactionElement(parcelInstanceID);
        return(statusElement);
    }


    public ResilienceParcel getUnprocessedParcel(FDNToken wupTypeID){
        // TODO - this is the mechanism to re-start on failure, not currently implemented.
        return(null);
    }
    
    public void registerWorkUnitProcessor( NodeElement newElement, Set<TopicToken> payloadTopicSet, WUPArchetypeEnum wupNature){
        LOG.debug(".registerWorkUnitProcessor(): Entry, newElement --> {}, payloadTopicSet --> {}", newElement, payloadTopicSet);
        // wupFrameworkManager.buildWUPFramework(newElement, payloadTopicSet, wupNature);
        // wupInterchangeManager.buildWUPInterchangeRoutes(newElement);
    }

}
