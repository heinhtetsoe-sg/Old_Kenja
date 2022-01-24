function btn_submit(cmd) {
    if (cmd == 'knjz100changeDiv') {
        document.forms[0].selectdata.value = '';
    } else {
        //
        for (var i = 0; i < document.forms[0].SCHOOL_NAME.length; i++)
        {  
          document.forms[0].SCHOOL_NAME.options[i].selected = 0;
        }

        var comma = '';
        document.forms[0].selectdata.value = "";
        for (var i = 0; i < document.forms[0].SCHOOL_SELECTED.length; i++)
        {  
          document.forms[0].SCHOOL_SELECTED.options[i].selected = 1;
          document.forms[0].selectdata.value += comma + document.forms[0].SCHOOL_SELECTED.options[i].value;
          comma = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if (document.forms[0].SCHOOL_SELECTED.length == 0)
	{
		alert('{rval MSG916}');
		return;
	}


	//
	for (var i = 0; i < document.forms[0].SCHOOL_NAME.length; i++)
	{  
		document.forms[0].SCHOOL_NAME.options[i].selected = 0;
	}

  var comma = '';
  document.forms[0].selectdata.value = "";
	for (var i = 0; i < document.forms[0].SCHOOL_SELECTED.length; i++)
	{  
		document.forms[0].SCHOOL_SELECTED.options[i].selected = 1;
    document.forms[0].selectdata.value += comma + document.forms[0].SCHOOL_SELECTED.options[i].value;
    comma = ",";
	}
  

    action = document.forms[0].action;
    target = document.forms[0].target;

	    document.forms[0].action = SERVLET_URL +"/KNJZ";
		document.forms[0].target = "_blank";
	    document.forms[0].submit();

	    document.forms[0].action = action;
	    document.forms[0].target = target;
}
	
function compareByValue(a, b) {
    var rtn;
    if (a.value < b.value) {
        rtn = -1;
    } else if (a.value > b.value) {
        rtn = 1;
    } else {
        rtn = 0;
    }
    return rtn;
}

function move_(side, left, right)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var attribute1;
    var attribute2;
    var i;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        temp2.push(attribute2.options[i]);
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                temp2.push(attribute1.options[i]);
            } else {
                temp1.push(attribute1.options[i]);
            }
        } else {
            temp2.push(attribute1.options[i]);
        }
    }

    //sort
    temp2.sort(compareByValue);

    //generating new options
    attribute2.options.innerHTML = optToHtml(temp2);
    
    //generating new options
    attribute1.options.innerHTML = optToHtml(temp1);
}

function optToHtml(optionArray) {
    var text = "";
    optionArray.forEach(function(o) {
        text += "<option value='";
        text += o.value;
        text += "'>";
        text += o.text;
    });
    return text;
}

