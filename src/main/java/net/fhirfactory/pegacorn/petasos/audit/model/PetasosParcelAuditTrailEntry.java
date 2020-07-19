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

package net.fhirfactory.pegacorn.petasos.audit.model;

import net.fhirfactory.pegacorn.common.model.FDNTokenSet;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;

import java.time.Instant;
import java.util.Date;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelFinalisationStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;

public class PetasosParcelAuditTrailEntry {
    private Date auditTrailEntryDate;
    private UoW actualUoW;
    private FDNToken parcelInstanceID;
    private ResilienceParcelFinalisationStatusEnum parcelFinalsationStatus;
    private ResilienceParcelProcessingStatusEnum processingStatus;
    private FDNTokenSet alternativeWUPInstanceIDSet;
    private FDNTokenSet alternativeParcelIDSet;
    private FDNTokenSet downstreamEpisodeIDSet;
    private FDNToken upstreamEpisodeID;
    private FDNToken primaryWUPInstanceID;
    private FDNToken parcelTypeID;
    private Date parcelRegistrationDate;
    private Date parcelStartDate;
    private Date parcelFinishedDate;
    private Date parcelFinalisedDate;
    private Date parcelCancellationDate;

    //
    // Constructor(s)
    //

    public PetasosParcelAuditTrailEntry(ResilienceParcel theParcel ){
        // First, we clean the slate
        this.auditTrailEntryDate = null;
        this.actualUoW = null;
        this.parcelInstanceID = null;
        this.parcelFinalsationStatus = null;
        this.alternativeWUPInstanceIDSet = null;
        this.processingStatus = null;
        this.downstreamEpisodeIDSet = null;
        this.upstreamEpisodeID = null;
        this.primaryWUPInstanceID = null;
        this.parcelRegistrationDate = null;
        this.parcelTypeID = null;
        this.parcelStartDate = null;
        this.parcelFinishedDate = null;
        this.parcelFinalisedDate = null;
        this.alternativeParcelIDSet = null;
        // Then, we try and add what we get given
        if( theParcel == null ){
            return;
        }
        this.auditTrailEntryDate = Date.from(Instant.now());
        if( theParcel.hasActualUoW()) {
            this.actualUoW = theParcel.getActualUoW();
        }
        if(theParcel.hasDownstreamEpisodeIDSet()){
            this.downstreamEpisodeIDSet = new FDNTokenSet(theParcel.getDownstreamEpisodeIDSet());
        }
        if(theParcel.hasUpstreamEpisodeID()){
            this.upstreamEpisodeID = theParcel.getUpstreamEpisodeID();
        }
        if(theParcel.hasTypeID()){
            this.parcelTypeID = theParcel.getTypeID();
        }
        if(theParcel.hasFinalisationDate()){
            this.parcelFinalisedDate = theParcel.getFinalisationDate();
        }
        if(theParcel.hasInstanceID()){
            this.parcelInstanceID = theParcel.getInstanceID();
        }
        if(theParcel.hasFinishedDate()){
            this.parcelFinishedDate = theParcel.getFinishedDate();
        }
        if(theParcel.hasRegistrationDate()){
            this.parcelRegistrationDate = theParcel.getRegistrationDate();
        }
        if(theParcel.hasStartDate()){
            this.parcelStartDate = theParcel.getStartDate();
        }
        if(theParcel.hasCancellationDate()){
            this.parcelCancellationDate = theParcel.getCancellationDate();
        }
        if(theParcel.hasProcessingStatus()){
            this.processingStatus = theParcel.getProcessingStatus();
        }
        if(theParcel.hasFinalisationStatus()){
            this.parcelFinalsationStatus = theParcel.getFinalisationStatus();
        }
    }

    //
    // Bean/Attribute Helper Methods
    //
    
    // Helpers for the this.downstreamEpisodeIDSet attribute
    
    public boolean hasDownstreamParcelIDSet() {
    	if(this.downstreamEpisodeIDSet == null ) {
    		return(false);
    	}
    	if(this.downstreamEpisodeIDSet.isEmpty()) {
    		return(false);
    	}
    	return(true);
    }
    
    public void setDownstreamEpisodeIDSet(FDNTokenSet newDownstreamParcelIDSet) {
    	if(newDownstreamParcelIDSet == null) {
    		this.downstreamEpisodeIDSet = null;
    		return;
    	}
    	if(newDownstreamParcelIDSet.isEmpty()) {
    		this.downstreamEpisodeIDSet = new FDNTokenSet();
    		return;
    	}
    	this.downstreamEpisodeIDSet = new FDNTokenSet(newDownstreamParcelIDSet);
    }
    
    public FDNTokenSet getDownstreamEpisodeIDSet() {
    	if(this.downstreamEpisodeIDSet == null) {
    		return(null);
    	}
    	FDNTokenSet fdnSetCopy = new FDNTokenSet(this.downstreamEpisodeIDSet);
    	return(fdnSetCopy);
    }

