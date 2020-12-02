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

package net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.archetypes;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.camel.BaseRouteBuilder;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPContainerIngresGatekeeper;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPContainerIngresProcessor;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPIngresConduit;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;

/**
 * @author Mark A. Hunter
 * @since 2020-07-1
 */

public class ExternalEgressWUPContainerRoute extends BaseRouteBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(ExternalEgressWUPContainerRoute.class);

	private NodeElement wupNode;
	private RouteElementNames nameSet;

	public ExternalEgressWUPContainerRoute(CamelContext camelCTX, NodeElement wupNode) {
		super(camelCTX);
		LOG.debug(".StandardWUPContainerRoute(): Entry, context --> ###, wupNode --> {}", wupNode);
		this.wupNode = wupNode;
		nameSet = new RouteElementNames(wupNode.getNodeFunctionToken());
	}

	@Override
	public void configure() {
		LOG.debug(".configure(): Entry!, for wupNode --> {}", this.wupNode);
		LOG.info("StandardWUPContainerRoute :: EndPointWUPContainerIngresProcessorIngres --> {}", nameSet.getEndPointWUPContainerIngresProcessorIngres());
		LOG.info("StandardWUPContainerRoute :: EndPointWUPContainerIngresProcessorEgress --> {}", nameSet.getEndPointWUPContainerIngresProcessorEgress());
		LOG.info("StandardWUPContainerRoute :: EndPointWUPContainerIngresGatekeeperIngres --> {}", nameSet.getEndPointWUPContainerIngresGatekeeperIngres());
		LOG.info("StandardWUPContainerRoute :: EndPointWUPIngresConduitIngres --> {}", nameSet.getEndPointWUPIngresConduitIngres());
		LOG.info("StandardWUPContainerRoute :: EndPointWUPIngres --> {}", nameSet.getEndPointWUPIngres());
		LOG.info("StandardWUPContainerRoute :: EndPointWUPEgress --> {}", nameSet.getEndPointWUPEgress());

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPContainerIngresProcessorIngres())
				.routeId(nameSet.getRouteWUPContainerIngressProcessor())
				.bean(WUPContainerIngresProcessor.class, "ingresContentProcessor(*, Exchange," + this.wupNode.extractNodeKey() + ")")
				.to(nameSet.getEndPointWUPContainerIngresProcessorEgress());

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPContainerIngresProcessorEgress())
				.routeId(nameSet.getRouteIngresProcessorEgress2IngresGatekeeperIngres())
				.to(nameSet.getEndPointWUPContainerIngresGatekeeperIngres());

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPContainerIngresGatekeeperIngres())
				.routeId(nameSet.getRouteWUPContainerIngresGateway())
				.bean(WUPContainerIngresGatekeeper.class, "ingresGatekeeper(*, Exchange," + this.wupNode.extractNodeKey() + ")");

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPIngresConduitIngres())
				.routeId(nameSet.getRouteIngresConduitIngres2WUPIngres())
				.bean(WUPIngresConduit.class, "forwardIntoWUP(*, Exchange," + this.wupNode.extractNodeKey() + ")")
				.to(nameSet.getEndPointWUPIngres());
	}
}
