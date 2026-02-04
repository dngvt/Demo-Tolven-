
	var VTEX = [false, false, false, false, false, false];
	var VTEN = [false, false, false, false, false, false];
	var VTED = [false, false, false, false, false, false];
        var grpName ;
	function qr1Init(qualityMeasureValue,operationType,VTEIndex){
//alert(" in qr1Init ");
           var qualityMeasureStatus = qualityMeasureValue;
	   var grp = document.getElementsByName(qualityMeasureValue.name);
           var i = qualityMeasureValue.id.replace(qualityMeasureValue.name+":","");
           grpName  = qualityMeasureValue.name.split(":")[0];
          var lastValue = (grp.length > 1 && (i == (grp.length -1))) ? true : false;

          //if the selected value is Exclusion
   
          
           if(operationType == "X") { 
              if(lastValue) {
               unCheckAllX(grp,i, VTEIndex);
              } else {
               markX(grp,i, VTEIndex);
              }
           } else if(operationType ==  "D") { 
             //alert(" SELECTED D : grp : " + grp + " VTEIndex : " + VTEIndex);

	     if(lastValue) {
               unCheckAllD(grp,i, VTEIndex);
              } else {
               markD(grp,i, VTEIndex);
               }
         
           } else  if(operationType == "N") {
             if(lastValue) {
               unCheckAllN(grp,i, VTEIndex);
              } else {
               markN(grp,i, VTEIndex);
              }
           } 
		 
	}

        function unCheckAllX(grp,i, VTEIndex) {
		 unCheckAll(grp, VTEIndex, VTEX);
		return true;
	} 

	function unCheckAllN(grp,i, VTEIndex) {
		 
		unCheckAll(grp, VTEIndex, VTEN)
		return true;
	} 
	
	function unCheckAllD(grp,i, VTEIndex) {
		 
		unCheckAll(grp, VTEIndex, VTED)
		return true;
	}
	
	function unCheckAll(grp, VTEIndex, stateArray) {
		if(grp[grp.length - 1].checked){
			for (var i = 0; i < grp.length - 1; i++) {
				grp[i].checked = false;
			}
			stateArray[VTEIndex] = false;
		}
		repaintResult();
		return true;
	} 
	

        function markX(grp,i, VTEIndex) {	
                markIt(i, VTEIndex, grp, VTEX)
		return true;
	}

	function markD(grp,i, VTEIndex) {	
                //alert(".111.marking D VTEIndex " + VTEIndex + " VTED[VTEIndex] "  + VTED[VTEIndex]);
        	markIt(i, VTEIndex, grp, VTED);
                //alert(".222.marking D VTEIndex " + VTEIndex + " VTED[VTEIndex] "  + VTED[VTEIndex]);
		return true;
 
	} 
	
	function markN(grp,i, VTEIndex) {	
	      markIt(i, VTEIndex, grp, VTEN)
	      return true;
	}


	function markIt(i, VTEIndex, grp, stateArray) {	
//alert(" mark it " + i + " grp[i] " + grp[i].id + " grp[i].checked " + grp[i].checked);
 		if(!grp[i].checked){
		    var lastOne = true;
			for (var j = 0; j< grp.length - 1; j++) {
				if(grp[j].checked) {
					lastOne = false;
	    			break;
				}
			}
			
			if(lastOne) {
				stateArray[VTEIndex] = false;
			 	repaintResult();
				return true;
			}
		}
   	       stateArray[VTEIndex] = true;
               if(grp.length > 1) {
	         grp[grp.length - 1].checked = false;
               }
	       repaintResult();
	       return true;
	}

	 function repaintResult(){
                var returnValue = "";
                var resultValue = "";

                resultValue = getResult(document.getElementsByName(grpName +":VTE1N"), document.getElementsByName(grpName +":VTE1D"), 0, "D");
		returnValue += resultValue;
                //alert(" resultValue ....0....." + resultValue);
		document.getElementById(grpName +":VTE_Prophylaxis").value = resultValue;

                resultValue = getResult(document.getElementsByName(grpName +":VTE2N"),document.getElementsByName(grpName +":VTE2D"), 1, "D");
		returnValue += resultValue;
                //alert(" resultValue ....1....." + resultValue);
		document.getElementById(grpName +":ICU_VTE_Prophylaxis").value = resultValue;
 

                resultValue = getResult(document.getElementsByName(grpName +":VTE3N"),document.getElementsByName(grpName +":VTE3D"), 2, "I");
		returnValue += resultValue;
                //alert(" resultValue ....2....." + resultValue + " ???? " + document.getElementsByName(grpName +":VTE3D")[0].id + " is checked " + document.getElementsByName(grpName +":VTE3D")[0].checked);
		document.getElementById(grpName +":Anticoagulation_Overlap_Therapy").value = resultValue;
 
                resultValue = getResult(document.getElementsByName(grpName +":VTE4N"),document.getElementsByName(grpName +":VTE4D"), 3, "I");
//alert( grpName + " VTE4D " + document.getElementsByName(grpName +":VTE4D")[0].id + " VTE5D " + document.getElementsByName(grpName +":VTE5D")[0].id + " VTE6D " + document.getElementsByName(grpName +":VTE6D")[0].id);
		returnValue += resultValue; 
                //alert(" resultValue ....3....." + resultValue);
		document.getElementById(grpName +":VTE_UFH").value = resultValue;

                resultValue = getResult(document.getElementsByName(grpName +":VTE5N"),document.getElementsByName(grpName +":VTE5D"), 4, "I");
		returnValue += resultValue; 
                //alert(" resultValue ....4....." + resultValue);
		document.getElementById(grpName +":Discharge_Instructions").value = resultValue;

 
                resultValue = getResult(document.getElementsByName(grpName +":VTE6N"),document.getElementsByName(grpName +":VTE6D"), 5, "I");
		returnValue += resultValue; 
                //alert(" resultValue ....5....." + resultValue);
		document.getElementById(grpName +":VTE_Potentially_Preventable").value =  resultValue;


                //alert(" returnValue " + returnValue);
		document.getElementById(grpName+":result").value = returnValue;

	}

      function getResult(grpN,grpD, VTEIndex, defValue){
	    var r = "";
            var grp;
            grp = grpN;
	    if (VTEX[VTEIndex]) {
                grp = grpN;
                r="X";
	    	VTEN[VTEIndex] = false;
	    	for (var i = 0; i < grp.length; i++) {
			 //grp[i].disabled = true;
			 grp[i].checked = false;
		 }
	    } else {
                grp = grpN;
	    	for (var i = 0; i < grp.length; i++) {
			 grp[i].disabled = false;
		}

		if (VTEN[VTEIndex]) { //If numerators are selected
		   grp=grpN;
		   if(VTEIndex == 3  ){
	   		 if(grp[0].checked == true && grp[1].checked == true  && grp[2].checked == true && grp[3].checked == true){
				r="N";
                         } else {
			       r= defValue;
                         }	

                  } else  if(VTEIndex == 4 ){
                   
	   	    if(grp[0].checked == true && grp[1].checked == true  && grp[2].checked == true && grp[3].checked == true && grp[4].checked == true){
			r="N";
                         } else {
			       r= defValue;
                         }	
                  } else  if(VTEIndex == 5  ){
	   	     if(grp[0].checked == true){
			r="N";
                     } else {
			       r= defValue;
                     }	
                 
              	  } else {
		      r="N";	
		  }	
		} else if (VTED[VTEIndex]) { //If denominators are selected
                   grp = grpD;
           // alert(" If denominators are selected VTEIndex ??? " + VTEIndex + " VTED[VTEIndex] " + VTED[VTEIndex] + " grpD[0] " + grpD[0].id+" is checked ?? " + grpD[0].checked);
	         if(VTEIndex == 2  ){
        		 if(grp[0].checked == true ){
				r="D";
                         } else {
			       r= defValue;
                         }	

                  } else  if(VTEIndex == 3 ){
	   	    if(grp[0].checked == true && grp[1].checked == true){
			r="D";
                         } else {
			       r= defValue;
                         }	
                  } else  if(VTEIndex == 4 ){
	   	    if(grp[0].checked == true && grp[1].checked == true){
			r="D";
                         } else {
			       r= defValue;
                         }	
                  } else  if(VTEIndex == 5  ){
//alert(" If denominators are selected  grp[0].checked " + grp[0].checked + " grp[1].checked " + grp[1].checked + " grp[2].checked " +  grp[2].checked  + " ..r...."+ r);
	   	     if(grp[0].checked == true && grp[1].checked == true && grp[2].checked == true){
			r="D";
                     } else {
			       r= defValue;
                     }	
                 
              	  } else {
		      r="D";	
		  }	
			

  	} else { //If exclusions are selected
                  
	    		r= defValue;
	    	}
	    }
	    
	    
	    
	    return r;
      }


