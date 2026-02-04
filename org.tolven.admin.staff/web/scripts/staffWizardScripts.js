
//instantiate staff wizard to show the relavant steps for staff type selection
function updateStaffWizard(staffType,element,path){
	var index =0;
	var staffTrimName;
	while($(staffType+':'+index)){
		if($(staffType+':'+index).checked){
			instantiate($(staffType+':'+index).value,element,path);
			return;
		}
		index++;		
	}
}