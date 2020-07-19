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
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.WUAEpisodeFinalisationRegistrationStatus;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.WUAEpisodeFinalisationRegistrationStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is the Cache Data Manager (CacheDM) for the ServiceModule WorkUnitActivity Episode ID
 * finalisation map. This map essentially allows for registration of WUPs that have registered interest
 * in the output UoW from a particular Episode. It then tracks when those "downstream" WUPs register a
 * new Episode ID for the processing out the output UoW from this "upstream" WorkUnitAcitivity Episode.
 * <p>
 * It uses a ConcurrentHasMap to store a full list of all downstream WUP Registered instances:
 * ConcurrentHashMap<FDNToken, WUAEpisodeFinalisationRegistrationStatus> downstreamRegistrationStatusSet
 * where the FDNToken is the WUPInstanceID and the WUAEpisodeFinalisationRegistrationStatus is their
 * registration status.
 * <p>
 * It also uses a ConcurrentHashMap to store a list of WUPs that have registered to consume the specific
 * UoW of the current WUAEpisodeID
 * ConcurrentHashMap<FDNToken, FDNTokenSet> downstreamWUPRegistrationMap
 * where the FDNToken is the EpisodeID and the FDNTokenSet is the list of WUPInstanceIDs for the downstream WUPS.
 *
 * @author Mark A. Hunter
 * @since 2020.07.01
 */

