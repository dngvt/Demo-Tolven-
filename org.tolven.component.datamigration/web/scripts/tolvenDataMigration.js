checkMenuStructure = function(){

	var msPath = $("migrateExtendedFieldsMS").value;
	if(msPath.length == 0)
		return;
	var myAjax = new Ajax.Request(
	   'checkMenuStructure.migration',
	   {
	    method: 'post',
	    parameters: 'msPath='+msPath,
	    onFailure: function(req) {
	    	alert("Error in tolvenDataMigration.js:checkMenuStructure"+req.responseText);	    	
	    },
	    onSuccess: function(req) {
	    	$("checkMenuStructureStatus").update(req.responseText);
	    }
	   });
}
checkMenuColumn = function(){
	var msPath = $("migrateExtendedFieldsMS").value;
	var newHeading = $("newExtendedFieldHeading").value;
	
	if(msPath.length == 0 || newHeading.length == 0)
		return;
	var myAjax = new Ajax.Request(
	   'checkOldMenuColumn.migration',
	   {
	    method: 'post',
	    parameters: 'msPath='+msPath+'&newHeading='+newHeading,
	    onFailure: function(req) {
	    	alert("Error in tolvenDataMigration.js:checkMenuColumn"+req.responseText);	    	
	    },
	    onSuccess: function(req) {
	    	if(req.responseText.length != 0){
	    		$("checkFieldName").update(req.responseText);
	    		$("startDataMigrationButton").setAttribute('disabled',true);	    		
	    	}else{
	     		$("checkFieldName").update("");
	    		$("startDataMigrationButton").removeAttribute('disabled');	    		
	    		
	    	}
	    }
	   });
}
startDataMigration = function(){
	var msPath = $("migrateExtendedFieldsMS").value;
	var oldHeading = $("oldExtendedFieldHeading").value;
	var newHeading = $("newExtendedFieldHeading").value;
	
	if(msPath.length == 0 || oldHeading.length == 0 || newHeading.length == 0)
		return;
	var myAjax = new Ajax.Request(
	   'triggerMigration.migration',
	   {
	    method: 'post',
	    parameters: 'msPath='+msPath+'&oldHeading='+oldHeading+'&newHeading='+newHeading,
	    onFailure: function(req) {
	    	alert("Error in tolvenDataMigration.js:startDataMigration"+req.responseText);	    	
	    },
	    onSuccess: function(req) {
	    	$("extendedFieldMigrationStatus").update(req.responseText);
	    }
	   });
}


getExtendedFields =function(){
	var mdId = $("menuDataID").value;
	if(isNaN(mdId)){
		alert("Only numbers are expected");
	}
	if(mdId.length == 0)
		return;
	var myAjax = new Ajax.Request(
	   'findExtededFields.migration',
	   {
	    method: 'post',
	    parameters: 'id='+mdId,
	    onFailure: function(req) {
	    	alert("Error in tolvenDataMigration.js:startRollbackMigration"+req.responseText);	    	
	    },
	    onSuccess: function(req) {
	    	$("extendedFields").update(req.responseText);
	    }
	   });
}



startRollbackMigration = function(){
	var changeId = $("migrateChangeId").value;
	
	if(changeId.length == 0 )
		return;
	var myAjax = new Ajax.Request(
	   'rollBackMigrationChanges.migration',
	   {
	    method: 'post',
	    parameters: 'changeId='+changeId,
	    onFailure: function(req) {
	    	alert("Error in tolvenDataMigration.js:startRollbackMigration"+req.responseText);	    	
	    },
	    onSuccess: function(req) {
	    	$("rollbackMigrationStatus").update(req.responseText);
	    }
	   });
}