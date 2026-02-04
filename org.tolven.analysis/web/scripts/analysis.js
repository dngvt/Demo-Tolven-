
/**
* Method to validate the age ranges entered.
* @author Pinky 
* Added on 09/07/2010 
*//*Copied from cchitWizardScripts. Would like to remove.*/
function validateAgeRanges(){

	var status = true;
	var lowAgeString = $("codesForm:lowAge").value;
	var lowAgeUnit = $("codesForm:lowAgeUnit").value;
	var highAgeString = $("codesForm:highAge").value;
	var highAgeUnit = $("codesForm:highAgeUnit").value;
	lowAge = parseInt(lowAgeString);
	highAge = parseInt(highAgeString);
	
	if((highAgeUnit == lowAgeUnit)&&(lowAge >= highAge))	
		status = false;
	
	else if(highAgeUnit!=lowAgeUnit){
		if((lowAgeUnit == "year")&&(highAgeUnit == "month")&&(lowAge*12 >= highAge))
			status = false;
		if((lowAgeUnit == "year")&&(highAgeUnit == "week")&&(lowAge*365 >= highAge*7))
			status = false;
		if((lowAgeUnit == "year")&&(highAgeUnit == "day")&&(lowAge*365 >= highAge))
			status = false;
		if((lowAgeUnit == "month")&&(highAgeUnit == "year")&&(lowAge >= highAge*12))
			status = false;
		if((lowAgeUnit == "month")&&(highAgeUnit == "week")&&(lowAge*30 >= highAge*7))
			status = false;
		if((lowAgeUnit == "month")&&(highAgeUnit == "day")&&(lowAge*30 >= highAge))
			status = false;
		if((lowAgeUnit == "week")&&(highAgeUnit == "year")&&(lowAge*7 >= highAge*365))
			status = false;
		if((lowAgeUnit == "week")&&(highAgeUnit == "month")&&(lowAge*7 >= highAge*30))
			status = false;
		if((lowAgeUnit == "week")&&(highAgeUnit == "day")&&(lowAge*7 >= highAge))
			status = false;
		if((lowAgeUnit == "day")&&(highAgeUnit == "year")&&(lowAge >= highAge*365))
			status = false;
		if((lowAgeUnit == "day")&&(highAgeUnit == "month")&&(lowAge >= highAge*30))
			status = false;
		if((lowAgeUnit == "day")&&(highAgeUnit == "week")&&(lowAge >= highAge*7))
			status = false;
	}
	
	if(status == false)
		$('ageRangeErrorBox').innerHTML = "Enter correct age range.Higher age range should be greater than low age range.";
	else
		$('ageRangeErrorBox').innerHTML = "";
	return status;
}


/**
 * Modified 'openTemplate' function to generate filtered pop-ups in analysis screens.
 * @author Pinky S
 * Added on 1/17/2011
 * @param contentName
 * @param placeholderid
 * @param methodName
 * @param formId
 * @param index
 * @param popupType
 * @param gridType
 *//*Copied from cchitWizardScripts. Would like to remove.*/
function openTemplateWithFilteredItems(contentName, placeholderid, methodName, formId, index, popupType,gridType)
{
 var lform = $(formId);

 // Async submission should be stopped when its required to submit form explicitly.
 // For ex. In Add Diagnosis wizard the form is explicitly submitted upon selecting Diagnosis.
 // During this time Async form submission should be stopped until form is refreshed.

 // set true to Stop Async Submission
 // Should make sure its set back to false at appropriate time or else asyn submission would stop working completely in the session.
 stopAsync(formId);

 // Build  required paramters (concactenated with '|' ) that will be passed back to javascript Method Name
  var lArguments = new Array();
  	lArguments.push(formId);
  	lArguments.push(index);

  var methodArgsStr = buildArguments(lArguments);
  
  openPopupWithFilteredItems( contentName, placeholderid, formId,  methodName, methodArgsStr,gridType,popupType);
}


/**
 * Modified 'openPopup' function to generate filtered pop-ups in analysis screens.
 * @author Pinky S
 * Added on 1/17/2011
 * @param contentName
 * @param placeholderid
 * @param formId
 * @param methodName
 * @param methodArgs
 * @param gridType
 * @param popupType
 * @return
 *//*Copied from cchitWizardScripts. Would like to remove.*/
function openPopupWithFilteredItems( contentName, placeholderid, formId, methodName, methodArgs,gridType,popupType){
	 serialNo++;
	// Tolven.Util.log( "Getting: " + contentName );
	 $('downloadStatus').innerHTML="Get " + contentName + "...";

	 // Update this block when ever a similar  new wizard is added.

	 // Update this block whenever a similar new wizard is added.
		new Ajax.Request(
		  'createGridWithFilteredItems.ajaxcchit',
		  {
			method: 'get',
			parameters: "element="+contentName+"&gridId="+placeholderid+"&gridType="+gridType+"&methodArgs="+methodArgs+"&methodName="+methodName+"&formId="+formId+"&popupType="+popupType,
			onSuccess: function(request){ setPopupContentFiltered(request,  placeholderid, formId ); },
			onFailure: function(request) {displayError(request,param);}
		  });
	}



/**
 * Modified 'setPopupContent' function to change the path of the close button image.
 * @author Pinky S
 * Added on 1/17/2011
 * @param req
 * @param placeholderid
 * @param formId
 * @return
 *//*Copied from cchitWizardScripts. Would like to remove.*/
function setPopupContentFiltered( req, placeholderid, formId){
	 var prefHTML = "";
	  prefHTML += "<div class=\"popupgridheader\">";
	  prefHTML += "<img class='closetab' src='/Tolven/images/x_black.gif' onclick=\"closePopup('" + $(placeholderid).id + "','" + formId + "' );return false;\"/>&nbsp;" ;
	  prefHTML += "</div>";
	  prefHTML += req.responseText;

	 // This is required for Grid to filter and refresh data.
	 visiblePage = placeholderid;
	var popupElement = $(placeholderid);
	 $(popupElement).update(prefHTML);
	 popupElement.style.display = 'block';
	 popupElement.style.top = document.body.clientHeight * .30 + "px";
	 popupElement.style.left = document.body.clientWidth * .30 + "px";

	}
