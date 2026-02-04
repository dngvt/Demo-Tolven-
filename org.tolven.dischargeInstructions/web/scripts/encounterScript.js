function encounterItems(path,outputId,flag) {
	new Ajax.Request(
			'getEncounter.ajaxenc',
			{
				method: 'get',
				parameters: "&path="+path,
				onSuccess: function(request){
					if(flag=="false")
						displayItems(request.responseText,outputId);
					else
						displayDrilldownItems(request.responseText);
					},
				onFailure: function(request) {displayError(request,param);}
			});
}

function displayItems(response,outputId) {	
	var encType = outputId+":encType";
	var admitDate = outputId+":admitDate";
	var dischargeDate = outputId+":dischargeDate";
	var loc = outputId+":loc";
	var Items=response.evalJSON();		
	$(encType).innerHTML = Items.EncounterType;
	$(admitDate).innerHTML = Items.AdmitDate;
	$(dischargeDate).innerHTML = Items.DischargeDate;
	$(loc).innerHTML = Items.Location;	
}

function displayDrilldownItems(response){	
	var Items=response.evalJSON();
	$("drilldown:encType").innerHTML = Items.EncounterType;
	$("drilldown:admitDate").innerHTML = Items.AdmitDate;
	$("drilldown:dischargeDate").innerHTML = Items.DischargeDate;
	$("drilldown:loc").innerHTML = Items.Location;
}