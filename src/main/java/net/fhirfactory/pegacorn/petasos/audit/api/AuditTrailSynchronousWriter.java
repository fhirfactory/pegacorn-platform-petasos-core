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

package net.fhirfactory.pegacorn.petasos.audit.api;

import net.fhirfactory.pegacorn.petasos.audit.forwarder.integration.HestiaAuditWriter;
import net.fhirfactory.pegacorn.petasos.audit.model.PetasosParcelAuditTrailEntry;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AuditTrailSynchronousWriter {
    private static final Logger LOG = LoggerFactory.getLogger(AuditTrailSynchronousWriter.class);

    @Inject
    HestiaAuditWriter hestiaAuditLog;

    public boolean synchronousWrite(ResilienceParcel parcel){
        PetasosParcelAuditTrailEntry newAuditEntry = new PetasosParcelAuditTrailEntry(parcel);
        boolean success = hestiaAuditLog.writeAuditEntry(newAuditEntry);
        return(success);
    }
}