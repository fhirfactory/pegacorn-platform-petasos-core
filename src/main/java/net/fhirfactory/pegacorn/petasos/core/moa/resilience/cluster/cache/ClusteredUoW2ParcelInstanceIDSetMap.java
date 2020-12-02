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
package net.fhirfactory.pegacorn.petasos.core.moa.resilience.cluster.cache;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNTokenSet;

/**
 * @author Mark A. Hunter
 *
 */
public class ClusteredUoW2ParcelInstanceIDSetMap {

    private static final Logger LOG = LoggerFactory.getLogger(ClusteredUoW2ParcelInstanceIDSetMap.class);
    @Inject
    DefaultCacheManager petasosCacheManager;

    // Camel Context is required for creating a ProducerTemplate so we can send our
    // parcel and WUP state JSON to a camel consumer endpoint which will then forward
    // on to other sites.
    @Inject
    CamelContext camelContext;


    private Cache<FDN, FDNTokenSet> uow2ParcelSetMap;

    @PostConstruct
    public void start() {
        uow2ParcelSetMap = petasosCacheManager.getCache("petasos-uow-map", true);
    }

    public void linkUoW2Parcel(FDN uowInstanceID, FDN parcelInstanceID) {
        if (parcelInstanceID == null) {
            return;
        }
        if (uowInstanceID == null) {
            return;
        }
        FDNTokenSet associatedParcelSet;
        if (uow2ParcelSetMap.containsKey(uowInstanceID)) {
            associatedParcelSet = uow2ParcelSetMap.get(uowInstanceID);
            associatedParcelSet.addElement(parcelInstanceID.getToken());
        } else {
            associatedParcelSet = new FDNTokenSet();
            associatedParcelSet.addElement(parcelInstanceID.getToken());
            uow2ParcelSetMap.put(uowInstanceID, associatedParcelSet);
        }
    }
}
