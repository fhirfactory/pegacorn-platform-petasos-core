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
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPContainerEgressGatekeeper;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPContainerEgressProcessor;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPContainerIngresGatekeeper;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPContainerIngresProcessor;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPEgressConduit;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.wupcontainer.worker.buildingblocks.WUPIngresConduit;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;

/**
 * @author Mark A. Hunter
 * @since 2020-07-1
 */

public class StandardWUPContainerRoute extends BaseRouteBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(StandardWUPContainerRoute.class);

	private NodeElement wupNode;
	private RouteElementNames nameSet;

	public StandardWUPContainerRoute( CamelContext camelCTX, NodeElement wupNode) {
		super(camelCTX);
		LOG.debug(".StandardWUPContainerRoute(): Entry, context --> ###, wupNode --> {}", wupNode);
		this.wupNode = wupNode;
		nameSet = new RouteElementNames(wupNode.getNodeFunctionToken());
	}

	public StandardWUPContainerRoute( CamelContext camelCTX, NodeElement wupNode, boolean requiresDirect) {
		super(camelCTX);
		LOG.debug(".StandardWUPContainerRoute(): Entry, context --> ###, wupNode --> {}", wupNode);
		this.wupNode = wupNode;
		nameSet = new RouteElementNames(wupNode.getNodeFunctionToken(), requiresDirect);
	}

	@Override
	public void configure() {
		LOG.debug(".configure(): Entry!, for wupNode --> {}", this.wupNode);
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPContainerIngresProcessorIngres --> {}", nameSet.getEndPointWUPContainerIngresProcessorIngres());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPContainerIngresProcessorEgress --> {}", nameSet.getEndPointWUPContainerIngresProcessorEgress());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPContainerIngresGatekeeperIngres --> {}", nameSet.getEndPointWUPContainerIngresGatekeeperIngres());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPIngresConduitIngres --> {}", nameSet.getEndPointWUPIngresConduitIngres());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPIngres --> {}", nameSet.getEndPointWUPIngres());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPEgress --> {}", nameSet.getEndPointWUPEgress());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPEgressConduitEgress --> {}", nameSet.getEndPointWUPEgressConduitEgress());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPContainerEgressProcessorIngres --> {}", nameSet.getEndPointWUPContainerEgressProcessorIngres());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPContainerEgressProcessorEgress --> {}", nameSet.getEndPointWUPContainerEgressProcessorEgress());
		LOG.debug("StandardWUPContainerRoute :: EndPointWUPContainerEgressGatekeeperIngres --> {}", nameSet.getEndPointWUPContainerEgressGatekeeperIngres());

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

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPEgress()).routeId(nameSet.getRouteWUPEgress2WUPEgressConduitEgress())
				.bean(WUPEgressConduit.class, "receiveFromWUP(*, Exchange," + this.wupNode.extractNodeKey() + ")")
				.to( nameSet.getEndPointWUPEgressConduitEgress());

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPEgressConduitEgress())
				.routeId(nameSet.getRouteWUPEgressConduitEgress2WUPEgressProcessorIngres())
				.to(nameSet.getEndPointWUPContainerEgressProcessorIngres());

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPContainerEgressProcessorIngres())
				.routeId(nameSet.getRouteWUPContainerEgressProcessor())
				.bean(WUPContainerEgressProcessor.class, "egressContentProcessor(*, Exchange," + this.wupNode.extractNodeKey() + ")")
				.to(nameSet.getEndPointWUPContainerEgressProcessorEgress());

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPContainerEgressProcessorEgress())
				.routeId(nameSet.getRouteWUPEgressProcessorEgress2WUPEgressGatekeeperIngres())
				.to(nameSet.getEndPointWUPContainerEgressGatekeeperIngres());

		fromWithStandardExceptionHandling(nameSet.getEndPointWUPContainerEgressGatekeeperIngres())
				.routeId(nameSet.getRouteWUPContainerEgressGateway())
				.bean(WUPContainerEgressGatekeeper.class, "egressGatekeeper(*, Exchange," + this.wupNode.extractNodeKey() + ")");

	}
}
