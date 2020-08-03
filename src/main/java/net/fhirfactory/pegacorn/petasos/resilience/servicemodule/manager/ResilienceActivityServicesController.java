/*
 * Copyright (c) 2020 Mark A. Hunter
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

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.cache.ServiceModuleActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.cache.ServiceModuleWUAEpisodeFinalisationCacheDM;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.manager.tasks.RegisterNewWorkUnitActivity;
import net.fhirfactory.pegacorn.petasos.resilience.servicemodule.manager.tasks.SynchroniseWorkUnitActivityJobCard;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;


@ApplicationScoped
public class ResilienceActivityServicesController {
    private static final Logger LOG = LoggerFactory.getLogger(ResilienceActivityServicesController.class);

    @Inject
    ServiceModuleActivityMatrixDM activityMatrixDM;

    @Inject
    TopologyIM topologyServer;

    @Inject
    ServiceModuleWUAEpisodeFinalisationCacheDM finalisationCacheDM;     
    
    //task Specific Classes

    @Inject
    RegisterNewWorkUnitActivity taskRegisterWUA;

    @Inject
    SynchroniseWorkUnitActivityJobCard taskSynchroniseWUA;


    public ParcelStatusElement registerNewWorkUnitActivity(WUPJobCard jobCard) {
        LOG.debug(".checkForExistingSystemWideFocusedElement(): Entry, activityID --> {}, statusEnum --> {}", jobCard);
        if (jobCard == null) {
            return (null);
        }
        ParcelStatusElement parcelStatusElement = taskRegisterWUA.doTask(jobCard);
        LOG.debug(".checkForExistingSystemWideFocusedElement(): Exit, parcelStatusElement --> {}", parcelStatusElement);
        return (parcelStatusElement);
    }

    public void synchroniseJobCard(WUPJobCard submittedJobCard){
        if( submittedJobCard == null){
            return;
        }
        taskSynchroniseWUA.doTask(submittedJobCard);
    }



    public ParcelStatusElement getStatusElement(FDNToken parcelInstanceID){
        LOG.debug(".getStatusElement(): Entry, parcelInstanceID --> {}", parcelInstanceID);
        ParcelStatusElement retrievedElement = activityMatrixDM.getParcelStatusElement(parcelInstanceID);
        LOG.debug(".getStatusElement(): Exit, retrievedElement --> {}", retrievedElement);
        return(retrievedElement);
    }

    public void registerWUAEpisodeDownstreamWUPInterest(FDNToken wuaEpisodeID, FDNToken downstreamWUPInstanceID) {
        finalisationCacheDM.registerDownstreamWUPInterest(wuaEpisodeID,downstreamWUPInstanceID);
    }


}
