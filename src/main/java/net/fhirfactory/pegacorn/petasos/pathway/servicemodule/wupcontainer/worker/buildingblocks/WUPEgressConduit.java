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

package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.buildingblocks;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPActivityStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;

/**
 * @author Mark A. Hunter
 * @since 2020-07-05
 */
public class WUPEgressConduit {
    private static final Logger LOG = LoggerFactory.getLogger(WUPEgressConduit.class);

    /**
     * This function reconstitutes the WorkUnitTransportPacket by extracting the WUPJobCard and ParcelStatusElement
     * from the Camel Exchange, and injecting them plus of the UoW into it.
     *
     * @param incomingUoW   The Unit of Work (UoW) received as output from the actual Work Unit Processor (Business Logic)
     * @param camelExchange The Apache Camel Exchange object, for extracting the WUPJobCard & ParcelStatusElement from
     * @param wupFunctionToken     The Work Unit Processor Type - should be unique within a ServiceModule and is the reference point for logic
     * @param wupInstanceID The Work Unit Processor Instance - an absolutely unique identifier for the instance of WUP within the entiry deployment.
     * @return A WorkUnitTransportPacket object for relay to the other
     */
    public WorkUnitTransportPacket receiveFromWUP(UoW incomingUoW, Exchange camelExchange, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID) {
        LOG.debug(".receiveFromWUP(): Entry, incomingUoW --> {}, wupFunctionToken --> {}, wupInstanceID --> {}", incomingUoW, wupFunctionToken, wupInstanceID);
        RouteElementNames elementNames = new RouteElementNames(wupFunctionToken);
        WUPJobCard jobCard = camelExchange.getProperty("WUPJobCard", WUPJobCard.class);
        ParcelStatusElement statusElement = camelExchange.getProperty("ParcelStatusElement", ParcelStatusElement.class);
        WorkUnitTransportPacket transportPacket = new WorkUnitTransportPacket(elementNames.getEndPointWUPEgressConduitEgress(), Date.from(Instant.now()), incomingUoW);
        LOG.trace(".receiveFromWUP(): We only want to check if the UoW was successful and modify the JobCard/StatusElement accordingly.");
        LOG.trace(".receiveFromWUP(): All detailed checking of the Cluster/SiteWide details is done in the WUPContainerEgressProcessor");
        switch (incomingUoW.getProcessingOutcome()) {
            case UOW_OUTCOME_SUCCESS:
                LOG.trace(".receiveFromWUP(): UoW was processed successfully - updating JobCard/StatusElement to FINISHED!");
                jobCard.setCurrentStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FINISHED);
                jobCard.setRequestedStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FINISHED);
                statusElement.setParcelStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
                statusElement.setEntryDate(Date.from(Instant.now()));
                break;
            case UOW_OUTCOME_NOTSTARTED:
            case UOW_OUTCOME_INCOMPLETE:
            case UOW_OUTCOME_FAILED:
            default:
                LOG.trace(".receiveFromWUP(): UoW was not processed or processing failed - updating JobCard/StatusElement to FAILED!");
                jobCard.setCurrentStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FAILED);
                jobCard.setRequestedStatus(WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_FAILED);
                statusElement.setParcelStatus(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
                statusElement.setEntryDate(Date.from(Instant.now()));
                break;
        }
        transportPacket.setCurrentJobCard(jobCard);
        transportPacket.setCurrentParcelStatus(statusElement);
        transportPacket.setToElement(elementNames.getEndPointWUPContainerEgressProcessorIngres());
        LOG.debug(".forwardIntoWUP(): Exit, transportPacket --> {}", transportPacket);
        return (transportPacket);
    }
}
