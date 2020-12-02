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

package net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.manager;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.audit.api.PetasosAuditWriter;
import net.fhirfactory.pegacorn.petasos.core.common.resilience.processingplant.cache.ProcessingPlantParcelCacheDM;
import net.fhirfactory.pegacorn.petasos.model.pathway.ActivityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.EpisodeIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelFinalisationStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import javax.transaction.Transactional;

/**
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class ProcessingPlantResilienceParcelServicesIM {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingPlantResilienceParcelServicesIM.class);
//    private FDN nodeInstanceFDN;

    @Inject
    ProcessingPlantParcelCacheDM parcelCacheDM;

    @Inject
    PetasosAuditWriter auditWriter;
    

    @Transactional
    public ResilienceParcel registerParcel(ActivityID activityID, UoW unitOfWork, boolean synchronousWriteToAudit) {
        LOG.debug(".registerParcel(): Entry"); 
        if ((unitOfWork == null) || (activityID == null)) {
            throw (new IllegalArgumentException("unitOfWork, wupTypeID or wupInstanceID are null in method invocation"));
        }
        if(LOG.isDebugEnabled()) {
    		LOG.debug(".registerParcel(): activityID (ActivityID).previousParcelIdentifier -->{}", activityID.getPreviousParcelIdentifier());
    		LOG.debug(".registerParcel(): activityID (ActivityID).previousEpisodeIdentifier --> {}", activityID.getPreviousEpisodeIdentifier());
    		LOG.debug(".registerParcel(): activityID (ActivityID).previousWUPFunctionTokan --> {}", activityID.getPreviousWUPFunctionToken());
    		LOG.debug(".registerParcel(): activityID (ActivityID).previousWUPIdentifier --> {}", activityID.getPreviousWUPIdentifier());
    		LOG.debug(".registerParcel(): activityID (ActivityID).presentParcelIdentifier -->{}", activityID.getPresentParcelIdentifier());
    		LOG.debug(".registerParcel(): activityID (ActivityID).presentEpisodeIdentifier --> {}", activityID.getPresentEpisodeIdentifier());
    		LOG.debug(".registerParcel(): activityID (ActivityID).presentWUPFunctionTokan --> {}", activityID.getPresentWUPFunctionToken());
    		LOG.debug(".registerParcel(): activityID (ActivityID).presentWUPIdentifier --> {}", activityID.getPresentWUPIdentifier());
    		LOG.debug(".registerParcel(): activityID (ContunuityID).createDate --> {}", activityID.getCreationDate());
    		LOG.debug(".registerParcel(): unitOfWork (UoW).instanceID --> {}", unitOfWork.getInstanceID());
    		LOG.debug(".registerParcel(): unitOfWork (UoW).typeID --> {}", unitOfWork.getTypeID());
    		LOG.debug(".registerParcel(): unitOfWork (UoW).payloadTopicID --> {}", unitOfWork.getPayloadTopicID());
    		LOG.debug(".registerParcel(): unitOfWork (UoW).ingresContent --> {}", unitOfWork.getIngresContent());
    		LOG.debug(".registerParcel(): unitOfWork (UoW).egressContent --> {}", unitOfWork.getEgressContent());
    		LOG.debug(".registerParcel(): unitOfWork (UoW).payloadTopicID --> {}", unitOfWork.getPayloadTopicID());
    		LOG.debug(".registerParcel(): unitOfWork (UoW).processingOutcome --> {}", unitOfWork.getProcessingOutcome());
    		LOG.debug(".registerParcel(): synchronousWriteToAudit (boolean) --> {}", synchronousWriteToAudit);
        }
        LOG.trace(".registerParcel(): Checking and/or Creating a WUAEpisde ID");
        if(!activityID.hasPresentEpisodeIdentifier()) {
        	FDN newWUAFDN = new FDN(activityID.getPresentWUPFunctionToken().getAsSingleFDNToken());
        	FDN uowTypeFDN = new FDN(unitOfWork.getTypeID());
        	newWUAFDN.appendFDN(uowTypeFDN);
        	EpisodeIdentifier wuaEpisodeToken = new EpisodeIdentifier(newWUAFDN.getToken());
        	activityID.setPresentEpisodeIdentifier(wuaEpisodeToken);
        }
        // 1st, lets register the parcel
        LOG.trace(".registerParcel(): check for existing ResilienceParcel instance for this WUP/UoW combination");
        ResilienceParcel parcelInstance =  parcelCacheDM.getCurrentParcelForWUP(activityID.getPresentWUPIdentifier(), unitOfWork.getTypeID());
        if(parcelInstance != null){
            LOG.trace(".registerParcel(): Well, there seems to be a Parcel already for this WUPInstanceID/UoWInstanceID. Odd, but let's use it!");
        } else {
            LOG.trace(".registerParcel(): Attempted to retrieve existing ResilienceParcel, and there wasn't one, so let's create it!");
            parcelInstance = new ResilienceParcel(activityID, unitOfWork);
            parcelCacheDM.addParcel(parcelInstance);
            LOG.trace(".registerParcel(): Set the PresentParcelInstanceID in the ActivityID (ActivityID), ParcelInstanceID --> {}", parcelInstance.getIdentifier());
            activityID.setPresentParcelIdentifier(parcelInstance.getIdentifier());
            Date registrationDate = Date.from(Instant.now());
            LOG.trace(".registerParcel(): Set the Registration Date --> {}", registrationDate);
            parcelInstance.setRegistrationDate(registrationDate);
            LOG.trace(".registerParcel(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
            parcelInstance.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
            LOG.trace(".registerParcel(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
            parcelInstance.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
            LOG.trace(".registerParcel(): Doing an Audit Write");
            auditWriter.writeAuditEntry(parcelInstance, synchronousWriteToAudit);
        }
        LOG.debug(".registerParcel(): Exit");
        if(LOG.isDebugEnabled()) {
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).episodeID --> {}", parcelInstance.getEpisodeIdentifier());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).upsteamEpisodeID --> {}", parcelInstance.getUpstreamEpisodeIdentifier());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).parcelInstanceID --> {}", parcelInstance.getIdentifier());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).associatedWUPInstanceID --> {}", parcelInstance.getAssociatedWUPIdentifier());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).processingStatus --> {}", parcelInstance.getProcessingStatus());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).typeID --> {}", parcelInstance.getTypeID());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).registrationDate --> {}", parcelInstance.getRegistrationDate());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).startDate --> {}", parcelInstance.getStartDate());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).finishedDate --> {}", parcelInstance.getFinishedDate());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).finalisationDate --> {}", parcelInstance.getFinalisationDate());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).finalisationStatus --> {}", parcelInstance.getFinalisationStatus());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).cancellationDate --> {}", parcelInstance.getCancellationDate());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).instanceID --> {}", parcelInstance.getActualUoW().getInstanceID());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).typeID --> {}", parcelInstance.getActualUoW().getTypeID());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", parcelInstance.getActualUoW().getPayloadTopicID());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).ingresContent --> {}", parcelInstance.getActualUoW().getIngresContent());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).egressContent --> {}", parcelInstance.getActualUoW().getEgressContent());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", parcelInstance.getActualUoW().getPayloadTopicID());
        	LOG.debug(".registerParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).processingOutcome --> {}", parcelInstance.getActualUoW().getProcessingOutcome());
        }
        return(parcelInstance);
    }

    @Transactional
    public ResilienceParcel notifyParcelProcessingStart(ResilienceParcelIdentifier parcelID) {
        LOG.debug(".notifyParcelProcessingStart(): Entry, parcelID --> {}", parcelID);
        if (parcelID == null) {
            throw (new IllegalArgumentException("parcelID is null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingStart(): retrieve existing Parcel");
        ResilienceParcel currentParcel = parcelCacheDM.getParcelInstance(parcelID);
        Date startDate = Date.from(Instant.now());
        LOG.trace(".notifyParcelProcessingStart(): Set the Start Date --> {}", startDate);
        currentParcel.setStartDate(startDate);
        LOG.trace(".notifyParcelProcessingStart(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        currentParcel.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        LOG.trace(".notifyParcelProcessingStart(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE);
        currentParcel.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE);
        // TODO Check to see if we should do an Audit Entry when we start processing (as well as when it is registered)
        // LOG.trace(".notifyParcelProcessingStart(): Doing an Audit Write, note that it is asynchronous by design");
        // auditWriter.writeAuditEntry(currentParcel,false);
        LOG.debug(".notifyParcelProcessingStart(): Exit, returning finished Parcel --> {}", currentParcel);
        return(currentParcel);
    }

    @Transactional
    public ResilienceParcel notifyParcelProcessingFinish(ResilienceParcelIdentifier parcelID, UoW unitOfWork) {
        LOG.debug(".notifyParcelProcessingFinish(): Entry, parcelID (ResilienceParcelIdentifier) --> {}, unitOfWork (UoW) --> {}", parcelID, unitOfWork);
        if ((unitOfWork == null) || (parcelID == null)) {
            throw (new IllegalArgumentException("unitOfWork or parcelID are null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingFinish(): retrieve existing Parcel");
        ResilienceParcel currentParcel = parcelCacheDM.getParcelInstance(parcelID);
        LOG.trace(".notifyParcelProcessingFinish(): Parcel Retrieved, contents --> {}", currentParcel);
        LOG.trace(".notifyParcelProcessingFinish(): update the UoW --> but only if the UoW content comes from the Agent, not the actual WUP itself");
        if(!(unitOfWork == currentParcel.getActualUoW())) {
            LOG.trace(".notifyParcelProcessingFinish(): update the UoW (Egress Content)");
            currentParcel.getActualUoW().setEgressContent(unitOfWork.getEgressContent());
            LOG.trace(".notifyParcelProcessingFinish(): update the UoW Processing Outcome --> {}", unitOfWork.getProcessingOutcome());
            currentParcel.getActualUoW().setProcessingOutcome(unitOfWork.getProcessingOutcome());
        }
        Date finishDate = Date.from(Instant.now());
        LOG.trace(".notifyParcelProcessingFinish(): Set the Finish Date --> {}", finishDate);
        currentParcel.setFinishedDate(finishDate);
        LOG.trace(".notifyParcelProcessingFinish(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        currentParcel.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        LOG.trace(".notifyParcelProcessingFinish(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
        currentParcel.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
        // TODO Check to see if we should do an Audit Entry when we finish processing
        // LOG.trace(".notifyParcelProcessingFinish(): Doing an Audit Write, note that it is asynchronous by design");
        auditWriter.writeAuditEntry(currentParcel,true);
       	LOG.debug(".notifyParcelProcessingFinish(): Exit, parcelInstance (ResilienceParcel) --> {}", currentParcel);
        return(currentParcel);
    }

    @Transactional
    public ResilienceParcel notifyParcelProcessingFailure(ResilienceParcelIdentifier parcelID, UoW unitOfWork) {
        LOG.debug(".notifyParcelProcessingFailure(): Entry, parcelID --> {}, unitOfWork --> {}", parcelID, unitOfWork);
        if ((unitOfWork == null) || (parcelID == null)) {
            throw (new IllegalArgumentException(".notifyParcelProcessingFailure(): unitOfWork or parcelID are null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingFailure(): retrieve existing Parcel");
        ResilienceParcel currentParcel = parcelCacheDM.getParcelInstance(parcelID);
        LOG.trace(".notifyParcelProcessingFailure(): update the UoW (Egress Content)");
        currentParcel.getActualUoW().setEgressContent(unitOfWork.getEgressContent());
        LOG.trace(".notifyParcelProcessingFailure(): update the UoW Processing Outcome --> {}", unitOfWork.getProcessingOutcome());
        currentParcel.getActualUoW().setProcessingOutcome(unitOfWork.getProcessingOutcome());
        Date finishDate = Date.from(Instant.now());
        LOG.trace(".notifyParcelProcessingFailure(): Set the Finish Date --> {}", finishDate);
        currentParcel.setFinishedDate(finishDate);
        LOG.trace(".notifyParcelProcessingFailure(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        currentParcel.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        LOG.trace(".notifyParcelProcessingFailure(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
        currentParcel.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
        LOG.trace(".notifyParcelProcessingFailure(): Doing an Audit Write, note that it is asynchronous by desgin");
        auditWriter.writeAuditEntry(currentParcel,false);
        LOG.debug(".notifyParcelProcessingFailure(): Exit, returning failed Parcel --> {}", currentParcel);
        return(currentParcel);
    }

    @Transactional
    public ResilienceParcel notifyParcelProcessingFinalisation(ResilienceParcelIdentifier parcelID) {
        LOG.debug(".notifyParcelProcessingFinalisation(): Entry, parcelID --> {}, unitOfWork --> {}", parcelID);
        if (parcelID == null) {
            throw (new IllegalArgumentException(".notifyParcelProcessingFinalisation(): parcelID is null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingFinalisation(): retrieve existing Parcel");
        ResilienceParcel currentParcel = parcelCacheDM.getParcelInstance(parcelID);
        LOG.trace(".notifyParcelProcessingFinalisation(): checking to see if finish date has been set and, if not, setting it");
        if(!currentParcel.hasFinishedDate()) {
            Date finishDate = Date.from(Instant.now());
            LOG.trace(".notifyParcelProcessingFinalisation(): Set the Finish Date --> {}", finishDate);
            currentParcel.setFinishedDate(finishDate);
        }
        Date finalisationDate = Date.from(Instant.now());
        LOG.trace(".notifyParcelProcessingFinalisation(): Set the Finalisation Date --> {}", finalisationDate);
        currentParcel.setFinalisationDate(finalisationDate);
        LOG.trace(".notifyParcelProcessingFinalisation(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
        currentParcel.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
        LOG.trace(".notifyParcelProcessingFinalisation(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED);
        currentParcel.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED);
        LOG.trace(".notifyParcelProcessingFinalisation(): Doing an Audit Write, note that it is asynchronous by design");
        auditWriter.writeAuditEntry(currentParcel,false);
        LOG.debug(".notifyParcelProcessingFinalisation(): Exit, returning finished Parcel --> {}", currentParcel);
        return(currentParcel);
    }

    @Transactional
    public ResilienceParcel notifyParcelProcessingCancellation(ResilienceParcelIdentifier parcelID) {
        LOG.debug(".notifyParcelProcessingCancellation(): Entry, parcelID --> {}", parcelID);
        if (parcelID == null) {
            throw (new IllegalArgumentException(".notifyParcelProcessingFinalisation(): parcelID is null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingCancellation(): retrieve existing Parcel");
        ResilienceParcel currentParcel = parcelCacheDM.getParcelInstance(parcelID);
        LOG.trace(".notifyParcelProcessingCancellation(): checking to see if finish date has been set and, if not, setting it");
        if(!currentParcel.hasFinishedDate()) {
            Date finishDate = Date.from(Instant.now());
            LOG.trace(".notifyParcelProcessingCancellation(): Set the Finish Date --> {}", finishDate);
            currentParcel.setFinishedDate(finishDate);
        }
        Date finalisationDate = Date.from(Instant.now());
        LOG.trace(".notifyParcelProcessingCancellation(): Set the Finalisation Date --> {}", finalisationDate);
        currentParcel.setFinalisationDate(finalisationDate);
        LOG.trace(".notifyParcelProcessingCancellation(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
        currentParcel.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
        LOG.trace(".notifyParcelProcessingCancellation(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED);
        currentParcel.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED);
        LOG.trace(".notifyParcelProcessingCancellation(): Doing an Audit Write, note that it is asynchronous by design");
        auditWriter.writeAuditEntry(currentParcel,false);
        LOG.debug(".notifyParcelProcessingCancellation(): Exit, returning finished Parcel --> {}", currentParcel);
        return(currentParcel);
    }

    @Transactional
    public void notifyParcelProcessingPurge(ResilienceParcelIdentifier parcelID) {
        LOG.debug(".notifyParcelProcessingPurge(): Entry, parcelID --> {}, unitOfWork --> {}", parcelID);
        if (parcelID == null) {
            throw (new IllegalArgumentException(".notifyParcelProcessingPurge(): parcelID is null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingPurge(): retrieve existing Parcel");
        // TODO: Ascertain if we need to do an audit-entry for this.
        //        LOG.trace(".notifyParcelProcessingPurge(): Doing an Audit Write, note that it is asynchronous by design");
        //        auditWriter.writeAuditEntry(currentParcel,false);
        //LOG.debug(".notifyParcelProcessingPurge(): Exit, returning finished Parcel --> {}", currentParcel);
    }
}
