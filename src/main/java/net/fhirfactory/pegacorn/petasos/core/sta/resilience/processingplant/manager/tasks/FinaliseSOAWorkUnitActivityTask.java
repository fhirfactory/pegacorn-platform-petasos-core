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

package net.fhirfactory.pegacorn.petasos.core.sta.resilience.processingplant.manager.tasks;

import net.fhirfactory.pegacorn.petasos.core.sta.resilience.processingplant.cache.STAServiceModuleActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.model.pathway.ActivityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.EpisodeIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class FinaliseSOAWorkUnitActivityTask {
	private static final Logger LOG = LoggerFactory.getLogger(FinaliseSOAWorkUnitActivityTask.class);

	@Inject
	STAServiceModuleActivityMatrixDM activityMatrixDM;

	@Inject
	TopologyIM topologyServer;

	public void finaliseWUA(WUPJobCard submittedJobCard) {
		LOG.debug(".synchroniseJobCard(): Entry"); 
		if (submittedJobCard == null) {
			throw (new IllegalArgumentException(".doTask(): submittedJobCard is null"));
		}
        if(LOG.isDebugEnabled()) {
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).previousParcelIdentifier -->{}", submittedJobCard.getActivityID().getPreviousParcelIdentifier());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).previousEpisodeIdentifier --> {}", submittedJobCard.getActivityID().getPreviousEpisodeIdentifier());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).previousWUPFunctionTokan --> {}", submittedJobCard.getActivityID().getPreviousWUPFunctionToken());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).perviousWUPIdentifier --> {}", submittedJobCard.getActivityID().getPreviousWUPIdentifier());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).presentParcelIdentifier -->{}", submittedJobCard.getActivityID().getPresentParcelIdentifier());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).presentEpisodeIdentifier --> {}", submittedJobCard.getActivityID().getPresentEpisodeIdentifier());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).presentWUPFunctionTokan --> {}", submittedJobCard.getActivityID().getPresentWUPFunctionToken());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ActivityID).presentWUPIdentifier --> {}", submittedJobCard.getActivityID().getPresentWUPIdentifier());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).cardID (ContunuityID).createDate --> {}", submittedJobCard.getActivityID().getCreationDate());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).clusterMode (ConcurrencyModeEnum) -->{}", submittedJobCard.getClusterMode());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).currentStatus (WUPActivityStatusEnum) --> {}", submittedJobCard.getCurrentStatus());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).grantedStatus (WUPActivityStatusEnum) --> {}", submittedJobCard.getGrantedStatus());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).toBeDiscarded (boolean) --> {}", submittedJobCard.getIsToBeDiscarded());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).requestedStatus (WUPActivityStatusEnum) --> {}", submittedJobCard.getRequestedStatus());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).systemMode (ResilienceModeEnum) --> {}", submittedJobCard.getSystemMode());
        	LOG.debug(".synchroniseJobCard(): submittedJobCard (WUPJobCard).updateDate (Date) --> {}", submittedJobCard.getUpdateDate());
        }
		ActivityID activityID = submittedJobCard.getActivityID();
        NodeElementIdentifier nodeID = new NodeElementIdentifier(activityID.getPresentWUPIdentifier());
		switch (topologyServer.getDeploymentResilienceMode(nodeID)) {
		case RESILIENCE_MODE_MULTISITE:
			LOG.trace(".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_MULTISITE");
			switch (topologyServer.getConcurrencyMode(nodeID)) {
			case CONCURRENCY_MODE_CONCURRENT: // Woo hoo - we are full-on highly available
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_MULTISITE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_CONCURRENT");
				break;
			case CONCURRENCY_MODE_STANDALONE: // WTF - why bother!
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_MULTISITE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_STANDALONE");
				break;
			case CONCURRENCY_MODE_ONDEMAND: // make it reliable, scalable
			default:
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_MULTISITE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_ONDEMAND (default concurrency mode)");

			}
			break;
		case RESILIENCE_MODE_CLUSTERED:
			LOG.trace(".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_CLUSTERED");
			switch (topologyServer.getConcurrencyMode(nodeID)) {
			case CONCURRENCY_MODE_CONCURRENT: // Not possible
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_CLUSTERED, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_CONCURRENT");
			case CONCURRENCY_MODE_STANDALONE: // A waste, we can have multiple - but only want one!
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_CLUSTERED, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_STANDALONE");
			case CONCURRENCY_MODE_ONDEMAND: // OK, preferred & MVP
			default:
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_CLUSTERED, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_ONDEMAND (default concurrency mode)");
			}
			break;
		case RESILIENCE_MODE_STANDALONE:
			LOG.trace(".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_STANDALONE");
		default:
			switch (topologyServer.getConcurrencyMode(nodeID)) {
			case CONCURRENCY_MODE_CONCURRENT: // Not possible!
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_STANDALONE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_CONCURRENT (not possible)");
			case CONCURRENCY_MODE_ONDEMAND: // Not possible!
				LOG.trace(
						".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_STANDALONE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_ONDEMAND (not possible)");
			case CONCURRENCY_MODE_STANDALONE: // Really only good for PoCs and Integration Testing
			default:
				LOG.trace( ".synchroniseJobCard(): Deployment Mode --> PETASOS_MODE_STANDALONE, Concurrency Mode --> PETASOS_WUA_CONCURRENCY_MODE_STANDALONE (default concurrent mode)");
				ResilienceParcelIdentifier parcelInstanceID = activityID.getPresentParcelIdentifier();
				EpisodeIdentifier wuaEpisodeID = activityID.getPresentEpisodeIdentifier();
				LOG.trace(".standaloneModeSynchroniseJobCard(): Retrieve the ParcelStatusElement from the Cache for ParcelInstanceID --> {}", parcelInstanceID);
				ParcelStatusElement statusElement = activityMatrixDM.getTransactionElement(parcelInstanceID);
				LOG.trace(".standaloneModeSynchroniseJobCard(): Retrieved ParcelStatusElement --> {}", statusElement);
				submittedJobCard.setGrantedStatus(submittedJobCard.getCurrentStatus());
				submittedJobCard.setUpdateDate(Date.from(Instant.now()));
				switch(submittedJobCard.getCurrentStatus()) {
					case WUP_ACTIVITY_STATUS_FINISHED:
						activityMatrixDM.finishTransaction(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FINALISED);
					case WUP_ACTIVITY_STATUS_FAILED:
					case WUP_ACTIVITY_STATUS_WAITING:
					case WUP_ACTIVITY_STATUS_CANCELED:
					case WUP_ACTIVITY_STATUS_EXECUTING:
					default:
						activityMatrixDM.finishTransaction(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_FAILED);
				}
			}
		}
	}
}
