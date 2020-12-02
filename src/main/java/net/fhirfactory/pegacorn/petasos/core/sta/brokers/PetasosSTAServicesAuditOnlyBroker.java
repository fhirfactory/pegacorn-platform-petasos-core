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

import java.time.Instant;
import java.util.Date;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.sta.STATransaction;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.audit.api.PetasosAuditWriter;
import net.fhirfactory.pegacorn.petasos.audit.model.PetasosParcelAuditTrailEntry;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelFinalisationStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;

@ApplicationScoped
public class PetasosSTAServicesAuditOnlyBroker {

    private static final Logger LOG = LoggerFactory.getLogger(PetasosSTAServicesAuditOnlyBroker.class);

    @Inject
    PetasosAuditWriter auditWriter;

    public PetasosParcelAuditTrailEntry transactionAuditEntry(WUPIdentifier wup, String action, UoW theUoW, PetasosParcelAuditTrailEntry previousTransaction) {
        if(LOG.isDebugEnabled()){
            LOG.debug(".transactionAuditEntry(): Entry, ");
            LOG.debug(".transactionAuditEntry(): Entry, wup (WUPIdentifier) --> {}", wup);
            LOG.debug(".transactionAuditEntry(): Entry, action (String) --> {}", action);
            LOG.debug(".transactionAuditEntry(): Entry, theUoW (UoW) --> {}", theUoW);
            LOG.debug(".transactionAuditEntry(): Entry, previousTransactio (PetasosParcelAuditTrailEntry) --> {}", previousTransaction);
        }
        if ((wup == null) || (action == null) || (theUoW == null)) {
            throw (new IllegalArgumentException(".writeAuditEntry(): wup, action or theUoW are null"));
        }
        PetasosParcelAuditTrailEntry newAuditEntry = new PetasosParcelAuditTrailEntry();
        newAuditEntry.setAuditTrailEntryDate(Date.from(Instant.now()));
        newAuditEntry.setActualUoW(theUoW);
        if (previousTransaction != null) {
            newAuditEntry.setParcelTypeID(previousTransaction.getParcelTypeID());
            newAuditEntry.setIdentifier(previousTransaction.getIdentifier());
        } else {
            FDN auditEntryType = new FDN();
            auditEntryType.appendFDN(new FDN(theUoW.getTypeID()));
            auditEntryType.appendRDN(new RDN("action", action));
            newAuditEntry.setParcelTypeID(auditEntryType.getToken());
            FDN auditEntryIdentifier = new FDN();
            auditEntryIdentifier.appendFDN(new FDN(theUoW.getInstanceID()));
            auditEntryIdentifier.appendRDN(new RDN("action", action));
            ResilienceParcelIdentifier parcelId = new ResilienceParcelIdentifier(auditEntryIdentifier.getToken());
            newAuditEntry.setIdentifier(parcelId);
        }
        switch (theUoW.getProcessingOutcome()) {
            case UOW_OUTCOME_SUCCESS: {
                newAuditEntry.setParcelFinalsationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
                newAuditEntry.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
                newAuditEntry.setParcelFinalisedDate(Date.from(Instant.now()));
                newAuditEntry.setParcelFinishedDate(Date.from(Instant.now()));
                if (previousTransaction != null) {
                    newAuditEntry.setParcelRegistrationDate(previousTransaction.getParcelRegistrationDate());
                    newAuditEntry.setParcelStartDate(previousTransaction.getParcelStartDate());
                }
                break;
            }
            case UOW_OUTCOME_FAILED: {
                newAuditEntry.setParcelFinalsationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_FINALISED);
                newAuditEntry.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
                newAuditEntry.setParcelFinalisedDate(Date.from(Instant.now()));
                newAuditEntry.setParcelFinishedDate(Date.from(Instant.now()));
                if (previousTransaction != null) {
                    newAuditEntry.setParcelRegistrationDate(previousTransaction.getParcelRegistrationDate());
                    newAuditEntry.setParcelStartDate(previousTransaction.getParcelStartDate());
                }
                break;
            }
            case UOW_OUTCOME_INCOMPLETE:
            case UOW_OUTCOME_NOTSTARTED: {
                newAuditEntry.setParcelFinalsationStatus(ResilienceParcelFinalisationStatusEnum.PARCEL_FINALISATION_STATUS_NOT_FINALISED);
                newAuditEntry.setProcessingStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE);
                newAuditEntry.setParcelStartDate(Date.from(Instant.now()));
                newAuditEntry.setParcelRegistrationDate(Date.from(Instant.now()));
            }
        }
        newAuditEntry.setPrimaryWUPIdentifier(wup);
        auditWriter.writeAuditEntry(newAuditEntry, true);
        return (newAuditEntry);
    }
}
