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
package net.fhirfactory.pegacorn.petasos.resilience.servicemodule.cache;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.FDNTokenSet;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.ParcelStatusElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;

import net.fhirfactory.pegacorn.petasos.model.pathway.ContinuityID;

/**
 * This is the re-factored Resilience framework ActivityMatrix for
 * ResilienceParcels. It is a representational Matrix of all the Resilience
 * Parcel activity within the ServiceModule - and has hooks for supporting
 * updates from Clustered and Multi-Site equivalents.
 *
 * @author Mark A. Hunter
 * @since 2020-06-01
 */
@ApplicationScoped
public class ServiceModuleActivityMatrixDM {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceModuleActivityMatrixDM.class);

    private ConcurrentHashMap<FDNToken, ParcelStatusElement> parcelElementStatusSet;
    private ConcurrentHashMap<FDNToken, FDNTokenSet> wuaEpisode2ParcelInstanceMap;

    @Inject
    ServiceModuleParcelCacheDM parcelCacheDM;

    @Inject
    TopologyIM moduleIM;

    public ServiceModuleActivityMatrixDM() {
        parcelElementStatusSet = new ConcurrentHashMap<FDNToken, ParcelStatusElement>();
        wuaEpisode2ParcelInstanceMap = new ConcurrentHashMap<FDNToken, FDNTokenSet>();
    }

    public ParcelStatusElement addWUA(ContinuityID activityID, ResilienceParcelProcessingStatusEnum initialProcessingStatus) {
        LOG.debug(".addWUA(): Entry, activityID --> {}, initialProcessingStatus --> {}", activityID, initialProcessingStatus);
        if (activityID == null) {
            throw (new IllegalArgumentException(".registerParcelExecution(): activityID is null"));
        }
        if( parcelElementStatusSet.containsKey(activityID.getPresentParcelInstanceID()))
        {
            LOG.trace(".addWUA(): activityID already registered, let's make sure it's the same though!");
            ParcelStatusElement existingStatusElement = parcelElementStatusSet.get(activityID.getPresentParcelInstanceID());
            boolean sameInstanceID = existingStatusElement.getParcelInstanceID().equals(activityID.getPresentParcelInstanceID());
            boolean sameEpisodeID = existingStatusElement.getActivityID().getPresentWUAEpisodeID().equals(activityID.getPresentWUAEpisodeID());
            boolean sameWUPInstanceID = existingStatusElement.getActivityID().getPresentWUPInstanceID().equals(activityID.getPresentWUPInstanceID());
            boolean sameWUPTypeID = existingStatusElement.getActivityID().getPresentWUPFunctionToken().equals(activityID.getPresentWUPFunctionToken());
            boolean sameUpstreamEpisodeID = existingStatusElement.getActivityID().getPreviousWUAEpisodeID().equals(activityID.getPresentWUAEpisodeID());
            if( sameInstanceID && sameEpisodeID && sameWUPInstanceID && sameWUPTypeID & sameUpstreamEpisodeID ){
                LOG.trace(".addWUA(): New ActivityID and existing (registered) ID the same, so update the status (maybe) and then exit");
                existingStatusElement.setParcelStatus(initialProcessingStatus);
                LOG.debug(".addWUA(): Exit, returning existingStatusElement --> {}", existingStatusElement);
                return(existingStatusElement);
            } else {
                LOG.trace(".addWUA(): Exit, new ActivityID and existing (registered) ID are different, so delete the existing one!");
                parcelElementStatusSet.remove(activityID.getPresentParcelInstanceID());
                FDNTokenSet wuaEpisodeParcelSet = wuaEpisode2ParcelInstanceMap.get(activityID.getPresentWUAEpisodeID());
                wuaEpisodeParcelSet.removeElement(activityID.getPresentParcelInstanceID());
            }
        }
        LOG.trace(".addWUA(): Create a new ParcelStatusElement, set its initial status and add it to the two caches!");
        ParcelStatusElement newStatusElement = new ParcelStatusElement(activityID);
        newStatusElement.setParcelStatus(initialProcessingStatus);
        FDNTokenSet wuaEpisodeParcelSet = wuaEpisode2ParcelInstanceMap.get(activityID.getPresentWUAEpisodeID());
        parcelElementStatusSet.put(activityID.getPresentParcelInstanceID(), newStatusElement);
        wuaEpisodeParcelSet.addElement(activityID.getPresentParcelInstanceID());
        LOG.debug(".addWUA(): Exit, newStatusElement --> {}", newStatusElement);
        return(newStatusElement);
    }

    /**
     * Update the Status of the WUA Element to reflect the requested change. The system is operating in
     * Standalone/Standalone Resilience/Concurrency mode - so there is no need to check for SystemWide/Cluster
     * Focus or support any form of concurrent processing.
     *
     * @param activityID The unique Identifier that distinctly represents this work/resilience activity function
     * @param status     The new status to be applied to the WUA Element
     */
    public void updateWUA(ContinuityID activityID, ResilienceParcelProcessingStatusEnum status) {
        LOG.debug(".updateWUA(): Entry, activityID --> {}, status --> {}", activityID, status);
        if (activityID == null) {
            throw (new IllegalArgumentException(".updateParcelActivity(): ContinuityID (activityID) Processing Status (status) is null"));
        }
        LOG.trace(".updateWUA(): Get the current ParcelStatusElement");
        FDNToken parcelInstanceID = activityID.getPresentParcelInstanceID();
        ParcelStatusElement currentStatusElement;
        if(parcelElementStatusSet.containsKey(parcelInstanceID)) {
            LOG.trace(".updateWUA(): ParcelStatusElement exists -> get it!");
            currentStatusElement = parcelElementStatusSet.get(parcelInstanceID);
            LOG.trace(".updateWUA(): Updating status of the ParcelStatusElement!");
            currentStatusElement.setParcelStatus(status);
        } else {
            LOG.trace(".updateWUA(): ParcelStatusElement does not exist -> create it!");
            currentStatusElement = this.addWUA(activityID, status);
        }
        LOG.debug(".updateWUA(): Exit, updated currentStatusElement --> {}", currentStatusElement);
    }

    public ParcelStatusElement getParcelStatusElement(FDNToken parcelInstanceID) {
        LOG.debug(".getCurrentParcelStatusElement(): Entry, parcelInstanceID --> {}", parcelInstanceID);
        ParcelStatusElement requestedElement = parcelElementStatusSet.get(parcelInstanceID);
        LOG.debug(".getCurrentParcelStatusElement(): Exit, returning requestedElement --> {}", requestedElement);
        return (requestedElement);
    }

    private FDNToken findWUAEpisodeForParcelInstance(FDNToken parcelInstanceID) {
        Enumeration<FDNToken> wuaEpisodeEnumeration = wuaEpisode2ParcelInstanceMap.keys();
        while (wuaEpisodeEnumeration.hasMoreElements()) {
            FDNToken presentWUAEpisode = wuaEpisodeEnumeration.nextElement();
            FDNTokenSet tokenSet = wuaEpisode2ParcelInstanceMap.get(presentWUAEpisode);
            if (tokenSet.getElements().contains(parcelInstanceID)) {
                return (presentWUAEpisode);
            }
        }
        return (null);
    }

    /**
     * This method extracts the list of Parcel ID's associated with a WUA
     * Episode and iterates through each - using them to get their associated
     * ParcelStatusElement - which is then checked to see if it has the System
     * Wide Focus for the WUA Episode.
     *
     * @param wuaEpisodeID The Work Unit Activity Episode ID we are attempting
     *                     to determine which Parcel has System Wide Focus for.
     * @return Returns the Parcel Instance Identifier for the Parcel that has
     * System Wide Focus, or null.
     */
    public FDNToken getSiteWideFocusElement(FDNToken wuaEpisodeID) {
        LOG.debug(".getSiteWideFocusElement(): Entry, wuaEpisodeID --> {}", wuaEpisodeID);
        FDNTokenSet wuaEpisodeParcelIDSet = wuaEpisode2ParcelInstanceMap.get(wuaEpisodeID);
        Iterator<FDNToken> wuaEpisodeParcelIDIterator = wuaEpisodeParcelIDSet.getElements().iterator();
        while (wuaEpisodeParcelIDIterator.hasNext()) {
            FDNToken currentParcelID = wuaEpisodeParcelIDIterator.next();
            ParcelStatusElement currentStatusElement = parcelElementStatusSet.get(currentParcelID);
            if (currentStatusElement.getHasSystemWideFocus()) {
                LOG.debug(".getSiteWideFocusElement(): Exit, Parcel has been found that has System Wide Focus, returning parcelInstanceID --> {}", currentParcelID);
                return (currentParcelID);
            }
        }
        LOG.debug(".getSiteWideFocusElement(): Exit, No parcel was found with System Wide Focus, returning -null-");
        return (null);
    }

    /**
     * This method extracts the list of Parcel ID's associated with a WUA
     * Episode and iterates through each - using them to get their associated
     * ParcelStatusElement - which is then checked to see if it has the Cluster
     * Focus for the WUA Episode.
     *
     * @param wuaEpisodeID The Work Unit Activity Episode ID we are attempting
     *                     to determine which Parcel has Cluster Focus for.
     * @return Returns the Parcel Instance Identifier for the Parcel that has
     * Cluster Focus, or null.
     */
    public FDNToken getClusterFocusElement(FDNToken wuaEpisodeID) {
        LOG.debug(".getClusterFocusElement(): Entry, wuaEpisodeID --> {}", wuaEpisodeID);
        FDNTokenSet wuaEpisodeParcelIDSet = wuaEpisode2ParcelInstanceMap.get(wuaEpisodeID);
        Iterator<FDNToken> wuaEpisodeParcelIDIterator = wuaEpisodeParcelIDSet.getElements().iterator();
        while (wuaEpisodeParcelIDIterator.hasNext()) {
            FDNToken currentParcelID = wuaEpisodeParcelIDIterator.next();
            ParcelStatusElement currentStatusElement = parcelElementStatusSet.get(currentParcelID);
            if (currentStatusElement.getHasClusterFocus()) {
                LOG.debug(".getClusterFocusElement(): Exit, Parcel has been found that has Cluster Focus, returning parcelInstanceID --> {}", currentParcelID);
                return (currentParcelID);
            }
        }
        LOG.debug(".getClusterFocusElement(): Exit, No parcel was found with Cluster Focus, returning -null-");
        return (null);
    }

    /**
     * Clear any aged content from the Cache systems
     */
    public List<FDNToken> getAgedContentFromUpActivityMatrix() {
        LOG.debug(".getAgedContentFromUpActivityMatrix(): Entry");
        ArrayList<FDNToken> agedContent = new ArrayList<FDNToken>();
        Enumeration<FDNToken> parcelEpisodeIDIterator = wuaEpisode2ParcelInstanceMap.keys();
        LOG.trace(".getAgedContentFromUpActivityMatrix(): Iterating through each EpisodeID");
        Date currentDate = Date.from(Instant.now());
        Long cutOffAge = currentDate.getTime() - (PetasosPropertyConstants.CACHE_ENTRY_RETENTION_PERIOD_SECONDS);
        Long timeOutAge = currentDate.getTime() - (PetasosPropertyConstants.WUP_ACTIVITY_DURATION_SECONDS);
        while (parcelEpisodeIDIterator.hasMoreElements()) {
            FDNToken parcelEpisodeID = parcelEpisodeIDIterator.nextElement();
            FDNTokenSet statusSet = wuaEpisode2ParcelInstanceMap.get(parcelEpisodeID);
            LOG.trace(".clearAgedContentFromUpActivityMatrix(): Iterating through ALL ParcelStatusElements to see if one is FINISHED, ParcelEpisodeID --> {}, ", parcelEpisodeID);
            Iterator<FDNToken> initialSearchStatusIterator = statusSet.getElements().iterator();
            boolean foundFinished = false;
            FDNToken finishedParcelID = null;
            while (initialSearchStatusIterator.hasNext()) {
                FDNToken currentParcelInstanceID = initialSearchStatusIterator.next();
                ParcelStatusElement currentElement = parcelElementStatusSet.get(currentParcelInstanceID);
                switch (currentElement.getParcelStatus()) {
                    case PARCEL_STATUS_FINALISED:
                    case PARCEL_STATUS_FINALISED_ELSEWHERE:
                        if (currentElement.getEntryDate().getTime() > cutOffAge) {
                            agedContent.add(currentParcelInstanceID);
                        }
                        break;
                    case PARCEL_STATUS_REGISTERED:
                    case PARCEL_STATUS_INITIATED:
                    case PARCEL_STATUS_ACTIVE:
                    case PARCEL_STATUS_FINISHED:
                    case PARCEL_STATUS_FINISHED_ELSEWHERE:
                        if (currentElement.getEntryDate().getTime() < timeOutAge) {
                            break;
                        }
                    case PARCEL_STATUS_FAILED:
                    default:
                        agedContent.add(currentParcelInstanceID);
                        break;
                }
            }
        }
        return(agedContent);
    }

    public List<ParcelStatusElement> getEpisodeElementSet(FDNToken episodeID){
        LOG.debug(".getEpisodeElementSet(): Entry, episodeID --> {}", episodeID);
        ArrayList<ParcelStatusElement> episodeSet = new ArrayList<ParcelStatusElement>();
        FDNTokenSet episodeParcelIDs = wuaEpisode2ParcelInstanceMap.get(episodeID);
        if(episodeParcelIDs == null){
            return(episodeSet);
        }
        if(episodeParcelIDs.isEmpty()){
            return(episodeSet);
        }
        Iterator<FDNToken> parcelStatusIDIterator = episodeParcelIDs.getElements().iterator();
        while(parcelStatusIDIterator.hasNext()){
            ParcelStatusElement currentElement = parcelElementStatusSet.get(parcelStatusIDIterator.next());
            if(currentElement != null){
                episodeSet.add(currentElement);
            }
        }
        return(episodeSet);
    }

}
