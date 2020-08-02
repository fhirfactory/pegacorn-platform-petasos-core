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

package net.fhirfactory.pegacorn.petasos.wup.helper;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.wup.PetasosServicesBroker;
import net.fhirfactory.pegacorn.petasos.model.pathway.ContinuityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPActivityStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;

public class IngresActivityBeginRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(IngresActivityBeginRegistration.class);

    @Inject
    TopologyIM topologyIM;

    @Inject
    PetasosServicesBroker servicesBroker;

    public void registerActivityStart(UoW unitOfWork, Exchange camelExchange, NodeElementFunctionToken wupfunctionToken, FDNToken wupInstanceID){
        LOG.debug(".registerActivityStart(): Entry, unitOfWork --> {}, wupTypeID --> {}, wupInstanceID --> {}", unitOfWork, wupfunctionToken, wupInstanceID);
        LOG.trace(".registerActivityStart(): Building the ActivityID for this activity");
        ContinuityID newActivityID = new ContinuityID();
        newActivityID.setPresentWUPFunctionToken(wupfunctionToken);
        newActivityID.setPresentWUPInstanceID(wupInstanceID);
        LOG.trace(".registerActivityStart(): Creating new JobCard");
        WUPJobCard activityJobCard = new WUPJobCard(newActivityID, WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING, WUPActivityStatusEnum.WUP_ACTIVITY_STATUS_EXECUTING, topologyIM.getConcurrencyMode(wupInstanceID), topologyIM.getDeploymentResilienceMode(wupInstanceID),  Date.from(Instant.now()));
        LOG.trace(".registerActivityStart(): Registering the Work Unit Activity using the activityJobCard --> {} and UoW --> {}", activityJobCard, unitOfWork);
        ParcelStatusElement statusElement = servicesBroker.registerSystemEdgeWUA(activityJobCard, unitOfWork);
        LOG.trace(".registerActivityStart(): Registration aftermath: statusElement --> {}", statusElement);
        // Now we have to Inject some details into the Exchange so that the WUPEgressConduit can extract them as per standard practice
        LOG.trace(".registerActivityStart(): Injecting Job Card and Status Element into Exchange for extraction by the WUP Egress Conduit");
        camelExchange.setProperty("WUPJobCard", activityJobCard); // <-- Note the "WUPJobCard" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        camelExchange.setProperty("ParcelStatusElement", statusElement); // <-- Note the "ParcelStatusElement" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        LOG.debug(".registerActivityStart(): exit");
    }
}
