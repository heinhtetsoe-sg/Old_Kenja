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
		tempaa[current2] = String(attribute2.options[i].value).substr(0,4)+","+current2;
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
			tempaa[current2] = String(attribute1.options[i].value).substr(0,4)+","+current2;
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
//??????
function newwin(SERVLET_URL){
	if (document.forms[0].category_name.length == 0)
	{
		alert('{rval MSG916}');
        return;
	}
	for (var i = 0; i < document.forms[0].category_name.length; i++)
	{  
		document.forms[0].category_name.options[i].selected = 1;
	}

	for (var i = 0; i < document.forms[0].category_selected.length; i++)
	{  
		document.forms[0].category_selected.options[i].selected = 0;
	}

	var no   = document.forms[0].category_name.value.split('-'); //????????????
	var sno  = document.forms[0].noinf_st.value; 				//????????????(????????????)
	var eno  = document.forms[0].noinf_ed.value; 				//????????????(????????????)
    var rno2 = no[1].replace(/^0+/, "");
    var rno3 = no[2].replace(/^0+/, "");
	var no2  = parseInt(rno2);									//????????????:??????????????????
	var no3  = parseInt(rno3);									//????????????:??????????????????
	var sns ;
	var ens ;
	if(document.forms[0].category_name.length == 1){
		//??????????????????
//	    if (/[^0-9]/.test(sno)) {
//    	    alert("?????????????????????????????????");
//			document.forms[0].noinf_st.value = "";
//        	return;
//	    }
//	    if (/[^0-9]/.test(eno)) {
//    	    alert("?????????????????????????????????");
//			document.forms[0].noinf_ed.value = "";
//        	return;
//	    }
		//?????????????????????????????????
		if((document.forms[0].noinf_ed.value > 0) && (document.forms[0].noinf_st.value == "")){
			alert("???????????????????????????????????????");
			return;
		}
		//ALL'0'????????????'0'??????(.test??????????????????)
	    if (/^0+$/.test(sno)) {
    	    sns = 0;
	    } else if (/^0+$/.test(eno)){
    	    ens = 0;
	    } else {
			//ZERO???????????????
    	    var sn2 = sno.replace(/^0+/, "");
    	    var en2 = eno.replace(/^0+/, "");
			//????????????
        	sns = parseInt(sn2);
        	ens = parseInt(en2);
	    }
		//???????????????????????????????????????
		if((((no3 < sns) || (no2 > sns)) && ((ens > no3) || (ens < no2)))){
			alert("??????????????????????????????(" + no2 + "???" + no3 + ")");
			document.forms[0].noinf_st.value = "";
			document.forms[0].noinf_ed.value = "";
			return;
		}
		if((no3 < sns) || (no2 > sns)){
			alert("??????????????????????????????(" + no2 + "???" + no3 + ")");
			document.forms[0].noinf_st.value = "";
			return;
		}else if((ens > no3) || (ens < no2)){
			alert("??????????????????????????????(" + no2 + "???" + no3 + ")");
			document.forms[0].noinf_ed.value = "";
			return;
		}else if(sns > ens){
			alert("????????????????????????????????????");
			document.forms[0].noinf_st.value = "";
			document.forms[0].noinf_ed.value = "";
			return;
		}
	} else {
			document.forms[0].noinf_st.value = "";
			document.forms[0].noinf_ed.value = "";
	}
    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

