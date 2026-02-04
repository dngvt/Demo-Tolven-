
	var STKX = [false, false, false, false, false, false,false];
	var STKN = [false, false, false, false, false, false,false];
	var STKD = [false, false, false, false, false, false,false];
        var grpName ;
	function qr2Init(qualityMeasureValue,operationType,STKIndex){
        var qualityMeasureStatus = qualityMeasureValue;
//alert("qualityMeasureValue" + qualityMeasureValue +  " operationType "+ operationType  + " STKIndex " +STKIndex);
	var grp = document.getElementsByName(qualityMeasureValue.name);
        var i = qualityMeasureValue.id.replace(qualityMeasureValue.name+":","");
        grpName  = qualityMeasureValue.name.split(":")[0];

	//alert(grpName);
        var lastValue = (grp.length > 1 && (i ==( (grp.length -1)))) ? true : false;
        //if the selected value is Exclusion
  
        
           if(operationType == "X") { 
         
              if(lastValue) {
               unCheckStkAllX(grp,i, STKIndex);
              } else {
               markStkX(grp,i, STKIndex);
              }
           } else if(operationType ==  "D") { 
		if(lastValue) {
               unCheckStkAllD(grp,i, STKIndex);
              } else {
               markStkD(grp,i, STKIndex);
              }
         
           } else  if(operationType == "N") {
             if(lastValue) {
               unCheckStkAllN(grp,i, STKIndex);
              } else {
               markStkN(grp,i, STKIndex);
              }
           } 
		 
	}

        function unCheckStkAllX(grp,i, STKIndex) {
		 unCheckStkAll(grp, STKIndex, STKX);
		return true;
	} 

	function unCheckStkAllN(grp,i, STKIndex) {
		 
		unCheckStkAll(grp, STKIndex, STKN)
		return true;
	} 
	
	function unCheckStkAllD(grp,i, STKIndex) {
		 
		unCheckStkAll(grp, STKIndex, STKD)
		return true;
	}
	
	function unCheckStkAll(grp, STKIndex, stateArray) {
		if(grp[grp.length - 1].checked){
			for (var i = 0; i < grp.length - 1; i++) {
				grp[i].checked = false;
			}
			stateArray[STKIndex] = false;
		}
		repaintResultStk();
		return true;
	} 
	

        function markStkX(grp,i, STKIndex) {	
                markItStk(i, STKIndex, grp, STKX)
		return true;
	}

	function markStkD(grp,i, STKIndex) {	
        	markItStk(i, STKIndex, grp, STKD)
		return true;
 
	} 
	
	function markStkN(grp,i, STKIndex) {	
	      markItStk(i, STKIndex, grp, STKN)
	      return true;
	}


	function markItStk(i, STKIndex, grp, stateArray) {	
 
		if(!grp[i].checked){
		    var lastOne = true;
			for (var j = 0; j< grp.length - 1; j++) {
				if(grp[j].checked) {
					lastOne = false;
	    			break;
				}
			}
			
			if(lastOne) {
				stateArray[STKIndex] = false;
			 	repaintResultStk();
				return true;
			}
		}
	
	       stateArray[STKIndex] = true;
	       if(grp.length > 1){
	       grp[grp.length - 1].checked = false;
	       }
	       repaintResultStk();
	       return true;
	}

	 function repaintResultStk(){
 
	   var r = "";
 
		r += getResultStk(document.getElementsByName(grpName +":STK2N"),document.getElementsByName(grpName +":STK2D"), 0, "I");

		document.getElementById(grpName +":stk2_value").value = getResultStk(document.getElementsByName(grpName +":STK2N"),document.getElementsByName(grpName +":STK2D"), 0, "I");
 		
		r += getResultStk(document.getElementsByName(grpName +":STK3N"),document.getElementsByName(grpName +":STK3D"), 1, "I");

		document.getElementById(grpName +":stk3_value").value = getResultStk(document.getElementsByName(grpName +":STK3N"),document.getElementsByName(grpName +":STK3D"), 1, "I");

		
		r += getResultStk(document.getElementsByName(grpName +":STK4N"),document.getElementsByName(grpName +":STK4D"), 2, "I");
		
		document.getElementById(grpName +":stk4_value").value = getResultStk(document.getElementsByName(grpName +":STK4N"),document.getElementsByName(grpName +":STK4D"), 2, "I");
 
		r += getResultStk(document.getElementsByName(grpName +":STK5N"),document.getElementsByName(grpName +":STK5D"), 3, "I");

		
		document.getElementById(grpName +":stk5_value").value = getResultStk(document.getElementsByName(grpName +":STK5N"),document.getElementsByName(grpName +":STK5D"), 3, "I");

		r += getResultStk(document.getElementsByName(grpName +":STK6N"),document.getElementsByName(grpName +":STK6D"), 4, "I");

		
		document.getElementById(grpName +":stk6_value").value = getResultStk(document.getElementsByName(grpName +":STK6N"),document.getElementsByName(grpName +":STK6D"), 4, "I");
 
		r += getResultStk(document.getElementsByName(grpName +":STK8N"),document.getElementsByName(grpName +":STK8D"), 5, "I");

		
		document.getElementById(grpName +":stk8_value").value =  getResultStk(document.getElementsByName(grpName +":STK8N"),document.getElementsByName(grpName +":STK8D"), 5, "I");

		r += getResultStk(document.getElementsByName(grpName +":STK10N"),document.getElementsByName(grpName +":STK10D"), 6, "I");

		
		document.getElementById(grpName +":stk10_value").value =  getResultStk(document.getElementsByName(grpName +":STK10N"),document.getElementsByName(grpName +":STK10D"), 6, "I");

		

		document.getElementById(grpName+":result").value = r;
		
 
	}


