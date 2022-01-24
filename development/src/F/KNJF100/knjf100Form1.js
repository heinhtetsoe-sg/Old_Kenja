function btn_submit(cmdb)			//読込ボタン
{
	if (document.forms[0].DATE1.value == "")
	{
		alert("日付が不正です。");
		document.forms[0].DATE1.focus();
		return;
	}
	if (document.forms[0].DATE2.value == "")
	{
		alert("日付が不正です。");
		document.forms[0].DATE2.focus();
		return;
	}

	var val = document.forms[0].SEME_DATE.value;
	var tmp = val.split(',');
	var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
	var tmp3 = document.forms[0].DATE2.value.split('/'); //印刷範囲終了日付
	var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
	var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
	var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
	var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
	var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
	var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）

	if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) > new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		alert("日付の大小が不正です。");
		return;
	}
	var flag1 = 0;
	var flag2 = 0;
	var val_seme = "";
	val_seme = document.forms[0].SEMESTER.value;
	if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
	{
		if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
		{
			flag1 = 1; //1学期
		}
	}
	if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
	{
		if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
		{
			flag1 = 2; //2学期
		}
	}
	if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
	{
		if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
		{
			flag1 = 3; //3学期
		}
	}
	if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
		{
			flag2 = 1; //1学期
		}
	}
	if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
		{
			flag2 = 2; //2学期
		}
	}
	if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
		{
			flag2 = 3; //3学期
		}
	}
	if (flag1 != flag2)
	{
		alert("指定範囲が学期をまたがっています。");
		return;
	}
	if ( (flag1 == "") || (flag2 == "") )
	{
		alert("指定範囲が学期外です。");
		return;
	}


	document.forms[0].cmd.value = cmdb;
	document.forms[0].submit();
	return false;
}

//印刷・プレビューボタン
function newwin(SERVLET_URL){

	if (document.forms[0].DATE1.value == "")
	{
		alert("日付が不正です。");
		document.forms[0].DATE1.focus();
		return;
	}
	if (document.forms[0].DATE2.value == "")
	{
		alert("日付が不正です。");
		document.forms[0].DATE2.focus();
		return;
	}

	var val = document.forms[0].SEME_DATE.value;
	var tmp = val.split(',');
	var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
	var tmp3 = document.forms[0].DATE2.value.split('/'); //印刷範囲終了日付
	var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
	var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
	var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
	var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
	var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
	var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）

	if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) > new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		alert("日付の大小が不正です。");
		return;
	}
	var flag1 = 0;
	var flag2 = 0;
	var val_seme = "";
	val_seme = document.forms[0].SEMESTER.value;
	if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
	{
		if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
		{
			flag1 = 1; //1学期
		}
	}
	if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
	{
		if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
		{
			flag1 = 2; //2学期
		}
	}
	if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
	{
		if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
		{
			flag1 = 3; //3学期
		}
	}
	if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
		{
			flag2 = 1; //1学期
		}
	}
	if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
		{
			flag2 = 2; //2学期
		}
	}
	if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
		{
			flag2 = 3; //3学期
		}
	}
	if (flag1 != flag2)
	{
		alert("指定範囲が学期をまたがっています。");
		return;
	}
	if ( (flag1 == "") || (flag2 == "") )
	{
		alert("指定範囲が学期外です。");
		return;
	}
	//学期が変わったら、再読込する
	if ( (val_seme != flag1) || (val_seme != flag2) )
	{
		document.forms[0].cmd.value = "knjf100";
		document.forms[0].submit();
		return false;
	}

	if (document.forms[0].CLASS_SELECTED.length == 0)
	{
		alert('出力対象クラスを指定してください');
		return;
	}


	for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++)
	{  
		document.forms[0].CLASS_NAME.options[i].selected = 0;
	}

	for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++)
	{  
		document.forms[0].CLASS_SELECTED.options[i].selected = 1;
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
		attribute = document.forms[0].CLASS_NAME;
        ClearList(attribute,attribute);
		attribute = document.forms[0].CLASS_SELECTED;
        ClearList(attribute,attribute);
}

function move1(side)
{   
	var temp1 = new Array();
	var temp2 = new Array();
	var tempa = new Array();
	var tempb = new Array();
	var current1 = 0;
	var current2 = 0;
	var y=0;
	var attribute;
	
	//assign what select attribute treat as attribute1 and attribute2
	if (side == "left")
	{  
		attribute1 = document.forms[0].CLASS_NAME;
		attribute2 = document.forms[0].CLASS_SELECTED;
	}
	else
	{  
		attribute1 = document.forms[0].CLASS_SELECTED;
		attribute2 = document.forms[0].CLASS_NAME;  
	}

	
	//fill an array with old values
	for (var i = 0; i < attribute2.length; i++)
	{  
		y=current1++
		temp1[y] = attribute2.options[i].value;
		tempa[y] = attribute2.options[i].text;
	}

	//assign new values to arrays
	for (var i = 0; i < attribute1.length; i++)
	{   
		if ( attribute1.options[i].selected )
		{  
			y=current1++
			temp1[y] = attribute1.options[i].value;
			tempa[y] = attribute1.options[i].text; 
		}
		else
		{  
			y=current2++
			temp2[y] = attribute1.options[i].value; 
			tempb[y] = attribute1.options[i].text;
		}
	}

	//generating new options 
	for (var i = 0; i < temp1.length; i++)
	{  
		attribute2.options[i] = new Option();
		attribute2.options[i].value = temp1[i];
		attribute2.options[i].text =  tempa[i];
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
	var current5 = 0;
	var z=0;
	
	//assign what select attribute treat as attribute5 and attribute6
	if (sides == "left")
	{  
		attribute5 = document.forms[0].CLASS_NAME;
		attribute6 = document.forms[0].CLASS_SELECTED;
	}
	else
	{  
		attribute5 = document.forms[0].CLASS_SELECTED;
		attribute6 = document.forms[0].CLASS_NAME;  
	}

	
	//fill an array with old values
	for (var i = 0; i < attribute6.length; i++)
	{  
		z=current5++
		temp5[z] = attribute6.options[i].value;
		tempc[z] = attribute6.options[i].text;
	}

	//assign new values to arrays
	for (var i = 0; i < attribute5.length; i++)
	{   
		z=current5++
		temp5[z] = attribute5.options[i].value;
		tempc[z] = attribute5.options[i].text; 
	}

	//generating new options 
	for (var i = 0; i < temp5.length; i++)
	{  
		attribute6.options[i] = new Option();
		attribute6.options[i].value = temp5[i];
		attribute6.options[i].text =  tempc[i];
	}

	//generating new options
	ClearList(attribute5,attribute5);

}

