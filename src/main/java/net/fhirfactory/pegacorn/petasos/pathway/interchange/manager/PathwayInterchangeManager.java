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

package net.fhirfactory.pegacorn.petasos.pathway.interchange.manager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.pathway.interchange.worker.InterchangeExtractAndRouteTemplate;

@ApplicationScoped
public class PathwayInterchangeManager {
    private static final Logger LOG = LoggerFactory.getLogger(PathwayInterchangeManager.class);

    @Inject
    CamelContext camelctx;


    /**
     * We have to establish a set of Routes for handling the egress traffic from a
     * WUP (and WUPContainer) which, essentially, pulls each UoWPayload element
     * from the UoW::EgressPayloadSet and creates a new UoW for it.
     * Then, we link this transformation function to a DynamicRouter (see Camel
     * doco) that then forwards the UoW to any Registered (Subscribed) downstream
     * WUP instance.
     *
     * @param nodeElement the WUP's NodeElement we are building the Interchange routes for
     */

    public void buildWUPInterchangeRoutes(NodeElement nodeElement){
        LOG.debug(".buildWUPInterchangeRoutes(): Entry, nodeElement --> {}", nodeElement);
        InterchangeExtractAndRouteTemplate newRoute = new InterchangeExtractAndRouteTemplate(camelctx,nodeElement);
        LOG.trace(".buildWUPInterchangeRoutes(): Attempting to install new Route");
        try{
            camelctx.addRoutes(newRoute);
            LOG.trace(".buildWUPInterchangeRoutes(): Route installation successful");
        }
        catch(Exception Ex){
            LOG.debug(".buildWUPInterchangeRoutes(): Route install failed! Exception --> {}", Ex);
        }
        LOG.debug(".buildWUPInterchangeRoutes(): Exit - All good it seems!");
    }
}
