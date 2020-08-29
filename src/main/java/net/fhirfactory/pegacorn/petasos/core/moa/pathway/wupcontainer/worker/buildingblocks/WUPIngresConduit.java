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

package net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;

/**
 * @author Mark A. Hunter
 * @since 2020-07-05
 */
@ApplicationScoped
public class WUPIngresConduit {
    private static final Logger LOG = LoggerFactory.getLogger(WUPIngresConduit.class);
    /**
     * This function strips the WUPJobCard and ParcelStatusElement from the ingresParcel, and injects them into the
     * Camel Exchange element for extraction by the WUPEgressConduit module. It then extracts the actual UoW and
     * returns this for forwarding into the WUP itself. This way, the only thing the Business Logic developer need
     * worry about is the UoW on which they are acting.
     *
     * @param ingresParcel The WorkUnitTransportPacket for the associated UoW - containing the WUPJobCard & ParcelStatusElement for the activity
     * @param camelExchange The Apache Camel Exchange object, for injecting the WUPJobCard & ParcelStatusElement into
     * @param wupTypeID The Work Unit Processor Type - should be unique within a ServiceModule and is the reference point for logic
     * @param wupInstanceID The Work Unit Processor Instance - an absolutely unique identifier for the instance of WUP within the entiry deployment.
     * @return A UoW (Unit of Work) object for injection into the WUP for processing by the Business Logic
     */
    public UoW forwardIntoWUP(WorkUnitTransportPacket ingresParcel, Exchange camelExchange,String wupInstanceKey){
        LOG.debug(".forwardIntoWUP(): Entry, ingresParcel --> {}, wupInstanceKey --> {}", ingresParcel, wupInstanceKey);
        UoW theUoW = ingresParcel.getPayload();
        String jobcardPropertyKey = "WUPJobCard" + wupInstanceKey;
        String parcelStatusPropertyKey = "ParcelStatusElement" + wupInstanceKey;
        camelExchange.setProperty(jobcardPropertyKey, ingresParcel.getCurrentJobCard());
        camelExchange.setProperty(parcelStatusPropertyKey, ingresParcel.getCurrentParcelStatus());
        LOG.debug(".forwardIntoWUP(): Exit, returning the UoW --> {}", theUoW);
        return(theUoW);
    }
}
