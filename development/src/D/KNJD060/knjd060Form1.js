function btn_submit(cmd) {
/*	if (cmd == 'gakki') {
		document.forms[0].GRADE_HR_CLASS.length = 0;	//学期が変わると対象クラスをクリア
	}*/
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){
    if (document.forms[0].CLASS_SELECTED.length == 0){
		alert('{rval MSG916}');
//		alert("指定範囲が正しく有りません。");
		return false;
	}

    var obj1 = document.forms[0].DATE;
    if (obj1.value == '')
	{
        alert("日付が不正です。");
        obj1.focus();
        return false;
	}
// NO004↓日付チェック処理
	var day   = document.forms[0].DATE.value.split('/'); //印刷範囲日付
	var sdate = document.forms[0].SEME_SDATE.value.split('/'); //学期開始日付
	var edate = document.forms[0].SEME_EDATE.value.split('/'); //学期終了日付

	var flag1 = document.forms[0].SEME_FLG.value;
	var val_seme = "";
	val_seme = document.forms[0].SEME_DATE.value;
//開始終了日付のチェック
	if((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(day[0]),eval(day[1])-1,eval(day[2])))
	   || ((new Date(eval(day[0]),eval(day[1])-1,eval(day[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
	{
		alert("日付が学期の範囲外です");
		return;
	}
//	if(val_seme != flag1)
//	{
//		document.forms[0].GRADE_HR_CLASS.length = 0;	//学期が変わると対象クラスをクリア
//   		document.forms[0].cmd.value = "semechg";
//   		document.forms[0].submit();
//		return;			//学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
//	}
// NO004↑

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

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
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
	
//NO008 ↓
function move1(side)
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

	for (var i = 0; i < attribute2.length; i++)
	{  
		y=current1++
		temp1[y] = attribute2.options[i].value;
		tempa[y] = attribute2.options[i].text;
		tempaa[y] = attribute2.options[i].value+","+y;
	}

	for (var i = 0; i < attribute1.length; i++)
	{   
		if ( attribute1.options[i].selected )
		{  
			y=current1++
			temp1[y] = attribute1.options[i].value;
			tempa[y] = attribute1.options[i].text; 
			tempaa[y] = attribute1.options[i].value+","+y;
		}
		else
		{  
			y=current2++
			temp2[y] = attribute1.options[i].value; 
			tempb[y] = attribute1.options[i].text;
		}
	}

    tempaa.sort();

	for (var i = 0; i < temp1.length; i++)
	{  
        var val = tempaa[i];
        var tmp = val.split(',');

		attribute2.options[i] = new Option();
		attribute2.options[i].value = temp1[tmp[1]];
		attribute2.options[i].text =  tempa[tmp[1]];
	}

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
	var tempaa = new Array();
	var current5 = 0;
	var z=0;
	
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

	for (var i = 0; i < attribute6.length; i++)
	{  
		z=current5++
		temp5[z] = attribute6.options[i].value;
		tempc[z] = attribute6.options[i].text;
		tempaa[z] = attribute6.options[i].value+","+z;
	}

	for (var i = 0; i < attribute5.length; i++)
	{   
		z=current5++
		temp5[z] = attribute5.options[i].value;
		tempc[z] = attribute5.options[i].text; 
		tempaa[z] = attribute5.options[i].value+","+z;
	}

    tempaa.sort();

	for (var i = 0; i < temp5.length; i++)
	{  
        var val = tempaa[i];
        var tmp = val.split(',');

		attribute6.options[i] = new Option();
		attribute6.options[i].value = temp5[tmp[1]];
		attribute6.options[i].text =  tempc[tmp[1]];
	}

	ClearList(attribute5,attribute5);

}
//NO008 ↑
