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

package net.fhirfactory.pegacorn.petasos.pathway.contentrouting.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.FDNTokenSet;

import javax.inject.Singleton;

@Singleton
public class UoWPayloadTopicSubscriptionMapDM {
	private static final Logger LOG = LoggerFactory.getLogger(UoWPayloadTopicSubscriptionMapDM.class);
	
	ConcurrentHashMap<FDNToken, FDNTokenSet> distributionList;
	
    public UoWPayloadTopicSubscriptionMapDM(){
        distributionList = new ConcurrentHashMap<FDNToken, FDNTokenSet>();
    }

    /**
     * This function retrieves the list (FDNTokenSet) of WUPs that are interested in 
     * receiving the identified uowPayloadTopicID (FDNToken).
     * 
     * @param topicID The FDNToken representing the UoW (Ingres) Payload Topic that we want to know which WUPs are interested in
     * @return The set of WUPs wanting to receive this payload type.
     */

    public FDNTokenSet getSubscriptionSetForUOWContentTopic(FDNToken topicID){
    	LOG.debug(".getSubscriptionSetForUOWContentTopic(): Entry, topicID --> {}", topicID);
    	if(distributionList.isEmpty()) {
    		LOG.debug("getSubscriptionSetForUOWContentTopic(): Exit, empty list so can't match");
    		
    		return(null);
    	}
    	if(this.distributionList.containsKey(topicID)) {
    		FDNTokenSet interestedWUPSet = this.distributionList.get(topicID);
    		LOG.debug(".getSubscriptionSetForUOWContentTopic(): Exit, returning associated FDNSet of the WUPs interested --> {}", interestedWUPSet);
    		return(interestedWUPSet);
    	}
    	LOG.debug(".getSubscriptionSetForUOWContentTopic(): Couldn't find any associated FDNTokenSet elements (i.e. couldn't find any interested WUPs, returning null");
    	return(null);
    }
    
    /**
     * This function establishes a link between a Payload Type and a WUP that is interested in
     * processing/using it.
     * 
     * @param contentTopicID The contentTopicID (FDNToken) of the payload we have received from a WUP
     * @param interestedWUP The ID of the WUP that is interested in the payload type.
     */
    public void addSubscriberToUoWContentTopic(FDNToken contentTopicID, FDNToken interestedWUP) {
    	LOG.debug(".addInterestedWUPforPayload(): Entry, contentTopicID --> {}, interestedWUP --> {}", contentTopicID, interestedWUP);
    	if((contentTopicID==null) || (interestedWUP==null)) {
    		throw(new IllegalArgumentException(".setRouteForPayload(): contentTopicID or interestedWUP is null"));
    	}
    	if(this.distributionList.containsKey(contentTopicID)) {
    		LOG.trace(".addInterestedWUPforPayload(): Removing existing map for contentTopicID --> {}", contentTopicID);
    		FDNTokenSet payloadDistributionList = this.distributionList.get(contentTopicID);
    		payloadDistributionList.addElement(interestedWUP);
    	} else {
    		FDNTokenSet newPayloadDistributionList = new FDNTokenSet();
    		newPayloadDistributionList.addElement(interestedWUP);
    		this.distributionList.put(contentTopicID, newPayloadDistributionList);
    	}
    	LOG.debug(".addInterestedWUPforPayload(): Exit, assigned the interestedWUP to the contentTopicID");
    }

}
