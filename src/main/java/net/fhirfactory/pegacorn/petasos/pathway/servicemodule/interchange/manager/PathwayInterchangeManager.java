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

package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.interchange.manager;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.pathway.servicemodule.interchange.worker.InterchangeExtractAndRouteTemplate;
import net.fhirfactory.pegacorn.petasos.model.topology.ElementNameExtensions;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;

public class PathwayInterchangeManager {
    private static final Logger LOG = LoggerFactory.getLogger(PathwayInterchangeManager.class);

    /**
     * We have to establish a set of Routes for handling the egress traffic from a
     * WUP (and WUPContainer) which, essentially, pulls each UoWPayload element
     * from the UoW::EgressPayloadSet and creates a new UoW for it.
     * Then, we link this transformation function to a DynamicRouter (see Camel
     * doco) that then forwards the UoW to any Registered (Subscribed) downstream
     * WUP instance.
     *
     * @param camel
     * @param wupFunctionToken the WUP we are building the Interchange routes for
     * @param wupInstanceID
     */

    public void buildWUPInterchangeRoutes(CamelContext camel, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID){
        LOG.debug(".buildWUPInterchangeRoutes(): Entry, wupFunctionToken --> {}, wupInstanceID --> {}", wupFunctionToken, wupInstanceID);
        InterchangeExtractAndRouteTemplate newRoute = new InterchangeExtractAndRouteTemplate(camel, wupFunctionToken, wupInstanceID);
        LOG.trace(".buildWUPInterchangeRoutes(): Attempting to install new Route");
        try{
            camel.addRoutes(newRoute);
            LOG.trace(".buildWUPInterchangeRoutes(): Route installation successful");
        }
        catch(Exception Ex){
            LOG.debug(".buildWUPInterchangeRoutes(): Route install failed! Exception --> {}", Ex);
        }
        LOG.debug(".buildWUPInterchangeRoutes(): Exit - All good it seems!");
    }

    /**
     * This function parses the ServiceModuleMap and ensures that all the associated
     * Intersection elements are instantiated (and, in a very simplistic way, operational).
     */
    public void buildAllInterchangeRoutes(){
        LOG.debug(".buildAllInterchangeRoutes(): Entry");
        // need to create Camel
        CamelContext camel = new DefaultCamelContext();
        LOG.debug(".buildAllInterchangeRoutes(): Exit - All good it seems!");
    }
}