/**
 * Function to show/hide as per the user's selection
 *
 **/
function showHiddenHCQM1(divId,radioButtonId,labelId){ 
 	var divElement = document.getElementById(divId); 
	var selection = document.getElementsByName(radioButtonId);
	var selectedValue = ""; 
	for (i=0; i<selection.length; i++){  
         	if (selection[i].checked == true){
          		 selectedValue = selection[i].value;
	
		}
        } 
	if(selectedValue == "yes"){
	  divElement.style.visibility = 'visible';
	  divElement.style.display = 'block';
	  checkByDefaultHCQM1(radioButtonId,labelId);
        
        } else{
           unCheckAllByDefaultHCQM1(radioButtonId,labelId);
	   divElement.style.visibility = 'hidden';
	   divElement.style.display = 'none';
        
       	} 
       
} 

/**
* Function to check default values
*/

function checkByDefaultHCQM1(radioButtonId,labelId){
	 	
		var iVTE = ['VTE3D','VTE4D','VTE5D','VTE6D'];
		var iVTEIndex = [2,3,4,5];
                  
 		if(radioButtonId == "iVTERadio"){
 			for (var i = 0; i < iVTE.length; i++){
 			   var iVTEElementArray = document.getElementsByName(labelId+":"+iVTE[i]);

                           for(var j = 0; j < iVTEElementArray.length ;j++) {
				   var iVTEElement = document.getElementById(labelId+":"+iVTE[i]+":"+j);
                                  //Call qr1Init to calculate the measures
                                   if(j == 0 ) {
  				       iVTEElement.checked = true;
 				        //Call qr1Init to calculate the measures
 				        qr1Init(iVTEElement,'D',iVTEIndex[i]);
                                    }
                            }
 			}
		}
 		
}
/**
* Function to check default values
*/
function unCheckAllByDefaultHCQM1(radioButtonId,labelId){
	 	
		var iVTE = ['VTE3D','VTE3X','VTE3N','VTE4D','VTE4X','VTE4N','VTE5D','VTE5X','VTE5N','VTE6D','VTE6X','VTE6N'];
		var iVTEIndex = [2,2,2,3,3,3,4,4,4,5,5,5];
                  if(radioButtonId == "iVTERadio"){
 			for (var i = 0; i < iVTE.length; i++){
			    var iVTEElementArray = document.getElementsByName(labelId+":"+iVTE[i]);
                            for(var j = 0; j < iVTEElementArray.length ;j++) {
				   var iVTEElement = document.getElementById(labelId+":"+iVTE[i]+":"+j);
				   iVTEElement.checked = false;
                                   
                                   var index = iVTEElement.id.replace(grpName+":","");
                                   var indexSplit = index.split(":"); 
                                   var grpDName   = indexSplit[0];
                                   var grpDID     = indexSplit[1];
                                   //alert(labelId+":"+iVTE[i] + " iVTE " + iVTE[i] + " iVTEIndex[i] " + iVTEIndex[i] + " grpDName " + grpDName);
                                   if(grpDName.indexOf("D") != -1) {
                                    qr1Init(iVTEElement,'D',iVTEIndex[i]);
                                   } else if(grpDName.indexOf("X") != -1) {
                                    qr1Init(iVTEElement,'X',iVTEIndex[i]);
                                   } else if(grpDName.indexOf("N") != -1) {
                                    qr1Init(iVTEElement,'N',iVTEIndex[i]);
                                   }
                             }
  			} 			 
        	  }
}

