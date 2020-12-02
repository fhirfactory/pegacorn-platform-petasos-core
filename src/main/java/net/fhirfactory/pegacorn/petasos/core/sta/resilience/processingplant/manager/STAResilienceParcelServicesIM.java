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

package net.fhirfactory.pegacorn.petasos.core.sta.resilience.processingplant.manager;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.audit.api.PetasosAuditWriter;
import net.fhirfactory.pegacorn.petasos.core.common.resilience.processingplant.cache.ProcessingPlantParcelCacheDM;
import net.fhirfactory.pegacorn.petasos.model.pathway.ActivityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.EpisodeIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelFinalisationStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Date;

/**
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class STAResilienceParcelServicesIM {
    private static final Logger LOG = LoggerFactory.getLogger(STAResilienceParcelServicesIM.class);

    @Inject
    ProcessingPlantParcelCacheDM parcelCacheDM;

    @Inject
    PetasosAuditWriter auditWriter;
    

    @Transactional
    public ResilienceParcel registerSOAParcel(ActivityID activityID, UoW unitOfWork) {
        LOG.debug(".registerSOAParcel(): Entry");
        if ((unitOfWork == null) || (activityID == null)) {
            throw (new IllegalArgumentException("unitOfWork, wupTypeID or wupInstanceID are null in method invocation"));
        }
        if(LOG.isDebugEnabled()) {
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).previousParcelIdentifier -->{}", activityID.getPreviousParcelIdentifier());
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).previousEpisodeIdentifier --> {}", activityID.getPreviousEpisodeIdentifier());
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).previousWUPFunctionTokan --> {}", activityID.getPreviousWUPFunctionToken());
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).previousWUPIdentifier --> {}", activityID.getPreviousWUPIdentifier());
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).presentParcelIdentifier -->{}", activityID.getPresentParcelIdentifier());
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).presentEpisodeIdentifier --> {}", activityID.getPresentEpisodeIdentifier());
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).presentWUPFunctionTokan --> {}", activityID.getPresentWUPFunctionToken());
    		LOG.debug(".registerSOAParcel(): activityID (ActivityID).presentWUPIdentifier --> {}", activityID.getPresentWUPIdentifier());
    		LOG.debug(".registerSOAParcel(): activityID (ContunuityID).createDate --> {}", activityID.getCreationDate());
    		LOG.debug(".registerSOAParcel(): unitOfWork (UoW).instanceID --> {}", unitOfWork.getInstanceID());
    		LOG.debug(".registerSOAParcel(): unitOfWork (UoW).typeID --> {}", unitOfWork.getTypeID());
    		LOG.debug(".registerSOAParcel(): unitOfWork (UoW).payloadTopicID --> {}", unitOfWork.getPayloadTopicID());
    		LOG.debug(".registerSOAParcel(): unitOfWork (UoW).ingresContent --> {}", unitOfWork.getIngresContent());
    		LOG.debug(".registerSOAParcel(): unitOfWork (UoW).egressContent --> {}", unitOfWork.getEgressContent());
    		LOG.debug(".registerSOAParcel(): unitOfWork (UoW).payloadTopicID --> {}", unitOfWork.getPayloadTopicID());
    		LOG.debug(".registerSOAParcel(): unitOfWork (UoW).processingOutcome --> {}", unitOfWork.getProcessingOutcome());
        }
        LOG.trace(".registerParcel(): Checking and/or Creating a SOAWUAEpisde ID");

        if(!activityID.hasPresentEpisodeIdentifier()) {
        	FDN newWUAFDN = new FDN(activityID.getPresentWUPFunctionToken().getAsSingleFDNToken());
        	FDN uowTypeFDN = new FDN(unitOfWork.getTypeID());
        	newWUAFDN.appendFDN(uowTypeFDN);
        	EpisodeIdentifier wuaEpisodeToken = new EpisodeIdentifier(newWUAFDN.getToken());
        	activityID.setPresentEpisodeIdentifier(wuaEpisodeToken);
        }
        // 1st, lets register the parcel
        LOG.trace(".registerSOAParcel(): check for existing ResilienceParcel instance for this WUP/UoW combination");
        ResilienceParcel parcelInstance =  parcelCacheDM.getCurrentParcelForWUP(activityID.getPresentWUPIdentifier(), unitOfWork.getTypeID());
        if(parcelInstance != null){
            LOG.trace(".registerSOAParcel(): Well, there seems to be a Parcel already for this WUPInstanceID/UoWInstanceID. Odd, but let's use it!");
        } else {
            LOG.trace(".registerSOAParcel(): Attempted to retrieve existing ResilienceParcel, and there wasn't one, so let's create it!");
            parcelInstance = new ResilienceParcel(activityID, unitOfWork);
            parcelCacheDM.addParcel(parcelInstance);
            LOG.trace(".registerSOAParcel(): Set the PresentParcelInstanceID in the ActivityID (ActivityID), ParcelInstanceID --> {}", parcelInstance.getIdentifier());
            activityID.setPresentParcelIdentifier(parcelInstance.getIdentifier());
            Date registrationDate = Date.from(Instant.now());
            LOG.trace(".registerSOAParcel(): Set the Registration Date --> {}", registrationDate);
            parcelInstance.setRegistrationDate(registrationDate);
            LOG.trace(".registerSOAParcel(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
            parcelInstance.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
            LOG.trace(".registerSOAParcel(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
            parcelInstance.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
            LOG.trace(".registerSOAParcel(): Doing an Audit Write");
            auditWriter.writeAuditEntry(parcelInstance, true);
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug(".registerParcel(): Exit");
            LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).episodeID --> {}", parcelInstance.getEpisodeIdentifier());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).upsteamEpisodeID --> {}", parcelInstance.getUpstreamEpisodeIdentifier());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).parcelInstanceID --> {}", parcelInstance.getIdentifier());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).associatedWUPInstanceID --> {}", parcelInstance.getAssociatedWUPIdentifier());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).processingStatus --> {}", parcelInstance.getProcessingStatus());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).typeID --> {}", parcelInstance.getTypeID());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).registrationDate --> {}", parcelInstance.getRegistrationDate());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).startDate --> {}", parcelInstance.getStartDate());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).finishedDate --> {}", parcelInstance.getFinishedDate());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).finalisationDate --> {}", parcelInstance.getFinalisationDate());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).finalisationStatus --> {}", parcelInstance.getFinalisationStatus());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).cancellationDate --> {}", parcelInstance.getCancellationDate());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).instanceID --> {}", parcelInstance.getActualUoW().getInstanceID());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).typeID --> {}", parcelInstance.getActualUoW().getTypeID());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", parcelInstance.getActualUoW().getPayloadTopicID());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).ingresContent --> {}", parcelInstance.getActualUoW().getIngresContent());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).egressContent --> {}", parcelInstance.getActualUoW().getEgressContent());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", parcelInstance.getActualUoW().getPayloadTopicID());
        	LOG.debug(".registerSOAParcel(): parcelInstance (ResilienceParcel).actualUoW (UoW).processingOutcome --> {}", parcelInstance.getActualUoW().getProcessingOutcome());
        }
        return(parcelInstance);
    }

    @Transactional
    public ResilienceParcel notifySOAParcelProcessingStart(ResilienceParcelIdentifier parcelID) {
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

    /**
     * This function notifies the "system" that a SOA transaction has finished (successfully). Note that, for SOA,
     * "finish" and "finalised" mean the same thing and so we set the ResilienceParcelFinalisationStatusEnum
     * to PARCEL_FINALISATION_STATUS_FINALISED.
     *
     * @param parcelID The relevant Parcel Identifier for the activity (for the originating WUP)
     * @param unitOfWork The (successfully completed) Unit of Work (UoW)
     * @return A ResilienceParcel with ALL the attributes updated accordingly.
     */
    @Transactional
    public ResilienceParcel notifySOAParcelProcessingFinish(ResilienceParcelIdentifier parcelID, UoW unitOfWork) {
        if(LOG.isDebugEnabled()) {
        	LOG.debug(".notifyParcelProcessingFinish(): Entry");
    		LOG.debug(".notifyParcelProcessingFinish(): parcelID (FDNToken) --> {}", parcelID);
    		LOG.debug(".notifyParcelProcessingFinish(): unitOfWork (UoW).instanceID --> {}", unitOfWork.getInstanceID());
    		LOG.debug(".notifyParcelProcessingFinish(): unitOfWork (UoW).typeID --> {}", unitOfWork.getTypeID());
    		LOG.debug(".notifyParcelProcessingFinish(): unitOfWork (UoW).payloadTopicID --> {}", unitOfWork.getPayloadTopicID());
    		LOG.debug(".notifyParcelProcessingFinish(): unitOfWork (UoW).ingresContent --> {}", unitOfWork.getIngresContent());
    		LOG.debug(".notifyParcelProcessingFinish(): unitOfWork (UoW).egressContent --> {}", unitOfWork.getEgressContent());
    		LOG.debug(".notifyParcelProcessingFinish(): unitOfWork (UoW).payloadTopicID --> {}", unitOfWork.getPayloadTopicID());
    		LOG.debug(".notifyParcelProcessingFinish(): unitOfWork (UoW).processingOutcome --> {}", unitOfWork.getProcessingOutcome());
        } 
        if ((unitOfWork == null) || (parcelID == null)) {
            throw (new IllegalArgumentException("unitOfWork or parcelID are null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingFinish(): retrieve existing Parcel");
        ResilienceParcel currentParcel = parcelCacheDM.getParcelInstance(parcelID);
        if(LOG.isTraceEnabled()){
            LOG.debug(".notifyParcelProcessingFinish(): Parcel Retrieved, contents:");
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).episodeIdentifier --> {}", currentParcel.getEpisodeIdentifier());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).upsteamEpisodeIdentifier --> {}", currentParcel.getUpstreamEpisodeIdentifier());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).identifier --> {}", currentParcel.getIdentifier());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).associatedWUPIdentifier --> {}", currentParcel.getAssociatedWUPIdentifier());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).processingStatus --> {}", currentParcel.getProcessingStatus());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).typeID --> {}", currentParcel.getTypeID());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).registrationDate --> {}", currentParcel.getRegistrationDate());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).startDate --> {}", currentParcel.getStartDate());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).finishedDate --> {}", currentParcel.getFinishedDate());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).finalisationDate --> {}", currentParcel.getFinalisationDate());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).finalisationStatus --> {}", currentParcel.getFinalisationStatus());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).cancellationDate --> {}", currentParcel.getCancellationDate());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).instanceID --> {}", currentParcel.getActualUoW().getInstanceID());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).typeID --> {}", currentParcel.getActualUoW().getTypeID());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", currentParcel.getActualUoW().getPayloadTopicID());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).ingresContent --> {}", currentParcel.getActualUoW().getIngresContent());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).egressContent --> {}", currentParcel.getActualUoW().getEgressContent());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", currentParcel.getActualUoW().getPayloadTopicID());
            LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).processingOutcome --> {}", currentParcel.getActualUoW().getProcessingOutcome());
        }
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
        LOG.trace(".notifyParcelProcessingFinish(): Set the Finalisation Date --> {}", finishDate);
        currentParcel.setFinalisationDate(finishDate);
        LOG.trace(".notifyParcelProcessingFinish(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
        currentParcel.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
        LOG.trace(".notifyParcelProcessingFinish(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
        currentParcel.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
        LOG.trace(".notifyParcelProcessingFinish(): Doing an Audit Write, note that it is synchronous by design");
        auditWriter.writeAuditEntry(currentParcel,true);
        if(LOG.isDebugEnabled()) {
        	LOG.debug(".notifyParcelProcessingFinish(): Exit, returning finished Parcel");
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).episodeIdentifier --> {}", currentParcel.getEpisodeIdentifier());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).upsteamEpisodeIdentifier --> {}", currentParcel.getUpstreamEpisodeIdentifier());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).identifier --> {}", currentParcel.getIdentifier());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).associatedWUPIdentifier --> {}", currentParcel.getAssociatedWUPIdentifier());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).processingStatus --> {}", currentParcel.getProcessingStatus());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).typeID --> {}", currentParcel.getTypeID());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).registrationDate --> {}", currentParcel.getRegistrationDate());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).startDate --> {}", currentParcel.getStartDate());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).finishedDate --> {}", currentParcel.getFinishedDate());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).finalisationDate --> {}", currentParcel.getFinalisationDate());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).finalisationStatus --> {}", currentParcel.getFinalisationStatus());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).cancellationDate --> {}", currentParcel.getCancellationDate());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).instanceID --> {}", currentParcel.getActualUoW().getInstanceID());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).typeID --> {}", currentParcel.getActualUoW().getTypeID());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", currentParcel.getActualUoW().getPayloadTopicID());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).ingresContent --> {}", currentParcel.getActualUoW().getIngresContent());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).egressContent --> {}", currentParcel.getActualUoW().getEgressContent());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).payloadTopicID --> {}", currentParcel.getActualUoW().getPayloadTopicID());
        	LOG.debug(".notifyParcelProcessingFinish(): parcelInstance (ResilienceParcel).actualUoW (UoW).processingOutcome --> {}", currentParcel.getActualUoW().getProcessingOutcome());
        }
        return(currentParcel);
    }

    @Transactional
    public ResilienceParcel notifySOAParcelProcessingFailure(ResilienceParcelIdentifier parcelID, UoW unitOfWork) {
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
        auditWriter.writeAuditEntry(currentParcel,true);
        LOG.debug(".notifyParcelProcessingFailure(): Exit, returning failed Parcel --> {}", currentParcel);
        return(currentParcel);
    }

    /*
    @Transactional
    public ResilienceParcel notifySOAParcelProcessingFinalisation(ResilienceParcelIdentifier parcelID) {
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
        auditWriter.writeAuditEntry(currentParcel,true);
        LOG.debug(".notifyParcelProcessingFinalisation(): Exit, returning finished Parcel --> {}", currentParcel);
        return(currentParcel);
    }
    */

    @Transactional
    public ResilienceParcel notifySOAParcelProcessingCancellation(ResilienceParcelIdentifier parcelID) {
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
    public void notifySOAParcelProcessingPurge(ResilienceParcelIdentifier parcelID) {
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
