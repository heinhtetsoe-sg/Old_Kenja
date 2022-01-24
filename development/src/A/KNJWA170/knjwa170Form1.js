function btn_submit(cmd) {

    if (cmd == "update") {
        if (document.forms[0].STR_DATE.value > document.forms[0].END_DATE.value) {
            alert("日付範囲が不正です。");
            return;
        }

        var flag = "";
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "PRINT_CHECK[]" 
                   && document.forms[0].elements[i].checked) {
                flag = "on";
            }
            
        }
        if (flag == "") {
            alert("チェックボックスが選択されておりません。");
            return;
        }
        
    }
    
    if (cmd == "read2"){
		if (document.forms[0].category_name.length == 0)
		{
			alert('{rval MSG916}');
			return;
		}
	    attribute3 = document.forms[0].selectdata;
    	attribute3.value = "";
	    sep = "";
		for (var i = 0; i < document.forms[0].category_selected.length; i++)
		{  
			document.forms[0].category_selected.options[i].selected = 0;
		}

		for (var i = 0; i < document.forms[0].category_name.length; i++)
		{  
			document.forms[0].category_name.options[i].selected = 1;
	        attribute3.value = attribute3.value + sep + document.forms[0].category_name.options[i].value;
    	    sep = ",";
		}
	}
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "PRINT_CHECK[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
    
}
//-----------------
function ClearList(OptionList, TitleName) 
{
	OptionList.length = 0;
}
	
function AllClearList(OptionList, TitleName) 
{
		attribute = document.forms[0].category_name;
        ClearList(attribute,attribute);
		attribute = document.forms[0].category_selected;
        ClearList(attribute,attribute);
}
function move(side)
{   
	var temp1 = new Array();
	var temp2 = new Array();
	var tempa = new Array();
	var tempb = new Array();
	var tempaa = new Array();	// 2004/01/23
	var current1 = 0;
	var current2 = 0;
	var y=0;
	var attribute;
	
	//assign what select attribute treat as attribute1 and attribute2
	if (side == "right" || side == "rightall")
	{  
		attribute1 = document.forms[0].category_name;
		attribute2 = document.forms[0].category_selected;
	}
	else
	{  
		attribute1 = document.forms[0].category_selected;
		attribute2 = document.forms[0].category_name;  
	}

	
	//fill an array with old values
	for (var i = 0; i < attribute2.length; i++)
	{  
		temp1[current2] = attribute2.options[i].value;
		tempa[current2] = attribute2.options[i].text;
		tempaa[current2] = String(attribute2.options[i].text).substring(9,12)+","+current2; // 2004/01/23
		current2++;
	}

	if (side == "rightall" || side == "leftall")
	{  
	    for (var i = 0; i < attribute1.length; i++)
	    {  
		    attribute1.options[i].selected = 1;
	    }
	}

	//assign new values to arrays
	for (var i = 0; i < attribute1.length; i++)
	{   
		if ( attribute1.options[i].selected )
		{  
			temp1[current2] = attribute1.options[i].value;
			tempa[current2] = attribute1.options[i].text; 
			tempaa[current2] = String(attribute1.options[i].text).substring(9,12)+","+current2; // 2004/01/23
			current2++;
		}
		else
		{  
			temp2[current1] = attribute1.options[i].value; 
			tempb[current1] = attribute1.options[i].text;
			current1++;
		}
	}

	ClearList(attribute2,attribute2);

    tempaa.sort();	// 2004/01/23

	//generating new options // 2004/01/23
	for (var i = 0; i < current2; i++)
	{  
        var val = tempaa[i];
        var tmp = val.split(',');

		attribute2.options[i] = new Option();
		attribute2.options[i].value = temp1[tmp[1]];
		attribute2.options[i].text =  tempa[tmp[1]];
//		attribute2.options[i] = new Option();
//		attribute2.options[i].value = temp1[i];
//		attribute2.options[i].text =  tempa[i];
	}
    attribute2.length = current2;

	//generating new options
	ClearList(attribute1,attribute1);
	if (current1>0)
	{	
		for (var i = 0; i < current1; i++)
		{   
			attribute1.options[i] = new Option();
			attribute1.options[i].value = temp2[i];
			attribute1.options[i].text =  tempb[i];
		}
	}
    attribute1.length = current1;

}
//-----------------
//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

	//url = location.hostname;
    
	//document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    
    document.forms[0].action = action;
    document.forms[0].target = target;
}

