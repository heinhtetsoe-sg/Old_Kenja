function btn_submit(cmd) {

	if (cmd == "csv"){
//		alert("作成中です。");
//		return;
		if (document.forms[0].category_selected.length == 0)
		{
			alert('{rval MSG916}');
			return;
		}
		if (document.forms[0].category_selected.length > 20)
		{
			alert("一度に出力が可能な件数は、最大２０件までです。");
			return;
		}

	    attribute3 = document.forms[0].selectdata;
    	attribute3.value = "";
	    sep = "";
		for (var i = 0; i < document.forms[0].category_name.length; i++)
		{  
			document.forms[0].category_name.options[i].selected = 0;
		}

		for (var i = 0; i < document.forms[0].category_selected.length; i++)
		{  
			document.forms[0].category_selected.options[i].selected = 1;
	        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
    	    sep = ",";
		}
	}

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;

}

//異動対象日付変更
function tmp_list(cmd, submit) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}

function newwin(SERVLET_URL){

	if (document.forms[0].category_selected.length == 0)
	{
		alert('{rval MSG916}');
		return;
	}
	if (document.forms[0].DATE.value == '')/* NO003 */
	{
		alert("異動対象日付が未入力です。");
		return;
	}
		//
		for (var i = 0; i < document.forms[0].category_name.length; i++)
		{  
			document.forms[0].category_name.options[i].selected = 0;
		}

		for (var i = 0; i < document.forms[0].category_selected.length; i++)
		{  
			document.forms[0].category_selected.options[i].selected = 1;
		}

	    action = document.forms[0].action;
	    target = document.forms[0].target;

//		url = location.hostname;
//		document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
	    document.forms[0].action = SERVLET_URL +"/KNJD";
    	document.forms[0].target = "_blank";
	    document.forms[0].submit();

	    document.forms[0].action = action;
	    document.forms[0].target = target;
}

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
function move1(side,chdt)
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
	if (side == "left")
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
		y=current1++
		temp1[y] = attribute2.options[i].value;
		tempa[y] = attribute2.options[i].text;
		if (chdt == 1){
			tempaa[y] = String(attribute2.options[i].text).substring(9,12)+","+y; // 2004/01/23
		}else {
			tempaa[y] = String(attribute2.options[i].value)+","+y; // NO005
		}
	}

	//assign new values to arrays
	for (var i = 0; i < attribute1.length; i++)
	{   
		if ( attribute1.options[i].selected )
		{  
			y=current1++
			temp1[y] = attribute1.options[i].value;
			tempa[y] = attribute1.options[i].text; 
			if (chdt == 1){
				tempaa[y] = String(attribute1.options[i].text).substring(9,12)+","+y; // 2004/01/23
			}else {
				tempaa[y] = String(attribute1.options[i].value)+","+y; // NO005
			}
		}
		else
		{  
			y=current2++
			temp2[y] = attribute1.options[i].value; 
			tempb[y] = attribute1.options[i].text;
		}
	}

    tempaa.sort();	// 2004/01/23

	//generating new options // 2004/01/23
	for (var i = 0; i < temp1.length; i++)
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

	//generating new options
	ClearList(attribute1,attribute1);
	if (temp2.length>0)
	{	
		for (var i = 0; i < temp2.length; i++)
		{   
			attribute1.options[i] = new Option();
			attribute1.options[i].value = temp2[i];
			attribute1.options[i].text =  tempb[i];
		}
	}

}
function moves(sides,chdt)
{   
	var temp5 = new Array();
	var tempc = new Array();
	var tempaa = new Array();	// 2004/01/23
	var current5 = 0;
	var z=0;
	
	//assign what select attribute treat as attribute5 and attribute6
	if (sides == "left")
	{  
		attribute5 = document.forms[0].category_name;
		attribute6 = document.forms[0].category_selected;
	}
	else
	{  
		attribute5 = document.forms[0].category_selected;
		attribute6 = document.forms[0].category_name;  
	}

	
	//fill an array with old values
	for (var i = 0; i < attribute6.length; i++)
	{  
		z=current5++
		temp5[z] = attribute6.options[i].value;
		tempc[z] = attribute6.options[i].text;
		if (chdt == 1){
			tempaa[z] = String(attribute6.options[i].text).substring(9,12)+","+z; // 2004/01/23
		}else {
			tempaa[z] = String(attribute6.options[i].value)+","+z; // NO005
		}
	}

	//assign new values to arrays
	for (var i = 0; i < attribute5.length; i++)
	{   
		z=current5++
		temp5[z] = attribute5.options[i].value;
		tempc[z] = attribute5.options[i].text; 
		if (chdt == 1){
			tempaa[z] = String(attribute5.options[i].text).substring(9,12)+","+z; // 2004/01/23
		}else {
			tempaa[z] = String(attribute5.options[i].value)+","+z; // NO005
		}
	}

    tempaa.sort();	// 2004/01/23

	//generating new options // 2004/01/23
	for (var i = 0; i < temp5.length; i++)
	{  
        var val = tempaa[i];
        var tmp = val.split(',');

		attribute6.options[i] = new Option();
		attribute6.options[i].value = temp5[tmp[1]];
		attribute6.options[i].text =  tempc[tmp[1]];
//		attribute6.options[i] = new Option();
//		attribute6.options[i].value = temp5[i];
//		attribute6.options[i].text =  tempc[i];
	}

	//generating new options
	ClearList(attribute5,attribute5);

}
