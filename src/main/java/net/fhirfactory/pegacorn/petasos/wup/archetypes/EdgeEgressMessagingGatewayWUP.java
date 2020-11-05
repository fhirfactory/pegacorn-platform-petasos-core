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

package net.fhirfactory.pegacorn.petasos.wup.archetypes;

import net.fhirfactory.pegacorn.petasos.core.moa.wup.GenericMOAWUPTemplate;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPArchetypeEnum;

public abstract class EdgeEgressMessagingGatewayWUP extends GenericMOAWUPTemplate {

    protected static final String IPC_FRAME_DECODER = "ipcFrameDecoder";
    protected static final String IPC_STRING_DECODER = "ipcStringDecoder";
    protected static final String IPC_STRING_ENCODER = "ipcStringEncoder";
    private static final String DEFAULT_NETTY_PARAMS = 
            "?allowDefaultCodec=false&decoders=#" + IPC_FRAME_DECODER + "&encoders=#" + IPC_STRING_ENCODER + 
            "&keepAlive=false&clientMode=true&sync=true&sendBufferSize=" + IPC_PACKET_MAXIMUM_FRAME_SIZE;
        
    @Override
    protected WUPArchetypeEnum specifyWUPArchetype(){
        return(WUPArchetypeEnum.WUP_NATURE_MESSAGE_EXTERNAL_EGRESS_POINT);
    }
    
    @Override
    protected String specifyEgressEndpoint(){
        getLogger().debug(".specifyEgressEndpoint(): Entry");
        String egressEndPointString;
        egressEndPointString = specifyEndpointComponentDefinition();
        egressEndPointString = egressEndPointString + ":";
        egressEndPointString = egressEndPointString + this.specifyEndpointProtocol();
        egressEndPointString = egressEndPointString + this.specifyEndpointProtocolLeadIn();
        egressEndPointString = egressEndPointString + deriveTargetEndpointDetails();
        egressEndPointString = egressEndPointString + specifyEndpointProtocolLeadout();
        getLogger().debug(".specifyEgressEndpoint(): Exit, egressEndPointString --> {}", egressEndPointString);
        return(egressEndPointString);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedIngresEndpoint(){
        return(true);
    }

    @Override
    protected String specifyIngresEndpointVersion() {
        return null;
    }

    @Override
    protected String specifyIngresTopologyEndpointName() {
        return null;
    }

    @Override
    protected String specifyIngresEndpoint(){
        getLogger().debug(".specifyIngresEndpoint(): Entry");
        String endpoint = this.getNameSet().getEndPointWUPIngres();
        getLogger().debug(".specifyIngresEndpoint(): Exit, ingresPoint --> {}", endpoint);
        return(endpoint);
    }

    @Override
    protected boolean specifyUsesWUPFrameworkGeneratedEgressEndpoint() {
        return(false);
    }


    protected String specifyEndpointComponentDefinition() {
        return ("netty");
    }

    protected String specifyEndpointProtocol() {
        return ("tcp");
    }

    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    protected String specifyEndpointProtocolLeadout() {
        return (DEFAULT_NETTY_PARAMS);
    }

    @Override
    abstract protected String specifyEgressTopologyEndpointName();
    @Override
    abstract protected String specifyEgressEndpointVersion();
    abstract protected String deriveTargetEndpointDetails();
}
