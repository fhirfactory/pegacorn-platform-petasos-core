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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.EpisodeIdentifier;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelIdentifier;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelFinalisationStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcelProcessingStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;

public class PetasosParcelAuditTrailEntry {
    private Date auditTrailEntryDate;
    private UoW actualUoW;
    private ResilienceParcelIdentifier identifier;
    private ResilienceParcelFinalisationStatusEnum parcelFinalsationStatus;
    private ResilienceParcelProcessingStatusEnum processingStatus;
    private HashSet<WUPIdentifier> alternativeWUPIdentifierSet;
    private HashSet<ResilienceParcelIdentifier> alternativeParcelIdentifiersSet;
    private HashSet<EpisodeIdentifier> downstreamEpisodeIdentifierSet;
    private EpisodeIdentifier upstreamEpisodeIdentifier;
    private WUPIdentifier primaryWUPIdentifier;
    private FDNToken parcelTypeID;
    private Date parcelRegistrationDate;
    private Date parcelStartDate;
    private Date parcelFinishedDate;
    private Date parcelFinalisedDate;
    private Date parcelCancellationDate;

    //
    // Constructor(s)
    //

    public PetasosParcelAuditTrailEntry(){
        this.auditTrailEntryDate = null;
        this.actualUoW = null;
        this.identifier = null;
        this.parcelFinalsationStatus = null;
        this.alternativeWUPIdentifierSet = null;
        this.processingStatus = null;
        this.downstreamEpisodeIdentifierSet = null;
        this.upstreamEpisodeIdentifier = null;
        this.primaryWUPIdentifier = null;
        this.parcelRegistrationDate = null;
        this.parcelTypeID = null;
        this.parcelStartDate = null;
        this.parcelFinishedDate = null;
        this.parcelFinalisedDate = null;
        this.alternativeParcelIdentifiersSet = null;        
    }
    
