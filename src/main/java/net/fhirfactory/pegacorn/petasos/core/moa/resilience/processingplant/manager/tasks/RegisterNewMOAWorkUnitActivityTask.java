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

package net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.manager.tasks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.petasos.model.pathway.ActivityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import net.fhirfactory.pegacorn.petasos.core.moa.resilience.processingplant.cache.ProcessingPlantWUAEpisodeActivityMatrixDM;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;

@ApplicationScoped
public class RegisterNewMOAWorkUnitActivityTask {
	private static final Logger LOG = LoggerFactory.getLogger(RegisterNewMOAWorkUnitActivityTask.class);

	@Inject
	ProcessingPlantWUAEpisodeActivityMatrixDM activityMatrixDM;

	@Inject
	TopologyIM topologerServer;

	public ParcelStatusElement registerNewWUA(WUPJobCard submittedJobCard) {
		LOG.debug(".registerNewWUA(): Now register the parcel with the ActivityMatrix, submittedJobCard -- {}",
				submittedJobCard);
		if (submittedJobCard == null) {
			throw (new IllegalArgumentException(".doTask(): submittedJobCard is null"));
		}
		ActivityID activityID = submittedJobCard.getActivityID();
		ParcelStatusElement newStatusElement;
		NodeElementIdentifier nodeID = new NodeElementIdentifier(activityID.getPresentWUPIdentifier());
		switch (topologerServer.getDeploymentResilienceMode(nodeID)) {
		case RESILIENCE_MODE_MULTISITE:
			LOG.trace(".registerNewWUA(): Asking for -Multisite- Reliability Mode for Work Unit Activity Registration");
			switch (topologerServer.getConcurrencyMode(nodeID)) {
			case CONCURRENCY_MODE_CONCURRENT: // Woo hoo - we are full-on highly available
				LOG.trace(".registerNewWUA(): Asking for -Concurrent- Concurrency Mode, in -Multisite- Reliability Mode - implementing Multisite/Concurrent mode");
				newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			case CONCURRENCY_MODE_STANDALONE: // WTF - why bother!
				LOG.trace(".registerNewWUA(): Asking for -Standalone- Concurrency Mode, in -Multisite- Reliability Mode - not possible, defaulting to Multisite/OnDemand mode");
				newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			case CONCURRENCY_MODE_ONDEMAND: // make it reliable, scalable
			default:
				LOG.trace( ".registerNewWUA(): Asking for -OnDemand- Concurrency Mode, in -Multisite- Reliability Mode - implementing Multisite/OnDemand mode");
				newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			}
		case RESILIENCE_MODE_CLUSTERED:
			LOG.trace(".registerNewWUA(): Asking for -Clustered- Reliability Mode for Work Unit Activity Registration");
			switch (topologerServer.getConcurrencyMode(nodeID)) {
			case CONCURRENCY_MODE_ONDEMAND: // OK, preferred & MVP
				LOG.trace(".registerNewWUA(): Asking for -On-Demand- Concurrency Mode, in -Clustered- Reliability Mode - implementing Clustered/OnDemand mode");
				newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			case CONCURRENCY_MODE_CONCURRENT: // Not possible
				LOG.trace(".registerNewWUA(): Asking for -Concurrent- Concurrency Mode, in -Clustered- Reliability Mode - not possible, defaulting to Clustered/OnDemand mode");
				newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			case CONCURRENCY_MODE_STANDALONE: // A waste, we can have multiple - but only want one!
			default:
				LOG.trace(".registerNewWUA(): Asking for -Standalone- Concurrency Mode, in -Clustered- Reliability Mode - not possible, defaulting to Clustered/OnDemand mode");
				newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			}
		case RESILIENCE_MODE_STANDALONE:
			LOG.trace(".registerNewWUA(): Asking for -Standalone- Reliability Mode for Work Unit Activity Registration");
		default:
			switch (topologerServer.getConcurrencyMode(nodeID)) {
			case CONCURRENCY_MODE_CONCURRENT: // Not possible!
				LOG.trace(".registerNewWUA(): Asking for -Concurrent- Concurrency Mode, in -Standalone- Reliability Mode - not possible, defaulting to Standalone/Standalone mode");
				newStatusElement = activityMatrixDM.addWUA(activityID,ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			case CONCURRENCY_MODE_ONDEMAND: // Not possible!
				LOG.trace(".registerNewWUA(): Asking for -On-Demand- Concurrency Mode, in -Standalone- Reliability Mode - not possible, defaulting to Standalone/Standalone mode");
				newStatusElement = activityMatrixDM.addWUA(activityID, ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			case CONCURRENCY_MODE_STANDALONE: // Really only good for PoCs and Integration Testing
			default:
				LOG.trace(".registerNewWUA(): Defaulting to -Standalone-/-Standalone- Reliability/Concurrency Mode");
				newStatusElement = activityMatrixDM.addWUA(activityID,ResilienceParcelProcessingStatusEnum.PARCEL_STATUS_REGISTERED);
				activityMatrixDM.setClusterWideFocusElement(activityID.getPresentEpisodeIdentifier(), activityID.getPresentParcelIdentifier());
				activityMatrixDM.setSystemWideFocusElement(activityID.getPresentEpisodeIdentifier(), activityID.getPresentParcelIdentifier());
				LOG.debug(".registerNewWUA(): Exit, newStatusElement --> {}", newStatusElement);
				return (newStatusElement);
			}
		}
	}
}
