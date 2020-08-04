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
package net.fhirfactory.pegacorn.petasos.pathway.servicemodule.interchange.worker;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class EndpointNameSet {
    Set<String> nameSet;

    public EndpointNameSet(){
        this.nameSet = new LinkedHashSet<String>();
    }

    public Set<String> getNameSet() {
        return nameSet;
    }

    public void setNameSet(Set<String> nameSet) {
        this.nameSet = nameSet;
    }

    public void addEndpointName(String name){
        this.nameSet.add(name);
    }

    public void removeEndpointName(String name){
        if(nameSet.isEmpty()) {
            return;
        }
        Iterator<String> setIterator = nameSet.iterator();
        while(setIterator.hasNext()){
            String currentEndpointName = setIterator.next();
            if(currentEndpointName.contentEquals(name)){
                nameSet.remove(currentEndpointName);
                return;
            }
        }
    }

    public boolean isEmpty(){
        if(nameSet == null){
            return(true);
        }
        if(nameSet.isEmpty()){
            return(true);
        } else {
            return(false);
        }
    }
}
