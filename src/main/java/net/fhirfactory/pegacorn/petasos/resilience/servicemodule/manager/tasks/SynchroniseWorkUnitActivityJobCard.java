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

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.cache.ServiceModuleActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.topology.properties.ServiceModuleProperties;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class SynchroniseWorkUnitActivityJobCard {
    private static final Logger LOG = LoggerFactory.getLogger(SynchroniseWorkUnitActivityJobCard.class);

    @Inject
    ServiceModuleActivityMatrixDM activityMatrixDM;

    @Inject
    ServiceModuleProperties moduleProperties;

    public void doTask(WUPJobCard submittedJobCard) {
        LOG.debug(".doTask(): Entry, submittedJobCard -- {}", submittedJobCard);
        if (submittedJobCard == null) {
            throw (new IllegalArgumentException(".doTask(): submittedJobCard is null"));
        }
        switch (moduleProperties.getDeploymentMode()) {
            case RESILIENCE_MODE_MULTISITE:
                LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_MULTISITE");
                switch (moduleProperties.getWUAConcurrencyMode()) {
                    case CONCURRENCY_MODE_CONCURRENT:   // Woo hoo - we are full-on highly available
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_MULTISITE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_CONCURRENT");
                        break;
                    case CONCURRENCY_MODE_STANDALONE:   // WTF - why bother!
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_MULTISITE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_STANDALONE");
                        break;
                    case CONCURRENCY_MODE_ONDEMAND:     // make it reliable, scalable
                    default:
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_MULTISITE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_ONDEMAND (default concurrency mode)");

                }
                break;
            case RESILIENCE_MODE_CLUSTERED:
                LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_CLUSTERED");
                switch (moduleProperties.getWUAConcurrencyMode()) {
                    case CONCURRENCY_MODE_CONCURRENT:   // Not possible
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_CLUSTERED, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_CONCURRENT");
                    case CONCURRENCY_MODE_STANDALONE:   // A waste, we can have multiple - but only want one!
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_CLUSTERED, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_STANDALONE");
                    case CONCURRENCY_MODE_ONDEMAND:     // OK, preferred & MVP
                    default:
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_CLUSTERED, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_ONDEMAND (default concurrency mode)");
                }
                break;
            case RESILIENCE_MODE_STANDALONE:
                LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_STANDALONE");
            default:
                switch (moduleProperties.getWUAConcurrencyMode()) {
                    case CONCURRENCY_MODE_CONCURRENT:   // Not possible!
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_STANDALONE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_CONCURRENT (not possible)");
                    case CONCURRENCY_MODE_ONDEMAND:     // Not possible!
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_STANDALONE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_ONDEMAND (not possible)");
                    case CONCURRENCY_MODE_STANDALONE:   // Really only good for PoCs and Integration Testing
                    default:
                        LOG.trace(".doTask(): Deployment Mode --> PETASOS_MODE_STANDALONE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_STANDALONE (default concurrent mode)");
                        standaloneModeSynchroniseJobCard(submittedJobCard);
                }
        }
    }

    /**
     *
     * @param submittedJobCard
     */
    public void standaloneModeSynchroniseJobCard(WUPJobCard submittedJobCard) {
        LOG.debug(".standaloneModeSynchroniseJobCard(): Entry, submittedJobCard --> {}", submittedJobCard);
        FDNToken parcelInstanceID = submittedJobCard.getCardID().getPresentParcelInstanceID();
        FDNToken wuaEpisodeID = submittedJobCard.getCardID().getPresentWUAEpisodeID();
        LOG.trace(".standaloneModeSynchroniseJobCard(): Retrieve the ParcelStatusElement from the Cache for ParcelInstanceID --> {}", parcelInstanceID);
        ParcelStatusElement statusElement = activityMatrixDM.getParcelStatusElement(parcelInstanceID);
        LOG.trace(".standaloneModeSynchroniseJobCard(): Retrieved ParcelStatusElement --> {}", statusElement);
        LOG.trace(".standaloneModeSynchroniseJobCard(): Retrieve the ParcelInstanceSet for the wuaEpisodeID --> {}", wuaEpisodeID);
        List<ParcelStatusElement> parcelSet = activityMatrixDM.getEpisodeElementSet(wuaEpisodeID);
        if (LOG.isTraceEnabled()) {
            LOG.trace(".doTask(): The ParcelSet associated with the ParcelEpisodeID --> {} contains {} elements", wuaEpisodeID, parcelSet.size());
        }
        FDNToken systemWideFocusedParcelInstanceID = activityMatrixDM.getSiteWideFocusElement(wuaEpisodeID);
        LOG.trace(".doTask(): The Parcel with systemWideFocusedParcel --> {}", systemWideFocusedParcelInstanceID);
        FDNToken clusterFocusedParcelInstanceID = activityMatrixDM.getClusterFocusElement(wuaEpisodeID);
        LOG.trace(".doTask(): The Parcel with clusterFocusedParcel --> {}", clusterFocusedParcelInstanceID);
        if (parcelSet.isEmpty()) {
            throw (new IllegalArgumentException(".synchroniseJobCard(): There are no ResilienceParcels for the given ParcelEpisodeID --> something is very wrong!"));
        }
        LOG.trace(".doTask(): Now, again, for the standalone mode - there should only be a single thread per WUA Episode ID, so set it to have FOCUS");
        if(systemWideFocusedParcelInstanceID!=null){
            ParcelStatusElement currentSystemWideFocusElement = activityMatrixDM.getParcelStatusElement(systemWideFocusedParcelInstanceID);
            if(currentSystemWideFocusElement != null){
                currentSystemWideFocusElement.setHasSystemWideFocus(false);
            }
        }
        if(clusterFocusedParcelInstanceID!=null){
            ParcelStatusElement currentClusterFocusElement = activityMatrixDM.getParcelStatusElement(clusterFocusedParcelInstanceID);
            if(currentClusterFocusElement != null){
                currentClusterFocusElement.setHasClusterFocus(false);
            }
        }
        statusElement.setHasSystemWideFocus(true);
        statusElement.setHasClusterFocus(true);
        LOG.trace(".doTask(): Now, lets update the JobCard based on the ActivityMatrix");
        submittedJobCard.setGrantedStatus(submittedJobCard.getRequestedStatus());
        submittedJobCard.setUpdateDate(Date.from(Instant.now()));
        if(LOG.isDebugEnabled()) {
            LOG.debug(".doTask(): Exit, associated ParcelStatusElement granted status --> {}", submittedJobCard.getGrantedStatus());
        }
    }
}