function getResultStk(grpN,grpD, STKIndex, defValue){
	    var r = "";
            var grp;
	    
            grp = grpN;
	    if (STKX[STKIndex]) {
                r="X";
	    	STKN[STKIndex] = false;
	    	for (var i = 0; i < grp.length; i++) {
			 //grp[i].disabled = true;
			 grp[i].checked = false;
		 }
	    } else {
	    	for (var i = 0; i < grp.length; i++) {
			 grp[i].disabled = false;
		}

		if (STKN[STKIndex]) {
			grp = grpN;
			if (STKIndex == 5){
                           
                           if(grp[0].checked == true && grp[1].checked == true && grp[2].checked == true && grp[3].checked == true && grp[4].checked == true && grp[5].checked == true) {
                             r="N";
                           } else {
                             r= defValue;
                           }
			} else {
			  //alert("....1 or 2 or 3 or 4 or 6............"+grp[0].checked);
                          if(grp[0].checked == true) {
                            r = "N";
                          } else {

                            r= defValue;
                          }
                        }

                   
		} else if (STKD[STKIndex]) {
                        grp = grpD
                  	if (STKIndex == 1){
                          
                           if(grp[0].checked == true && grp[1].checked == true) {
                             r="D";
                           } else {
                             r= defValue;
                           }
                        } else if (STKIndex == 4){
                           
                           if(grp[0].checked == true && (grp[1].checked == true || grp[2].checked == true || grp[3].checked == true || grp[4].checked == true )) {
                             r="D";
                           } else {
                             r= defValue;
                           }
			} else {
			  //alert("Denominator"+grp[0].checked);
                          if(grp[0].checked == true) {
                            r= "D";
                          } else {
                            r= defValue;
                          }
			  //alert("Denominator R   :"+r) ;
                        }	    	
                } else { 
                        grp = grpN;
	    		r= defValue;
	    	}
	    }

//alert(" r of getResultStk "+r)
	    return r;
}
	
	
/**
 * Function to show/hide as per the user's selection
 *
 **/
function showHidden(divId,radioButtonId,labelId){ 
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
	  checkByDefault(radioButtonId,labelId);
        
        } else{
           unCheckAllByDefault(radioButtonId,labelId);
	   divElement.style.visibility = 'hidden';
	   divElement.style.display = 'none';
        
       	} 
       
       } 

/**
* Function to check default values
*/

