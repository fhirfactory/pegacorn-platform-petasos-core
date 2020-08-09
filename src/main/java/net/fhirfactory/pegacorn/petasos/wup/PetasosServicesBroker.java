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

package net.fhirfactory.pegacorn.petasos.wup;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.interchange.manager.PathwayInterchangeManager;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.manager.ResilienceActivityServicesController;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.manager.ResilienceParcelServicesIM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.manager.WorkUnitProcessorFrameworkManager;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;

@ApplicationScoped
public class PetasosServicesBroker {
    private static final Logger LOG = LoggerFactory.getLogger(PetasosServicesBroker.class);

    @Inject
    ResilienceParcelServicesIM parcelServicesIM;

    @Inject
    ResilienceActivityServicesController rasController;
    
    @Inject 
    WorkUnitProcessorFrameworkManager wupFrameworkManager;

    @Inject
    PathwayInterchangeManager interchangeManager;

    @Inject
    TopicIM topicManager;

    public ParcelStatusElement registerStandardWorkUnitActivity(WUPJobCard jobCard, UoW initialUoW){
        if((jobCard == null) || (initialUoW == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard or initialUoW are null"));
        }
        ResilienceParcel newParcel = parcelServicesIM.registerParcel(jobCard.getCardID(), initialUoW, false );
        jobCard.getCardID().setPresentParcelIdentifier(newParcel.getIdentifier());
        ParcelStatusElement statusElement = rasController.registerNewWorkUnitActivity(jobCard);
        return(statusElement);
    }

    public ParcelStatusElement registerSystemEdgeWUA(WUPJobCard jobCard, UoW initialUoW){
        if((jobCard == null) || (initialUoW == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard or initialUoW are null"));
        }
        ResilienceParcel newParcel = parcelServicesIM.registerParcel(jobCard.getCardID(), initialUoW, true );
        jobCard.getCardID().setPresentParcelIdentifier(newParcel.getIdentifier());
        ParcelStatusElement statusElement = rasController.registerNewWorkUnitActivity(jobCard);
        return(statusElement);
    }

    public void notifyStartOfWorkUnitActivity(WUPJobCard jobCard){
        if((jobCard == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard or startedUoW are null"));
        }
        ResilienceParcel finishedParcel = parcelServicesIM.notifyParcelProcessingStart(jobCard.getCardID().getPresentParcelIdentifier());
        rasController.synchroniseJobCard(jobCard);
    }

    public void notifyFinishOfWorkUnitActivity(WUPJobCard jobCard, UoW finishedUoW){
        if((jobCard == null) || (finishedUoW == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel finishedParcel = parcelServicesIM.notifyParcelProcessingFinish(jobCard.getCardID().getPresentParcelIdentifier(), finishedUoW);
        rasController.synchroniseJobCard(jobCard);
    }

    public void notifyFinalisationOfWorkUnitActivity(WUPJobCard jobCard){
        if((jobCard == null)){
            throw( new IllegalArgumentException(".registerWorkUnitActivity(): jobCard is null"));
        }
        ResilienceParcel finishedParcel = parcelServicesIM.notifyParcelProcessingFinalisation(jobCard.getCardID().getPresentParcelIdentifier());
        rasController.synchroniseJobCard(jobCard);
    }

    public void notifyFailureOfWorkUnitActivity(WUPJobCard jobCard, UoW failedUoW){
        if((jobCard == null) || (failedUoW == null)){
            throw( new IllegalArgumentException(".notifyFailureOfWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel failedParcel = parcelServicesIM.notifyParcelProcessingFinish(jobCard.getCardID().getPresentParcelIdentifier(), failedUoW);
        rasController.synchroniseJobCard(jobCard);
    }

    public void notifyCancellationOfWorkUnitActivity(WUPJobCard jobCard){
        if(jobCard == null){
            throw( new IllegalArgumentException(".notifyCancellationOfWorkUnitActivity(): jobCard or finishedUoW are null"));
        }
        ResilienceParcel failedParcel = parcelServicesIM.notifyParcelProcessingCancellation(jobCard.getCardID().getPresentParcelIdentifier());
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
        parcelServicesIM.notifyParcelProcessingPurge(jobCard.getCardID().getPresentParcelIdentifier());
    }

    public void synchroniseJobCard(WUPJobCard existingJobCard){
        rasController.synchroniseJobCard(existingJobCard);
    }

    public ParcelStatusElement getCurrentParcelStatusElement(ResilienceParcelIdentifier parcelInstanceID){
        ParcelStatusElement statusElement = rasController.getStatusElement(parcelInstanceID);
        return(statusElement);
    }

    public void registerDownstreamWUP(FDNToken wuaEpisodeID, FDNToken interestedWUPInstanceID){
        rasController.registerWUAEpisodeDownstreamWUPInterest(wuaEpisodeID, interestedWUPInstanceID);
    }

    public ResilienceParcel getUnprocessedParcel(FDNToken wupTypeID){
        // TODO - this is the mechanism to re-start on failure, not currently implemented.
        return(null);
    }
    
    public void registerWorkUnitProcessor( NodeElement newElement, Set<TopicToken> payloadTopicSet, WUPArchetypeEnum wupNature){
        LOG.debug(".registerWorkUnitProcessor(): Entry, newElement --> {}, payloadTopicSet --> {}", newElement, payloadTopicSet);
        topicManager.initialiseServices();
        wupFrameworkManager.buildWUPFramework(newElement, payloadTopicSet, wupNature);
        interchangeManager.buildWUPInterchangeRoutes(newElement);
    }

}
