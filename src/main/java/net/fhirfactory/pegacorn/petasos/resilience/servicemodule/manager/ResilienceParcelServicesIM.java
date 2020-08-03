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

package net.fhirfactory.pegacorn.petasos.resilience.servicemodule.manager;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.audit.api.PetasosAuditWriter;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.cache.ServiceModuleParcelCacheDM;
import net.fhirfactory.pegacorn.petasos.model.pathway.ContinuityID;
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
import net.fhirfactory.pegacorn.petasos.topology.manager.proxies.ServiceModuleTopologyProxy;

/**
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class ResilienceParcelServicesIM {
    private static final Logger LOG = LoggerFactory.getLogger(ResilienceParcelServicesIM.class);
//    private FDN nodeInstanceFDN;

    @Inject
    ServiceModuleParcelCacheDM parcelCacheDM;

    @Inject
    PetasosAuditWriter auditWriter;
    
//    public ResilienceParcelServicesIM() {
//        this.nodeInstanceFDN = new FDN();
//    }

    @Transactional
    public ResilienceParcel registerParcel(ContinuityID activityID, UoW unitOfWork, boolean synchronousWriteToAudit) {
        LOG.debug(".registerParcel(): Entry, activityID --> {}, unitOfWork --> {}, synchronousWriteToAudit -->{}", activityID, unitOfWork, synchronousWriteToAudit);
        if ((unitOfWork == null) || (activityID == null)) {
            throw (new IllegalArgumentException("unitOfWork, wupTypeID or wupInstanceID are null in method invocation"));
        }
        // 1st, lets register the parcel
        LOG.trace(".registerParcel(): check for existing ResilienceParcel instance for this WUP/UoW combination");
        ResilienceParcel parcelInstance =  parcelCacheDM.getCurrentParcelForWUP(activityID.getPresentParcelInstanceID(), unitOfWork.getInstanceID());
        if(parcelInstance != null){
            LOG.trace(".registerParcel(): Well, there seems to be a Parcel already for this WUPInstanceID/UoWInstanceID. Odd, but let's use it!");
        } else {
            LOG.trace(".registerParcel(): Attempted to retrieve existing ResilienceParcel, and there wasn't one, so let's create it!");
            parcelInstance = new ResilienceParcel(activityID, unitOfWork);
            parcelCacheDM.addParcel(parcelInstance);
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

        // now, lets register the parcel with the AcitivityMatrix and check where things are at
        // TODO check status when Registering a new Parcel in the ActivityMatrix
        LOG.debug(".registerParcel(): Exit, returning registered Parcel --> {}", parcelInstance);
        return(parcelInstance);
    }

    @Transactional
    public ResilienceParcel notifyParcelProcessingStart(FDNToken parcelID) {
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
    public ResilienceParcel notifyParcelProcessingFinish(FDNToken parcelID, UoW unitOfWork) {
        LOG.debug(".notifyParcelProcessingFinish(): Entry, parcelID --> {}, unitOfWork --> {}", parcelID, unitOfWork);
        if ((unitOfWork == null) || (parcelID == null)) {
            throw (new IllegalArgumentException("unitOfWork or parcelID are null in method invocation"));
        }
        LOG.trace(".notifyParcelProcessingFinish(): retrieve existing Parcel");
        ResilienceParcel currentParcel = parcelCacheDM.getParcelInstance(parcelID);
        LOG.trace(".notifyParcelProcessingFinish(): update the UoW (Egress Content)");
        currentParcel.getActualUoW().setEgressContent(unitOfWork.getEgressContent());
        LOG.trace(".notifyParcelProcessingFinish(): update the UoW Processing Outcome --> {}", unitOfWork.getProcessingOutcome());
        currentParcel.getActualUoW().setProcessingOutcome(unitOfWork.getProcessingOutcome());
        Date finishDate = Date.from(Instant.now());
        LOG.trace(".notifyParcelProcessingFinish(): Set the Finish Date --> {}", finishDate);
        currentParcel.setFinishedDate(finishDate);
        LOG.trace(".notifyParcelProcessingFinish(): Set the Parcel Finalisation Status --> {} ", ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        currentParcel.setFinalisationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
        LOG.trace(".notifyParcelProcessingFinish(): Set the Parcel Processing Status --> {}", ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
        currentParcel.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
        // TODO Check to see if we should do an Audit Entry when we finish processing
        // LOG.trace(".notifyParcelProcessingFinish(): Doing an Audit Write, note that it is asynchronous by design");
        // auditWriter.writeAuditEntry(currentParcel,false);
        LOG.debug(".notifyParcelProcessingFinish(): Exit, returning finished Parcel --> {}", currentParcel);
        return(currentParcel);
    }

    @Transactional
    public ResilienceParcel notifyParcelProcessingFailure(FDNToken parcelID, UoW unitOfWork) {
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
    public ResilienceParcel notifyParcelProcessingFinalisation(FDNToken parcelID) {
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
    public ResilienceParcel notifyParcelProcessingCancellation(FDNToken parcelID) {
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
    public void notifyParcelProcessingPurge(FDNToken parcelID) {
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