function checkByDefault(radioButtonId,labelId){
	 	
		var iSTK = ['STK2D','STK3D','STK4D','STK5D','STK6D'];
		var hSTK = ['STK8D','STK10D'];
 		if(radioButtonId == "iStrokeRadio"){
 			for (var i = 0; i < iSTK.length; i++){
 			   var iSTKElementArray = document.getElementsByName(labelId+":"+iSTK[i]);

                           for(var j = 0; j < iSTKElementArray.length ;j++) {
				   var iSTKElement = document.getElementById(labelId+":"+iSTK[i]+":"+j);
                                  //Call qr2Init to calculate the measures
                                   if(j == 0 ) {
				     iSTKElement.checked = true;
                                     qr2Init(iSTKElement,'D',i);
                                    }
                            }
 			}

                        //Call qr2Init to calculate the measures
                        //var iSTKElementArrayDef = document.getElementsByName(labelId+":"+iSTK[0]);
                        //qr2Init(iSTKElementArrayDef[0],'D',0);
		}
		if(radioButtonId == "hStrokeRadio"){
  
 			for (var i = 0; i < hSTK.length; i++){
			   //alert("inside for loophStrokeRadio ");
			   var hSTKElementArray = document.getElementsByName(labelId+":"+hSTK[i]);

                           for(var j = 0; j < hSTKElementArray.length ;j++) {
				   var hSTKElement = document.getElementById(labelId+":"+hSTK[i]+":"+j);
                                   if(j == 0 ) {
				      hSTKElement.checked = true; 
                                      qr2Init(hSTKElement,'D',(i + iSTK.length));
                                   }
                            }
  
			}
                       //Call qr2Init to calculate the measures
                       //var hSTKElementArrayDef = document.getElementsByName(labelId+":"+hSTK[0]);
                       //qr2Init(hSTKElementArrayDef[0],'D',0);
             }
		
}
/**
* Function to check default values
*/
function unCheckAllByDefault(radioButtonId,labelId){
	 	
		var iSTK = ['STK2D','STK2X','STK2N','STK3D','STK3X','STK3N','STK4D','STK4X','STK4N','STK5D','STK5X','STK5N','STK6D','STK6X','STK6N'];
		var iSTKIndex = [0,0,0,1,1,1,2,2,2,3,3,3,4,4,4];
		var hSTK = ['STK8D','STK8X','STK8N','STK10D','STK10X','STK10N'];
		var hSTKIndex = [5,5,5,6,6,6];
		if(radioButtonId == "iStrokeRadio"){
 			for (var i = 0; i < iSTK.length; i++){
			   var iSTKElementArray = document.getElementsByName(labelId+":"+iSTK[i]);
                            for(var j = 0; j < iSTKElementArray.length ;j++) {
				   var iSTKElement = document.getElementById(labelId+":"+iSTK[i]+":"+j);
				   iSTKElement.checked = false;
//alert(" iSTK " + iSTK[i] + " iSTKIndex[i] " + iSTKIndex[i]);

                                   var index = iSTKElement.id.replace(grpName+":","");
                                   var indexSplit = index.split(":"); 
                                   var grpDName   = indexSplit[0];
                                   var grpDID     = indexSplit[1];
                                   if(grpDName.indexOf("D") != -1) {
                                      qr2Init(iSTKElement,'D',iSTKIndex[i]);
                                   } else if(grpDName.indexOf("X") != -1) {
                                     qr2Init(iSTKElement,'X',iSTKIndex[i]);
                                   } else if(grpDName.indexOf("N") != -1) {
                                     qr2Init(iSTKElement,'N',iSTKIndex[i]);
                                   }
                            }
  			}
       	        }
		if(radioButtonId == "hStrokeRadio"){
 			for (var i = 0; i < hSTK.length; i++){
			   var hSTKElementArray = document.getElementsByName(labelId+":"+hSTK[i]);
                            for(var j = 0; j < hSTKElementArray.length ;j++) {
				   var hSTKElement = document.getElementById(labelId+":"+hSTK[i]+":"+j);
				   hSTKElement.checked = false;

                                   var index = iSTKElement.id.replace(grpName+":","");
                                   var indexSplit = index.split(":"); 
                                   var grpDName   = indexSplit[0];
                                   var grpDID     = indexSplit[1];
//alert(" hSTK " + hSTK[i] + " hSTKIndex[i] " + hSTKIndex[i]);
                        //Call qr2Init to calculate the measures 
                                   if(grpDName.indexOf("D") != -1) {
                                      qr2Init(hSTKElement,'D',hSTKIndex[i]);
                                   } else if(grpDName.indexOf("X") != -1) {
                                     qr2Init(hSTKElement,'X',hSTKIndex[i]);
                                   } else if(grpDName.indexOf("N") != -1) {
                                     qr2Init(hSTKElement,'N',hSTKIndex[i]);
                                   }
                            }
  			}

    		}
		
}

