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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import org.apache.camel.CamelContext;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;


/**
 * 
 * @author Mark A. Hunter
 * @author Scott Yeadon
 *
 */
@ApplicationScoped
public class ClusteredGeneralParcelMap {
	private static final Logger LOG = LoggerFactory.getLogger(ClusteredGeneralParcelMap.class);
	
	@Inject
	DefaultCacheManager petasosCacheManager;
	
    // Camel Context is required for creating a ProducerTemplate so we can send our
    // parcel and WUP state JSON to a camel consumer endpoint which will then forward
    // on to other sites.
    @Inject
    CamelContext camelContext;
    
   
    // The clustered cache for the PetasosParcels coordinated by this Petasos::Node (cluster) instance
    private Cache<FDNToken, ResilienceParcel> petasosParcelCache;
    
    @PostConstruct
    public void start() {
        // get or create the clustered cache which will hold the transactions (aka Units of Work)
        petasosParcelCache = petasosCacheManager.getCache("petasos-parcel-cache", true);    	
    }
    
    public void addParcel(ResilienceParcel parcel) {
    	LOG.debug(".addParcel(): Entry, parcel --> {}", parcel);
    	if(parcel == null) {
    		return; 
    	}
    	if(!parcel.hasInstanceIdentifier()) {
    		return;
    	}
    	FDNToken parcelID = parcel.getIdentifier();
    	petasosParcelCache.put(parcelID, parcel);
    }
    
    public void removeParcel(ResilienceParcel parcel) {
    	LOG.debug(".removeParcel(): Entry, parcel --> {}", parcel);
    	if(parcel==null) {
    		return;
    	}
    	if(!parcel.hasInstanceIdentifier()) {
    		return;
    	}
    	petasosParcelCache.remove(parcel.getIdentifier());
    }
    
    public void removeParcel(FDN parcelInstanceID) {
    	LOG.debug(".removeParcel(): Entry, parcelInstanceID --> {}", parcelInstanceID);
    	if(parcelInstanceID==null) {
    		return;
    	}
    	petasosParcelCache.remove(parcelInstanceID);
    }
    
    public List<ResilienceParcel> getParcelSet(){
    	LOG.debug(".getParcelSet(): Entry");
    	List<ResilienceParcel> parcelList = new LinkedList<ResilienceParcel>();
    	petasosParcelCache.entrySet().forEach(entry -> parcelList.add(entry.getValue()));
    	return(parcelList);
    }
    
    public List<ResilienceParcel> getParcelSetByState(ResilienceParcelProcessingStatusEnum status){
    	LOG.debug(".getParcelSet(): Entry, status --> {}", status);
    	List<ResilienceParcel> parcelList = new LinkedList<ResilienceParcel>();
    	Iterator<ResilienceParcel> parcelListIterator = parcelList.iterator();
    	while(parcelListIterator.hasNext()) {
    		ResilienceParcel currentParcel = parcelListIterator.next();
    		if(currentParcel.hasProcessingStatus()) {
    			if(currentParcel.getProcessingStatus()==status) {
    				parcelList.add(currentParcel);
    			}
    		}
    	}
    	return(parcelList);
    }    
    
    public List<ResilienceParcel> getActiveParcelSet(){
    	List<ResilienceParcel> parcelList = getParcelSetByState(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE);
    	return(parcelList);
    }
    
    public List<ResilienceParcel> getFinishedParcelSet(){
    	List<ResilienceParcel> parcelList = getParcelSetByState(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINISHED);
    	return(parcelList);
    }    
    
    public List<ResilienceParcel> getFinalisedParcelSet(){
    	List<ResilienceParcel> parcelList = getParcelSetByState(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED);
    	return(parcelList);
    }    
    
    public List<ResilienceParcel> getInProgressParcelSet(){
    	LOG.debug(".getInProgressParcelSet(): Entry");
    	List<ResilienceParcel> parcelList = new LinkedList<ResilienceParcel>();
    	parcelList.addAll(getParcelSetByState(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_ACTIVE));
    	parcelList.addAll(getParcelSetByState(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_INITIATED));
    	parcelList.addAll(getParcelSetByState(ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED));
    	return(parcelList);
    }
}
