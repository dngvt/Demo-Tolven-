
/**
 * Method to select the image order from the list and save as a relationship in trim.
 * @author Pinky
 * @param template
 * @param root
 * @param methodArgs
 */
saveImageOrder=function(template, root, methodArgs){
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