public class ServiceModuleWUAEpisodeFinalisationCacheDM {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceModuleWUAEpisodeFinalisationCacheDM.class);

    private ConcurrentHashMap<FDNToken, WUAEpisodeFinalisationRegistrationStatus> downstreamRegistrationStatusSet;
    private ConcurrentHashMap<FDNToken, FDNTokenSet> downstreamWUPRegistrationMap;
    private Object wupRegistrationSetLock;

    /**
     * The default constructor. This function merely initialises all the various to non-null states,
     * including instantiation of the ConcurrentHashMaps used for caching the data.
     */
    public ServiceModuleWUAEpisodeFinalisationCacheDM() {
        downstreamRegistrationStatusSet = new ConcurrentHashMap<FDNToken, WUAEpisodeFinalisationRegistrationStatus>();
        downstreamWUPRegistrationMap = new ConcurrentHashMap<FDNToken, FDNTokenSet>();
        wupRegistrationSetLock = new Object();
    }

    /**
     * This function allows for the Registration of a WUPInstance as a consumer of the UoW from the given EpisodeID.
     * <p>
     * It first adds the WUP Instance to the complete registration set by first creating a WUAEpisodeFinalisationRegistrationStatus
     * for it and then adding it to the downstreamRegistrationStatusSet ConcurrentHashMap using the WUPInstanceID as the key.
     * <p>
     * It then adds the WUPInstanceID to the FDNTokenSet associated with the WUA EpisodeID for tracking - as stored in the
     * downstreamWUPRegistrationMap cache - which uses the WUA EpisodeID as the key.
     * <p>
     * It should be noted that this registration is performed by the PetasosIntersection elements - as it is only at the point
     * of Egress from the WUP processing is it known if a particular UoW is (successfully produced) by a WUP Instance.
     *
     * @param wuaEpisodeID            The WUA Episode ID (that generates the output UoW which we are tracking the finalisation of the associated parcel of)
     * @param downstreamWUPInstanceID The WUP Instance that will consuming the UoW and, therefore, is a downstream consumer of the output of this WUA Episode.
     */
    public void registerDownstreamWUPInterest(FDNToken wuaEpisodeID, FDNToken downstreamWUPInstanceID) {
        LOG.debug(".registerDownstreamWUPInterest(): Entry, wuaEpisodeID --> {}, downstreamWUPInstanceID --> {}", wuaEpisodeID, downstreamWUPInstanceID);
        if ((wuaEpisodeID == null) || (downstreamWUPInstanceID == null)) {
            throw (new IllegalArgumentException(".registerDownstreamWUPInterest(): wuaEpisodeID or downstreamWUPInstanceID are null"));
        }
        synchronized (wupRegistrationSetLock) {
            if(!downstreamRegistrationStatusSet.containsKey(downstreamWUPInstanceID)) {
                WUAEpisodeFinalisationRegistrationStatus newRegistrationStatusElement = new WUAEpisodeFinalisationRegistrationStatus(downstreamWUPInstanceID);
                downstreamRegistrationStatusSet.put(downstreamWUPInstanceID,newRegistrationStatusElement );
            }
            if (downstreamWUPRegistrationMap.containsKey(wuaEpisodeID)) {
                FDNTokenSet downstreamEpisode2WUPSet = downstreamWUPRegistrationMap.get(wuaEpisodeID);
                downstreamEpisode2WUPSet.addElement(downstreamWUPInstanceID);
            } else {
                FDNTokenSet downstreamEpisode2WUPSet = new FDNTokenSet();
                downstreamEpisode2WUPSet.addElement(downstreamWUPInstanceID);
                downstreamWUPRegistrationMap.put(wuaEpisodeID, downstreamEpisode2WUPSet);
            }
        }
    }

    /**
     * This function allows for the Registration of a "downstream" EpisodeID as a consumer of the UoW from the given EpisodeID.
     * <p>
     * Once a "downstream" Episode ID has been registered, the "upstream" Episode can assume that the Petasos framework will ensure that
     * it will be processed. Therefore, once ALL the potential "downstream" Episodes are registered, this "upstream" can be considered as
     * "Finalised".
     *
     * @param originalEpisodeID       The WUA Episode ID (that generates the output UoW which we are tracking the finalisation of the associated parcel of)
     * @param downstreamWUPInstanceID The WUP Instance that will consuming the UoW and, therefore, is a downstream consumer of the output of this WUA Episode.
     * @param downstreamEpisodeID     The new WUA Episode ID creating by the WUP (and, therefore, synchronised across the WHOLE deployment).
     */
    public void registerDownstreamEpisodeID(FDNToken originalEpisodeID, FDNToken downstreamWUPInstanceID, FDNToken downstreamEpisodeID) {
        LOG.debug(".registerDownstreamEpisodeID(): Entry, originalEpisodeID --> {}, downstreamWUPInstanceID --> {}, downstreamEpisodeID --> {} ", originalEpisodeID, downstreamWUPInstanceID, downstreamEpisodeID);
        if ((originalEpisodeID == null) || (downstreamWUPInstanceID == null) || (downstreamEpisodeID == null)) {
            throw (new IllegalArgumentException(".registerDownstreamEpisodeID(): originalEpisodeID, downstreamWUPInstanceID, downstreamEpisodeID are null"));
        }
        WUAEpisodeFinalisationRegistrationStatus wupInstanceFinalisationStatus;
        if (!downstreamRegistrationStatusSet.containsKey(downstreamWUPInstanceID)) {
            registerDownstreamWUPInterest(originalEpisodeID, downstreamWUPInstanceID);
        }
        wupInstanceFinalisationStatus = downstreamRegistrationStatusSet.get(downstreamWUPInstanceID);
        wupInstanceFinalisationStatus.setActualDownstreamEpisodeID(downstreamEpisodeID);
    }

    /**
     * This function parses all the FinalisationStatus elements associated to the WUA Episode ID and, if all are Finalised - returns true,
     * otherwise, it returns false.
     *
     * @param wuaEpisodeID The Episode ID that we would like to know if all the downstream WUPs have registered a successor WUA Episode ID for.
     * @return True if all downstream WUPs have registered a new WUA Episode ID (for a successor task), false if one or more haven't.
     */
    public boolean checkForEpisodeFinalisation(FDNToken wuaEpisodeID){
        LOG.debug(".checkForEpisodeFinalisation(): Entry, wuaEpisodeID --> {} ", wuaEpisodeID);
        if(wuaEpisodeID==null){
            LOG.debug(".checkForEpisodeFinalisation(): wuaEpisodeID parameter is null, returning false");
            return(false);
        }
        FDNTokenSet downstreamEpisode2WUPSet = downstreamWUPRegistrationMap.get(wuaEpisodeID);
        if(downstreamEpisode2WUPSet.isEmpty()){
            LOG.debug(".checkForEpisodeFinalisation(): If there are no registered downstream WUPs, then - by default - it's finalised! Returning -true-");
            return(true);
        }
        Iterator<FDNToken> wupInstanceIDIterator = downstreamEpisode2WUPSet.getElements().iterator();
        while(wupInstanceIDIterator.hasNext()){
            FDNToken wupInstanceID = wupInstanceIDIterator.next();
            if(downstreamRegistrationStatusSet.containsKey(wupInstanceID)) {
                WUAEpisodeFinalisationRegistrationStatus finalisationRegistrationStatus = downstreamRegistrationStatusSet.get(wupInstanceID);
                if (finalisationRegistrationStatus.getRegistrationStatus() == WUAEpisodeFinalisationRegistrationStatusEnum.DOWNSTREAM_EPISODE_ID_NOT_REGISTERED) {
                    LOG.debug(".checkForEpisodeFinalisation(): If any single registered downstream WUP has registered a successor WUA Episode ID, then it is finalised! Returning -false-");
                    return (false);
                }
            }
        }
        LOG.debug(".checkForEpisodeFinalisation(): It seems that ALL dowstream WUPs have registered successor WUA Episode IDs - returning -true-");
        return(true);
    }
}