    // Helpers for the this.alternativeWUPInstance attribute

    public boolean hasAlternativeWUPInstanceIDSet(){
        if(this.alternativeWUPInstanceIDSet == null ){
            return(false);
        }
        if(this.alternativeWUPInstanceIDSet.isEmpty()){
            return(false);
        }
        return(true);
    }
    
    public void setAlternativeWUPInstanceIDSet(FDNTokenSet alternativeWUPInstanceIDSet) {
    	if(alternativeWUPInstanceIDSet == null )
    	{
    		this.alternativeWUPInstanceIDSet = new FDNTokenSet();
    	}
        this.alternativeWUPInstanceIDSet = new FDNTokenSet(alternativeWUPInstanceIDSet);
    }

    public FDNTokenSet getAlternativeWUPInstanceIDSet(){
        if(hasAlternativeWUPInstanceIDSet()){
        	FDNTokenSet newFDNSet = new FDNTokenSet(this.alternativeWUPInstanceIDSet);
            return(this.alternativeWUPInstanceIDSet);
        }
        return(null);
    }

    // Helpers for the this.auditTrailEntryDate attribute

    public boolean hasAuditTrailEntryDate(){
        if(this.auditTrailEntryDate == null){
            return(false);
        }
        return(true);
    }

    public Date getAuditTrailEntryDate() {
        return auditTrailEntryDate;
    }

    public void setAuditTrailEntryDate(Date auditTrailEntryDate) {
        this.auditTrailEntryDate = auditTrailEntryDate;
    }

    // Helpers for the this.work attribute

    public boolean hasActualUoW(){
        if(this.actualUoW == null ){
            return(false);
        }
        return(true);
    }

    public UoW getActualUoW() {
        return actualUoW;
    }

    public void setActualUoW(UoW actualUoW) {
        this.actualUoW = actualUoW;
    }

    // Helpers for the this.uowOutcome attribute

    public boolean hasProcessingStatus(){
        if(this.processingStatus == null){
            return(false);
        }
        return(true);
    }
    public ResilienceParcelProcessingStatusEnum getProcessingStatus() {
        return(this.processingStatus);
    }

    public void setUowOutcome(ResilienceParcelProcessingStatusEnum newProcessingStatus) {
        this.processingStatus = newProcessingStatus;
    }

    // Helpers for the this.upstreamEpisodeID attribute

    public boolean hasUpstreamParcelID(){
        if(this.upstreamEpisodeID==null){
            return(false);
        }
        return(true);
    }

    public FDNToken getUpstreamEpisodeID() {
        return(upstreamEpisodeID);
    }

    public void setUpstreamEpisodeID(FDNToken upstreamEpisodeID) {
        this.upstreamEpisodeID = upstreamEpisodeID;
    }

    // Helpers for the this.primaryWUPInstanceID attribute

    public boolean hasPrimaryWUPInstanceID(){
        if(this.primaryWUPInstanceID==null){
            return(false);
        }
        return(true);
    }

    public FDNToken getPrimaryWUPInstanceID() {
        return primaryWUPInstanceID;
    }

    public void setPrimaryWUPInstanceID(FDNToken primaryWUPInstanceID) {
        this.primaryWUPInstanceID = primaryWUPInstanceID;
    }

    // Helpers for the this.parcelRegistrationDate attribute

    public boolean hasParcelRegistrationDate(){
        if(this.parcelRegistrationDate==null){
            return(false);
        }
        return(true);
    }
    public Date getParcelRegistrationDate() {
        return parcelRegistrationDate;
    }

    public void setParcelRegistrationDate(Date parcelRegistrationDate) {
        this.parcelRegistrationDate = parcelRegistrationDate;
    }

    // Helpers for the this.parcelStartDate attribute

    public boolean hasParcelStartDate(){
        if(this.parcelStartDate==null){
            return(false);
        }
        return(true);
    }

    public Date getParcelStartDate() {
        return parcelStartDate;
    }

    public void setParcelStartDate(Date parcelStartDate) {
        this.parcelStartDate = parcelStartDate;
    }

    // Helpers for the this.parcelFinishedDate attribute

    public boolean hasParcelFinishedDate(){
        if(this.parcelFinishedDate==null){
            return(false);
        }
        return(true);
    }

    public Date getParcelFinishedDate() {
        return parcelFinishedDate;
    }

    public void setParcelFinishedDate(Date parcelFinishedDate) {
        this.parcelFinishedDate = parcelFinishedDate;
    }

    // Helpers for the this.parcelFinalisedDate attribute

    public boolean hasParcelFinalisedDate(){
        if(this.parcelFinalisedDate==null){
            return(false);
        }
        return(true);
    }

    public Date getParcelFinalisedDate() {
        return parcelFinalisedDate;
    }

    public void setParcelFinalisedDate(Date parcelFinalisedDate) {
        this.parcelFinalisedDate = parcelFinalisedDate;
    }
}
