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

package net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Mark A. Hunter
 * @since 2020-06-01
 */
public class RouteElementNames {
    private static final Logger LOG = LoggerFactory.getLogger(RouteElementNames.class);

    private NodeElementFunctionToken nodeFunctionToken;
    private boolean mustBeDirect;
    private String wupTypeName;
    private String wupVersion;
    private static final String INTRA_FUNCTION_DIRECT_TYPE = "direct:";
    private static final String DIRECT_INTER_FUNCTION_DIRECT_TYPE = "direct:";
    private static final String SEDA_INTER_FUNCTION_DIRECT_TYPE = "seda:";

    public RouteElementNames(NodeElementFunctionToken functionToken, boolean mustBeDirect){
        this.nodeFunctionToken = functionToken;
        this.wupTypeName = simplifyName();
        this.mustBeDirect = mustBeDirect;
    }

    public RouteElementNames(NodeElementFunctionToken functionToken){
        this.nodeFunctionToken = functionToken;
        this.wupTypeName = simplifyName();
        this.mustBeDirect = false;
    }

    public String simplifyName(){
        LOG.debug(".simplifyName(): this.nodeFunctionToken --> {}", this.nodeFunctionToken);
        FDN wupFunctionFDN = new FDN(this.nodeFunctionToken.getFunctionID());
        LOG.trace(".simplifyName(): wupFunctionFDN --> {}", wupFunctionFDN);
        RDN processingPlantRDN = wupFunctionFDN.extractRDNViaQualifier(NodeElementTypeEnum.PROCESSING_PLANT.getNodeElementType());
        LOG.trace(".simplifyName(): processingPlantRDN (RDN) --> {} ", processingPlantRDN);
        RDN wupFunctionRDN = wupFunctionFDN.getUnqualifiedRDN();
        LOG.trace(".simplifyName(): wupFunctionRDN (RDN) --> {}", wupFunctionRDN);
        String nodeVersion = nodeFunctionToken.getVersion();
        String nodeVersionSimplified = nodeVersion.replace(".","");
        String wupName = processingPlantRDN.getValue()+"."+wupFunctionRDN.getValue()+"."+nodeVersionSimplified;
        LOG.debug(".simplifyName(): wupName (String) --> {}", wupName);
        return(wupName);
    }

    public String getWupTypeName(){
        return(this.wupTypeName);
    }
    
    public String getRouteCoreWUP(){
        return(this.wupTypeName+".WUP.Core");
    }

    public String getEndPointWUPContainerIngresProcessorIngres() {
        String endpointName;
        if(this.mustBeDirect){
            endpointName = DIRECT_INTER_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUPContainer.IngresProcessor.Ingres";
        } else {
            endpointName = SEDA_INTER_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUPContainer.IngresProcessor.Ingres";
        }
        return(endpointName);
    }

    public String getEndPointWUPContainerIngresProcessorEgress() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUPContainer.IngresProcessor.Egress";
        return(endpointName);
    }

    public String getEndPointWUPContainerIngresGatekeeperIngres() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUPContainer.IngresGatekeeper.Ingres";
        return(endpointName);
    }

    public String getEndPointWUPIngresConduitIngres() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUP.IngresConduit.Ingres";
        return(endpointName);
    }

    public String getEndPointWUPIngres() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUP.Ingres";
        return(endpointName);
    }

    public String getEndPointWUPEgress() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUP.Egress";
        return(endpointName);
    }

    public String getEndPointWUPEgressConduitEgress() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUP.EgressConduit.Egress";
        return(endpointName);
    }

    public String getEndPointWUPContainerEgressGatekeeperIngres() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUPContainer.EgressGatekeeper.Ingres";
        return(endpointName);
    }

    public String getEndPointWUPContainerEgressProcessorIngres() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUPContainer.EgressProcessor.Ingres";
        return(endpointName);
    }

    public String getEndPointWUPContainerEgressProcessorEgress() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".WUPContainer.EgressProcessor.Egress";
        return(endpointName);
    }

    public String getEndPointInterchangePayloadTransformerIngres() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".Interchange.PayloadTransformer.Ingres";
        return(endpointName);
    }

    public String getEndPointInterchangePayloadTransformerEgress() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".Interchange.PayloadTransformer.Egress";
        return(endpointName);
    }

    public String getEndPointInterchangeRouterIngres() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".Interchange.Router.Ingres";
        return(endpointName);
    }

    public String getEndPointInterchangeRouterEgress() {
        String endpointName = INTRA_FUNCTION_DIRECT_TYPE + wupTypeName + ".Interchange.Router.Egress";
        return(endpointName);
    }

    public String getRouteIngresProcessorEgress2IngresGatekeeperIngres() {
        String endpointName = "FROM-" + wupTypeName + ".WUPC.IP.E-To-" + wupTypeName +".WUPC.IG.I" ;
        return(endpointName);
    }

    public String getRouteIngresConduitIngres2WUPIngres() {
        String endpointName = "FROM-" + wupTypeName + ".WUP.IC.I-To-" + wupTypeName +".WUP.I" ;
        return(endpointName);
    }

    public String getRouteWUPEgress2WUPEgressConduitEgress() {
        String endpointName = "FROM-" + wupTypeName + ".WUP.E-To-" + wupTypeName +".WUP.EC.E" ;
        return(endpointName);
    }

    public String getRouteWUPEgressConduitEgress2WUPEgressProcessorIngres() {
        String endpointName = "FROM-" + wupTypeName + ".WUP.EC.E-To-" + wupTypeName +".WUPC.EP.I" ;
        return(endpointName);
    }

    public String getRouteWUPEgressProcessorEgress2WUPEgressGatekeeperIngres() {
        String endpointName = "FROM-" + wupTypeName + ".WUP.EP.E-To-" + wupTypeName +".WUPC.EG.I" ;
        return(endpointName);
    }

    public String getRouteInterchangePayloadTransformerEgress2InterchangePayloadRouterIngres() {
        String endpointName = "FROM-" + wupTypeName + ".IC.PT.E-To-" + wupTypeName +".IC.R.I" ;
        return(endpointName);
    }

    public String getRouteWUPContainerIngressProcessor() {
        String endpointName = "FROM-" + wupTypeName + ".WUPC.IP.I-To-" + wupTypeName +".WUPC.IP.E" ;
        return(endpointName);
    }

    public String getRouteWUPContainerIngresGateway() {
        String endpointName = "FROM-" + wupTypeName + ".WUPC.IG.I-To-" + wupTypeName +".WUPC.IG.E" ;
        return(endpointName);
    }

    public String getRouteWUPContainerEgressGateway() {
        String endpointName = "FROM-" + wupTypeName + ".WUPC.EG.I-To-" + wupTypeName +".WUPC.EG.E" ;
        return(endpointName);
    }

    public String getRouteWUPContainerEgressProcessor() {
        String endpointName = "FROM-" + wupTypeName + ".WUPC.EP.I-To-" + wupTypeName +".WUPC.EP.E" ;
        return(endpointName);
    }

    public String getRouteInterchangePayloadTransformer(){
        String endpointName = "FROM-" + wupTypeName + ".IC.PT.I-To-" + wupTypeName +".IC.PT.E" ;
        return(endpointName);
    }

    public String getRouteInterchangeRouter(){
        String endpointName = "FROM-" + wupTypeName + ".IC.R.I-To-" + wupTypeName +".IC.R.E" ;
        return(endpointName);
    }
}
