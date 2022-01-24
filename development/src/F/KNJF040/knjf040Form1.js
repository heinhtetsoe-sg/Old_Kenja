function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if (document.forms[0].category_selected.length == 0)
	{
		alert('{rval MSG916}');
		return;
	}
	if ( (document.forms[0].CHECK1.checked==false) && (document.forms[0].CHECK2.checked==false) && (document.forms[0].CHECK3.checked==false) && (document.forms[0].CHECK4.checked==false) && (document.forms[0].CHECK5.checked==false) && (document.forms[0].CHECK6.checked==false) && (document.forms[0].CHECK7.checked==false) && (document.forms[0].CHECK8.checked==false) && (document.forms[0].CHECK9.checked==false) )
	{
		alert('{rval MSG916}');
		return;
	}
	if (document.forms[0].CHECK9.checked == true)
	{
		ind = document.forms[0].SELECT1.selectedIndex;
		ind2 = document.forms[0].SELECT2.selectedIndex;
		if((document.forms[0].SELECT1.options[ind].value == "01") && (document.forms[0].SELECT2.options[ind2].value == "01"))
		{
// 2003/11/15
//			alert('{rval MSG916}');
			alert('一般条件または歯・口腔条件を指定して下さい。');
			return;
		}
	}

	if ((document.forms[0].CHECK3.checked == true) || (document.forms[0].CHECK4.checked == true))
	{
		if(document.forms[0].DATE.value == "")
		{
// 2003/11/15
//			alert('{rval MSG916}');
			alert('提出日を指定して下さい。');
			return;
		}
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

	    document.forms[0].action = SERVLET_URL +"/KNJF";
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

function move1(side)
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
		tempaa[y] = String(attribute2.options[i].text).substring(9,12)+","+y; // 2004/01/23
	}

	//assign new values to arrays
	for (var i = 0; i < attribute1.length; i++)
	{   
		if ( attribute1.options[i].selected )
		{  
			y=current1++
			temp1[y] = attribute1.options[i].value;
			tempa[y] = attribute1.options[i].text; 
			tempaa[y] = String(attribute1.options[i].text).substring(9,12)+","+y; // 2004/01/23
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



function moves(sides)
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
		tempaa[z] = String(attribute6.options[i].text).substring(9,12)+","+z; // 2004/01/23
	}

	//assign new values to arrays
	for (var i = 0; i < attribute5.length; i++)
	{   
		z=current5++
		temp5[z] = attribute5.options[i].value;
		tempc[z] = attribute5.options[i].text; 
		tempaa[z] = String(attribute5.options[i].text).substring(9,12)+","+z; // 2004/01/23
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

function DataUse(obj1)
{
	if((document.forms[0].CHECK3.checked == true) || (document.forms[0].CHECK4.checked == true))
	{
		flag1 = false;
	}
	else
	{
		flag1 = true;
	}
	document.forms[0].DATE.disabled = flag1;
	document.forms[0].btn_calen.disabled = flag1;
}


function SelectUse(obj2)
{
	if(document.forms[0].CHECK9.checked == true)
	{
		document.forms[0].SELECT1.disabled = false;
		document.forms[0].SELECT2.disabled = false;
	}
	else
	{
		document.forms[0].SELECT1.disabled = true;
		document.forms[0].SELECT2.disabled = true;
	}
}


function OptionUse(obj3)
{
	if(document.forms[0].CHECK6.checked == true)
	{
		document.forms[0].OUTPUT[0].disabled = false;
		document.forms[0].OUTPUT[1].disabled = false;
	}
	else
	{
		document.forms[0].OUTPUT[0].disabled = true;
		document.forms[0].OUTPUT[1].disabled = true;
	}
}

function OptionUse2(obj4)	// 2003/11/27 nakamoto
{
	if(document.forms[0].CHECK1.checked == true)
	{
		document.forms[0].OUTPUTA[0].disabled = false;
		document.forms[0].OUTPUTA[1].disabled = false;
	}
	else
	{
		document.forms[0].OUTPUTA[0].disabled = true;
		document.forms[0].OUTPUTA[1].disabled = true;
	}
}


function OptionUse3(obj5)	// 2003/11/27 nakamoto
{
	if(document.forms[0].CHECK2.checked == true)
	{
		document.forms[0].OUTPUTB[0].disabled = false;
		document.forms[0].OUTPUTB[1].disabled = false;
	}
	else
	{
		document.forms[0].OUTPUTB[0].disabled = true;
		document.forms[0].OUTPUTB[1].disabled = true;
	}
}

function dis_date(flag)
{
	document.forms[0].DATE.disabled = flag;
	document.forms[0].btn_calen.disabled = flag;
}

