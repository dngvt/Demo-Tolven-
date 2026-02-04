
/**
 * Method to select the laboratory order from the list and save as a relationship in trim.
 * @author Pinky
 * @param template
 * @param root
 * @param methodArgs
 */
saveLabOrder=function(template, root, methodArgs){
	var lArgs = splitArguments(methodArgs);
	var formId = lArgs[0];
	var rootForm = $(formId);
	$(formId + ":computeTemplate").value = template;
	$(formId + ":computeEnable").value = "true";
	$(formId + ":computeAction").value = 'add';
	var wipNode = rootForm.parentNode;
	var currentStep = 1 * rootForm.getAttribute('currentStep');
	ajaxSubmit4(rootForm, wipNode.id,currentStep);
	$(formId + ":computeEnable").value = "false";
}

saveLabResult=function(template, root, methodArgs){
	var lArgs = splitArguments(methodArgs);
	var formId = lArgs[0];
	var rootForm = $(formId);
	$(formId + ":computeTemplate").value = template;
	$(formId + ":computeEnable").value = "true";
	$(formId + ":computeAction").value = 'add';
	var wipNode = rootForm.parentNode;
	var currentStep = 1 * rootForm.getAttribute('currentStep');
	ajaxSubmit4(rootForm, wipNode.id,currentStep);
	$(formId + ":computeEnable").value = "false";
}



function addDiagnosisDetails(formId){
	var rootForm = $(formId);
	$(formId + ":computeEnableDiagnosis").value = "true";
	$(formId + ":computeActionDiagnosis").value = 'addDiagnosisDetails';
	var wipNode = rootForm.parentNode;
	var currentStep = 1 * rootForm.getAttribute('currentStep');
	ajaxSubmit4(rootForm, wipNode.id,currentStep);
	$(formId + ":computeEnableDiagnosis").value = "false";

}


function removeDiagnosis(formId,index){
	var rootForm = $(formId);
	$(formId + ":computeEnableDiagnosis").value = "true";
	$(formId + ":computePositionDiagnosis").value = index;
	$(formId + ":computeActionDiagnosis").value = 'remove';
	var wipNode = rootForm.parentNode;
	var currentStep = 1 * rootForm.getAttribute('currentStep');
	ajaxSubmit4(rootForm, wipNode.id,currentStep);
	$(formId + ":computeEnableDiagnosis").value = "false";	
}

function addProblemDetails(formId){
	var rootForm = $(formId);
	$(formId + ":computeEnableProblem").value = "true";
	$(formId + ":computeActionProblem").value = 'addProblemDetails';
	var wipNode = rootForm.parentNode;
	var currentStep = 1 * rootForm.getAttribute('currentStep');
	ajaxSubmit4(rootForm, wipNode.id,currentStep);
	$(formId + ":computeEnableProblem").value = "false";

}


function removeProblem(formId,index){
	var rootForm = $(formId);
	$(formId + ":computeEnableProblem").value = "true";
	$(formId + ":computePositionProblem").value = index;
	$(formId + ":computeActionProblem").value = 'remove';
	var wipNode = rootForm.parentNode;
	var currentStep = 1 * rootForm.getAttribute('currentStep');
	ajaxSubmit4(rootForm, wipNode.id,currentStep);
	$(formId + ":computeEnableProblem").value = "false";	
}


