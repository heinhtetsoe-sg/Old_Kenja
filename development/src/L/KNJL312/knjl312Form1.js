function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
function move(side)
{   
	var temp1 = new Array();
	var temp2 = new Array();
	var tempa = new Array();
	var tempb = new Array();
	var tempaa = new Array();
	var current1 = 0;
	var current2 = 0;
	var y=0;
	var attribute;
	
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

	
	for (var i = 0; i < attribute2.length; i++)
	{  
		temp1[current2] = attribute2.options[i].value;
		tempa[current2] = attribute2.options[i].text;
		tempaa[current2] = String(attribute2.options[i].value).substr(0,5)+","+current2;	//NO002
		current2++;
	}

	if (side == "rightall" || side == "leftall")
	{  
	    for (var i = 0; i < attribute1.length; i++)
	    {  
		    attribute1.options[i].selected = 1;
	    }
	}

	for (var i = 0; i < attribute1.length; i++)
	{   
		if ( attribute1.options[i].selected )
		{  
			temp1[current2] = attribute1.options[i].value;
			tempa[current2] = attribute1.options[i].text; 
			tempaa[current2] = String(attribute1.options[i].value).substr(0,5)+","+current2;	//NO002
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

    tempaa.sort();

	for (var i = 0; i < current2; i++)
	{  
        var val = tempaa[i];
        var tmp = val.split(',');

		attribute2.options[i] = new Option();
		attribute2.options[i].value = temp1[tmp[1]];
		attribute2.options[i].text =  tempa[tmp[1]];
	}
    attribute2.length = current2;

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
//印刷
function newwin(SERVLET_URL){

	for (var i = 0; i < document.forms[0].category_name.length; i++)
	{  
		document.forms[0].category_name.options[i].selected = 1;
	}

	for (var i = 0; i < document.forms[0].category_selected.length; i++)
	{  
		document.forms[0].category_selected.options[i].selected = 0;
	}
	if (document.forms[0].category_name.length == 0)
	{
		alert('{rval MSG916}');
        return;
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

