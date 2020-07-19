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

package net.fhirfactory.pegacorn.petasos.resilience.servicemodule.manager.tasks;

import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.cache.ServiceModuleActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.topology.properties.ServiceModuleProperties;
import net.fhirfactory.pegacorn.petasos.model.pathway.ContinuityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class RegisterNewWorkUnitActivity {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterNewWorkUnitActivity.class);

    @Inject
    ServiceModuleActivityMatrixDM activityMatrixDM;

    @Inject
    ServiceModuleProperties moduleProperties;

    public ParcelStatusElement doTask(WUPJobCard submittedJobCard){
        LOG.debug(".doTask(): Now register the parcel with the ActivityMatrix, submittedJobCard -- {}", submittedJobCard);
        if (submittedJobCard == null) {
            throw (new IllegalArgumentException(".doTask(): submittedJobCard is null"));
        }
        ContinuityID activityID = submittedJobCard.getCardID();
        ParcelStatusElement newStatusElement;
        switch (moduleProperties.getDeploymentMode()) {
            case RESILIENCE_MODE_MULTISITE:
                LOG.trace(".doTask(): Asking for -Multisite- Reliability Mode for Work Unit Activity Registration");
                switch (moduleProperties.getWUAConcurrencyMode()) {
                    case CONCURRENCY_MODE_CONCURRENT:   // Woo hoo - we are full-on highly available
                        LOG.trace(".doTask(): Asking for -Concurrent- Concurrency Mode, in -Multisite- Reliability Mode - implementing Multisite/Concurrent mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                    case CONCURRENCY_MODE_STANDALONE:   // WTF - why bother!
                        LOG.trace(".doTask(): Asking for -Standalone- Concurrency Mode, in -Multisite- Reliability Mode - not possible, defaulting to Multisite/OnDemand mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                    case CONCURRENCY_MODE_ONDEMAND:     // make it reliable, scalable
                    default:
                        LOG.trace(".doTask(): Asking for -OnDemand- Concurrency Mode, in -Multisite- Reliability Mode - implementing Multisite/OnDemand mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                }
            case RESILIENCE_MODE_CLUSTERED:
                LOG.trace(".doTask(): Asking for -Clustered- Reliability Mode for Work Unit Activity Registration");
                switch (moduleProperties.getWUAConcurrencyMode()) {
                    case CONCURRENCY_MODE_ONDEMAND:     // OK, preferred & MVP
                        LOG.trace(".doTask(): Asking for -On-Demand- Concurrency Mode, in -Clustered- Reliability Mode - implementing Clustered/OnDemand mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);;
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                    case CONCURRENCY_MODE_CONCURRENT:   // Not possible
                        LOG.trace(".doTask(): Asking for -Concurrent- Concurrency Mode, in -Clustered- Reliability Mode - not possible, defaulting to Clustered/OnDemand mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);;
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                    case CONCURRENCY_MODE_STANDALONE:   // A waste, we can have multiple - but only want one!
                    default:
                        LOG.trace(".doTask(): Asking for -Standalone- Concurrency Mode, in -Clustered- Reliability Mode - not possible, defaulting to Clustered/OnDemand mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);;
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                }

            case RESILIENCE_MODE_STANDALONE:
                LOG.trace(".doTask(): Asking for -Standalone- Reliability Mode for Work Unit Activity Registration");
            default:
                switch (moduleProperties.getWUAConcurrencyMode()) {
                    case CONCURRENCY_MODE_CONCURRENT:   // Not possible!
                        LOG.trace(".doTask(): Asking for -Concurrent- Concurrency Mode, in -Standalone- Reliability Mode - not possible, defaulting to Standalone/Standalone mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                    case CONCURRENCY_MODE_ONDEMAND:     // Not possible!
                        LOG.trace(".doTask(): Asking for -On-Demand- Concurrency Mode, in -Standalone- Reliability Mode - not possible, defaulting to Standalone/Standalone mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                    case CONCURRENCY_MODE_STANDALONE:   // Really only good for PoCs and Integration Testing
                    default:
                        LOG.trace(".doTask(): Defaulting to -Standalone-/-Standalone- Reliability/Concurrency Mode");
                        newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
                        LOG.debug(".doTask(): Exit, newStatusElement --> {}", newStatusElement);
                        return (newStatusElement);
                }
        }
    }
}
