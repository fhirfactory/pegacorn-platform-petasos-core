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
import net.fhirfactory.pegacorn.petasos.wup.PetasosServicesBroker;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import net.fhirfactory.pegacorn.petasos.model.pathway.WorkUnitTransportPacket;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import static net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum.RESILIENCE_MODE_CLUSTERED;
import static net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum.RESILIENCE_MODE_MULTISITE;
import static net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum.RESILIENCE_MODE_STANDALONE;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;

/**
 * @author Mark A. Hunter
 * @since 2020-07-01
 */
public class WUPContainerEgressProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(WUPContainerEgressProcessor.class);
    RouteElementNames elementNames = null;

    @Inject
    PetasosServicesBroker petasosServicesBroker;

    @Inject
    TopologyIM topologyServer;


    public WorkUnitTransportPacket egressContentProcessor(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID) {
        LOG.debug(".egressContentProcessor(): Enter, ingresPacket --> {}, wupFunctionToken --> {}, wupInstanceID --> {}", ingresPacket, wupFunctionToken, wupInstanceID);
        WorkUnitTransportPacket egressPacket;
        switch (topologyServer.getDeploymentResilienceMode(wupInstanceID)) {
            case RESILIENCE_MODE_MULTISITE:
                LOG.trace(".egressContentProcessor(): Deployment Mode --> PETASOS_MODE_MULTISITE");
            case RESILIENCE_MODE_CLUSTERED:
                LOG.trace(".egressContentProcessor(): Deployment Mode --> PETASOS_MODE_CLUSTERED");
            case RESILIENCE_MODE_STANDALONE:
                LOG.trace(".egressContentProcessor(): Deployment Mode --> PETASOS_MODE_STANDALONE");
                egressPacket = standaloneDeploymentModeECP(ingresPacket, camelExchange, wupFunctionToken, wupInstanceID);
        }
        return (ingresPacket);
    }

    private WorkUnitTransportPacket standaloneDeploymentModeECP(WorkUnitTransportPacket ingresPacket, Exchange camelExchange, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID) {
        LOG.debug(".standaloneDeploymentModeECP(): Enter, ingresPacket --> {}, wupFunctionToken --> {}, wupInstanceID --> {}", ingresPacket, wupFunctionToken, wupInstanceID);
        elementNames = new RouteElementNames(wupFunctionToken);
        LOG.trace(".standaloneDeploymentModeECP(): Now, check if this the 1st time the associated UoW has been (attempted to be) processed");
        WorkUnitTransportPacket newTransportPacket;
        WUPJobCard jobCard = ingresPacket.getCurrentJobCard();
        ParcelStatusElement statusElement = ingresPacket.getCurrentParcelStatus();
        UoW uow = ingresPacket.getPayload();
        ResilienceParcelProcessingStatusEnum parcelProcessingStatusEnum = statusElement.getParcelStatus();
        switch (parcelProcessingStatusEnum) {
            case PARCEL_STATUS_FINISHED:
                petasosServicesBroker.notifyFinishOfWorkUnitActivity(jobCard, uow);
                break;
            case PARCEL_STATUS_ACTIVE_ELSEWHERE:
            case PARCEL_STATUS_FINISHED_ELSEWHERE:
            case PARCEL_STATUS_FINALISED_ELSEWHERE:
            case PARCEL_STATUS_REGISTERED:
            case PARCEL_STATUS_INITIATED:
            case PARCEL_STATUS_ACTIVE:
            case PARCEL_STATUS_FINALISED:
            case PARCEL_STATUS_FAILED:
            default:
                petasosServicesBroker.notifyFailureOfWorkUnitActivity(jobCard, uow);
        }
        return (ingresPacket);
    }
}
