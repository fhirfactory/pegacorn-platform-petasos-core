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

package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.archetypes;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.buildingblocks.WUPContainerEgressGatekeeper;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.buildingblocks.WUPContainerEgressProcessor;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.buildingblocks.WUPEgressConduit;

public class ExternalIngresWUPContainerRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalIngresWUPContainerRoute.class);

    private NodeElement wupNodeElement;
    private RouteElementNames nameSet;

    public ExternalIngresWUPContainerRoute( CamelContext camelCTX, NodeElement wupNode) {
        super(camelCTX);
        LOG.debug(".ExternalIngresWUPContainerRoute(): Entry, context --> ###, wupNode --> {}", wupNode );
        this.wupNodeElement = wupNode;
        nameSet = new RouteElementNames(wupNodeElement.getNodeFunctionToken());
    }

    @Override
    public void configure() {
        LOG.debug(".configure(): Entry!, for wupNodeElement --> {}", this.wupNodeElement);
        LOG.info("ExternalIngresWUPContainerRoute :: EndPointWUPIngres --> Per Implementation Specified");
        LOG.info("ExternalIngresWUPContainerRoute :: EndPointWUPEgress --> {}", nameSet.getEndPointWUPEgress() );
        LOG.info("ExternalIngresWUPContainerRoute :: EndPointWUPEgressConduitEgress --> {}", nameSet.getEndPointWUPEgressConduitEgress());
        LOG.info("ExternalIngresWUPContainerRoute :: EndPointWUPContainerEgressProcessorEgress --> {}", nameSet.getEndPointWUPContainerEgressProcessorEgress());
        
      
        from(nameSet.getEndPointWUPEgress())
        		.log(LoggingLevel.INFO, "content --> ${body}")
                .routeId(nameSet.getRouteWUPEgress2WUPEgressConduitEgress())
                .bean(WUPEgressConduit.class, "receiveFromWUP(*, Exchange," + this.wupNodeElement.extractNodeKey() + ")")
                .to(nameSet.getEndPointWUPEgressConduitEgress());

        from(nameSet.getEndPointWUPEgressConduitEgress())
                .routeId(nameSet.getRouteWUPContainerEgressProcessor())
                .bean(WUPContainerEgressProcessor.class, "egressContentProcessor(*, Exchange," + this.wupNodeElement.extractNodeKey() + ")")
                .to(nameSet.getEndPointWUPContainerEgressProcessorEgress());

        from(nameSet.getEndPointWUPContainerEgressProcessorEgress())
                .routeId(nameSet.getRouteWUPContainerEgressGateway())
                .bean(WUPContainerEgressGatekeeper.class, "egressGatekeeper(*, Exchange," + this.wupNodeElement.extractNodeKey() + ")");

    }
}
