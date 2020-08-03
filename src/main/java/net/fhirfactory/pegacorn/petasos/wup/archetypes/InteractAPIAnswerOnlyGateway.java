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

import net.fhirfactory.pegacorn.petasos.model.pathway.ContinuityID;

import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.*;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.common.GenericWUPTemplate;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public abstract class InteractAPIAnswerOnlyGateway extends GenericWUPTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(InteractAPIAnswerOnlyGateway.class);

    public InteractAPIAnswerOnlyGateway(){
        super();
    }

    @Override
    public WUPArchetypeEnum specifyWUPArchetype(){
        return(WUPArchetypeEnum.WUP_NATURE_API_ANSWER);
    }

     public void registerActivityStart(UoW unitOfWork, WUPClusterModeEnum clusterMode, WUPSystemModeEnum systemMode){
        LOG.debug(".registerActivityStart(): Entry, unitOfWork --> {}", unitOfWork);
        ContinuityID newContinuityID = new ContinuityID();
        newContinuityID.setPresentWUPFunctionToken(this.getWUPFunctionToken());
        newContinuityID.setPresentWUPInstanceID(this.getWupInstanceID());
     }

    public void registerActivityFinish(UoW unitOfWork){
        LOG.debug(".registerActivityFinish(): Entry, unitOfWork --> {}", unitOfWork);

    }
}
