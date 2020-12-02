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

package net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.manager;

import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.EpisodeIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.cache.ProcessingPlantWUAEpisodeActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.cache.ProcessingPlantWUAEpisodeFinalisationCacheDM;
import net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.manager.tasks.RegisterNewMOAWorkUnitActivityTask;
import net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.manager.tasks.SynchroniseMOAWorkUnitActivityJobCardTask;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 */
@ApplicationScoped
public class ProcessingPlantResilienceActivityServicesController {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingPlantResilienceActivityServicesController.class);

    @Inject
    ProcessingPlantWUAEpisodeActivityMatrixDM activityMatrixDM;

    @Inject
    ProcessingPlantWUAEpisodeFinalisationCacheDM finalisationCacheDM;
    
    //task Specific Classes

    @Inject
    RegisterNewMOAWorkUnitActivityTask wuaRegistry;

    @Inject
    SynchroniseMOAWorkUnitActivityJobCardTask taskSynchroniseWUA;


    public ParcelStatusElement registerNewWorkUnitActivity(WUPJobCard jobCard) {
        LOG.debug(".registerNewWorkUnitActivity(): Entry, activityID --> {}, statusEnum --> {}", jobCard);
        if (jobCard == null) {
            return (null);
        }
        ParcelStatusElement parcelStatusElement = wuaRegistry.registerNewWUA(jobCard);
        synchroniseJobCard(jobCard);
        LOG.debug(".registerNewWorkUnitActivity(): Exit, parcelStatusElement --> {}", parcelStatusElement);
        return (parcelStatusElement);
    }

    public void synchroniseJobCard(WUPJobCard submittedJobCard){
        if( submittedJobCard == null){
            return;
        }
        taskSynchroniseWUA.synchroniseJobCard(submittedJobCard);
    }



    public ParcelStatusElement getStatusElement(ResilienceParcelIdentifier parcelInstanceID){
        LOG.debug(".getStatusElement(): Entry, parcelInstanceID --> {}", parcelInstanceID);
        ParcelStatusElement retrievedElement = activityMatrixDM.getParcelStatusElement(parcelInstanceID);
        LOG.debug(".getStatusElement(): Exit, retrievedElement --> {}", retrievedElement);
        return(retrievedElement);
    }

    public void registerWUAEpisodeDownstreamWUPInterest(EpisodeIdentifier wuaEpisodeID, WUPFunctionToken downstreamWUPInstanceID) {
        finalisationCacheDM.registerDownstreamWUPInterest(wuaEpisodeID,downstreamWUPInstanceID);
    }


}