    public PetasosParcelAuditTrailEntry(ResilienceParcel theParcel ){
        // First, we clean the slate
        this.auditTrailEntryDate = null;
        this.actualUoW = null;
        this.identifier = null;
        this.parcelFinalsationStatus = null;
        this.alternativeWUPIdentifierSet = null;
        this.processingStatus = null;
        this.downstreamEpisodeIdentifierSet = null;
        this.upstreamEpisodeIdentifier = null;
        this.primaryWUPIdentifier = null;
        this.parcelRegistrationDate = null;
        this.parcelTypeID = null;
        this.parcelStartDate = null;
        this.parcelFinishedDate = null;
        this.parcelFinalisedDate = null;
        this.alternativeParcelIdentifiersSet = null;
        // Then, we try and add what we get given
        if( theParcel == null ){
            return;
        }
        this.auditTrailEntryDate = Date.from(Instant.now());
        if( theParcel.hasActualUoW()) {
            this.actualUoW = theParcel.getActualUoW();
        }
        if(theParcel.hasDownstreamEpisodeIdentifierSet()){
            this.downstreamEpisodeIdentifierSet = new HashSet<EpisodeIdentifier>();
        }
        if(theParcel.hasUpstreamEpisodeIdentifier()){
            this.upstreamEpisodeIdentifier = theParcel.getUpstreamEpisodeIdentifier();
        }
        if(theParcel.hasTypeID()){
            this.parcelTypeID = theParcel.getTypeID();
        }
        if(theParcel.hasFinalisationDate()){
            this.parcelFinalisedDate = theParcel.getFinalisationDate();
        }
        if(theParcel.hasInstanceIdentifier()){
            this.identifier = theParcel.getIdentifier();
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
        if(theParcel.hasAssociatedWUPIdentifier()){
            this.primaryWUPIdentifier = theParcel.getAssociatedWUPIdentifier();
        }
    }

    //
    // Bean/Attribute Helper Methods
    //
    
    // Helpers for the this.downstreamEpisodeIDSet attribute
    
    public boolean hasDownstreamParcelIDSet() {
    	if(this.downstreamEpisodeIdentifierSet == null ) {
    		return(false);
    	}
    	if(this.downstreamEpisodeIdentifierSet.isEmpty()) {
    		return(false);
    	}
    	return(true);
    }
    
    public void setDownstreamEpisodeIdentifierSet(HashSet<EpisodeIdentifier> newDownstreamEpisodeIdentifierSet) {
    	if(newDownstreamEpisodeIdentifierSet == null) {
    		this.downstreamEpisodeIdentifierSet = null;
    		return;
    	}
    	if(newDownstreamEpisodeIdentifierSet.isEmpty()) {
    		this.downstreamEpisodeIdentifierSet = new HashSet<EpisodeIdentifier>();
    		return;
    	}
    	this.downstreamEpisodeIdentifierSet = newDownstreamEpisodeIdentifierSet;
    }
    
    public Set<EpisodeIdentifier> getDownstreamEpisodeIdentifierSet() {
    	if(this.downstreamEpisodeIdentifierSet == null) {
    		return(null);
    	}
    	return(this.downstreamEpisodeIdentifierSet);
    }

    // Helpers for the this.alternativeWUPInstance attribute

    public boolean hasAlternativeWUPInstanceIDSet(){
        if(this.alternativeWUPIdentifierSet == null ){
            return(false);
        }
        if(this.alternativeWUPIdentifierSet.isEmpty()){
            return(false);
        }
        return(true);
    }
    
    public void setAlternativeWUPIdentifierSet(HashSet<WUPIdentifier> alternativeWUPIdentifierSet) {
    	this.alternativeWUPIdentifierSet = alternativeWUPIdentifierSet;
    }

    public Set<WUPIdentifier> getAlternativeWUPIdentifierSet(){
        return(this.alternativeWUPIdentifierSet);
    }

    // Helpers for the this.auditTrailEntryDate attribute

    public boolean hasAuditTrailEntryDate(){
        if(this.auditTrailEntryDate == null){
            return(false);
        }
        return(true);
    }

    @JsonSerialize(using=JsonDateSerializer.class)
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
    
    public void setProcessingStatus(ResilienceParcelProcessingStatusEnum newProcessingStatus) {
        this.processingStatus = newProcessingStatus;
    }    

    public void setUowOutcome(ResilienceParcelProcessingStatusEnum newProcessingStatus) {
        this.processingStatus = newProcessingStatus;
    }

    // Helpers for the this.upstreamEpisodeID attribute

    public boolean hasUpstreamParcelID(){
        if(this.upstreamEpisodeIdentifier ==null){
            return(false);
        }
        return(true);
    }

    public EpisodeIdentifier getUpstreamEpisodeIdentifier() {
        return(upstreamEpisodeIdentifier);
    }

    public void setUpstreamEpisodeIdentifier(EpisodeIdentifier upstreamEpisodeIdentifier) {
        this.upstreamEpisodeIdentifier = upstreamEpisodeIdentifier;
    }

    // Helpers for the this.primaryWUPInstanceID attribute

    public boolean hasPrimaryWUPInstanceID(){
        if(this.primaryWUPIdentifier==null){
            return(false);
        }
        return(true);
    }

    public WUPIdentifier getPrimaryWUPInstanceID() {
        return primaryWUPIdentifier;
    }

    public void setPrimaryWUPInstanceID(WUPIdentifier primaryWUPInstanceID) {
        this.primaryWUPIdentifier = primaryWUPInstanceID;
    }

    // Helpers for the this.parcelRegistrationDate attribute

    public boolean hasParcelRegistrationDate(){
        if(this.parcelRegistrationDate==null){
            return(false);
        }
        return(true);
    }

    @JsonSerialize(using=JsonDateSerializer.class)
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


    @JsonSerialize(using=JsonDateSerializer.class)
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

    @JsonSerialize(using=JsonDateSerializer.class)
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

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getParcelFinalisedDate() {
        return parcelFinalisedDate;
    }

    public void setParcelFinalisedDate(Date parcelFinalisedDate) {
        this.parcelFinalisedDate = parcelFinalisedDate;
    }

    public ResilienceParcelIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(ResilienceParcelIdentifier identifier) {
        this.identifier = identifier;
    }

    public ResilienceParcelFinalisationStatusEnum getParcelFinalsationStatus() {
        return parcelFinalsationStatus;
    }

    public void setParcelFinalsationStatus(ResilienceParcelFinalisationStatusEnum parcelFinalsationStatus) {
        this.parcelFinalsationStatus = parcelFinalsationStatus;
    }

    public WUPIdentifier getPrimaryWUPIdentifier() {
        return primaryWUPIdentifier;
    }

    public void setPrimaryWUPIdentifier(WUPIdentifier primaryWUPIdentifier) {
        this.primaryWUPIdentifier = primaryWUPIdentifier;
    }

    public FDNToken getParcelTypeID() {
        return parcelTypeID;
    }

    public void setParcelTypeID(FDNToken parcelTypeID) {
        this.parcelTypeID = parcelTypeID;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getParcelCancellationDate() {
        return parcelCancellationDate;
    }

    public void setParcelCancellationDate(Date parcelCancellationDate) {
        this.parcelCancellationDate = parcelCancellationDate;
    }
}
