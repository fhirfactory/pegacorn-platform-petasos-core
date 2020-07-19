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

package net.fhirfactory.pegacorn.petasos.audit.forwarder.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.fhirfactory.pegacorn.petasos.audit.model.PetasosParcelAuditTrailEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HestiaAuditWriter {
    private static final Logger LOG = LoggerFactory.getLogger(HestiaAuditWriter.class);

    public HestiaAuditWriter(){

    }

    public boolean writeAuditEntry(PetasosParcelAuditTrailEntry auditTrailEntry){
        LOG.debug(".writeAuditEntry(): Entry", auditTrailEntry );
        if( LOG.isInfoEnabled()){
            prettyPrintAuditEntry(auditTrailEntry);
        }
        return(true);
    }

    private void prettyPrintAuditEntry(PetasosParcelAuditTrailEntry auditTrailEntry){
        ObjectMapper entryObjectMapper = new ObjectMapper();
        entryObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String auditEntryString = new String();
        try {
            auditEntryString = entryObjectMapper.writeValueAsString(auditTrailEntry);
        } catch (JsonProcessingException jsonException ){
            auditEntryString = "Can't Decode. Error --> " + jsonException.getMessage();
        }
        LOG.info("Audit Entry --> " + auditEntryString);
    }
}
