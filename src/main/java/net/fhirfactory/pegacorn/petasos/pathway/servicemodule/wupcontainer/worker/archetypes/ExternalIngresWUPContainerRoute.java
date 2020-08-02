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

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.naming.RouteElementNames;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.wupcontainer.worker.buildingblocks.*;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalIngresWUPContainerRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalIngresWUPContainerRoute.class);

    private NodeElementFunctionToken wupFunctionToken;
    private FDNToken wupInstanceID;
    private RouteElementNames nameSet;

    public ExternalIngresWUPContainerRoute(CamelContext context, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID) {
        super(context);
        LOG.debug(".ExternalIngresWUPContainerRoute(): Entry, context --> ###, wupFunctionToken --> {}, wupInstanceID --> {}", wupFunctionToken, wupInstanceID );
        this.wupFunctionToken = wupFunctionToken;
        this.wupInstanceID = wupInstanceID;
        nameSet = new RouteElementNames(wupFunctionToken);
    }

    @Override
    public void configure() {
        LOG.debug(".configure(): Entry!, for wupFunctionToken --> {}, wupInstanceID --> {}", this.wupFunctionToken, this.wupInstanceID);

        from(nameSet.getEndPointWUPEgress())
                .routeId(nameSet.getRouteWUPEgress2WUPEgressConduitEgress())
                .bean(WUPEgressConduit.class, "receiveFromWUP(*, Exchange," + this.wupFunctionToken + "," + this.wupInstanceID + ")")
                .to(nameSet.getEndPointWUPContainerEgressProcessorIngres());

        from(nameSet.getEndPointWUPContainerEgressProcessorIngres())
                .routeId(nameSet.getRouteWUPContainerEgressProcessor())
                .bean(WUPContainerEgressProcessor.class, "egressContentProcessor(*, Exchange," + this.wupFunctionToken + "," + this.wupInstanceID + ")")
                .to(nameSet.getEndPointWUPContainerEgressProcessorIngres());

        from(nameSet.getEndPointWUPContainerEgressProcessorIngres())
                .routeId(nameSet.getRouteWUPContainerEgressGateway())
                .dynamicRouter(method(WUPContainerEgressGatekeeper.class, "egressGatekeeper(*, Exchange," + this.wupFunctionToken + "," + this.wupInstanceID + ")"));

    }
}
