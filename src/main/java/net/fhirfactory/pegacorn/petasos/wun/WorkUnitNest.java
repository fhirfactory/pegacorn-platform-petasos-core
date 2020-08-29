package net.fhirfactory.pegacorn.petasos.wun;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPFunctionToken;

import javax.inject.Inject;
import java.util.HashMap;

public class WorkUnitNest {
    HashMap<FDNToken, WUPFunctionToken> activeWUPSet;

    @Inject


    public void registerWUPInstance(FDNToken wupID, WUPFunctionToken wupFunction){
        if(activeWUPSet.containsKey(wupID)){
            return;
        }
        activeWUPSet.put(wupID,wupFunction);
    }



}
