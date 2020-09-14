package net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming;

import org.apache.camel.Exchange;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WUPRouteIDInjector {
    private final String EXCHANGE_WUP_KEY_PROPERTY = "PetasosWorkUnitProcessorKey";

    public void injectWUPKey(Exchange camelExchange, String wupKey){
        camelExchange.setProperty(EXCHANGE_WUP_KEY_PROPERTY, wupKey);
    }

    public String extractWUPKey(Exchange camelExchange){
        return(camelExchange.getProperty(EXCHANGE_WUP_KEY_PROPERTY, String.class));
    }
}
