function btn_submit(cmd)
{
	document.forms[0].cmd.value = cmd;
	document.forms[0].submit();
	return false;
}

function btn_submit2(cmd)
{

	var val = document.forms[0].SEME_DATE.value;
	var tmp = val.split(',');
	var tmps = document.forms[0].DATE2.value.split('/'); //学籍処理日
	var tmp1f = tmp[0].split('/'); //学期開始日付（1学期）
	var tmp1t = tmp[1].split('/'); //学期終了日付（1学期）
	var tmp2f = tmp[2].split('/'); //学期開始日付（2学期）
	var tmp2t = tmp[3].split('/'); //学期終了日付（2学期）
	var tmp3f = tmp[4].split('/'); //学期開始日付（3学期）
	var tmp3t = tmp[5].split('/'); //学期終了日付（3学期）

	//学期フラグ初期化
	var flag = 0;

	//学期コード取得
	var val_seme = "";
	val_seme = document.forms[0].SEMESTER.value;
	document.forms[0].SEMESTER.value = "";

	//１学期の場合（１学期開始日 ＜＝ 学籍処理日 ＜＝ １学期終了日）
	if(new Date(eval(tmp1f[0]),eval(tmp1f[1])-1,eval(tmp1f[2])) <= new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])))
	{
		if(new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])) <= new Date(eval(tmp1t[0]),eval(tmp1t[1])-1,eval(tmp1t[2])))
		{
			flag = 1;		//1学期
		}
	}

	//２学期の場合（２学期開始日 ＜＝ 学籍処理日 ＜＝ ２学期終了日）
	if(new Date(eval(tmp2f[0]),eval(tmp2f[1])-1,eval(tmp2f[2])) <= new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])))
	{
		if(new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])) <= new Date(eval(tmp2t[0]),eval(tmp2t[1])-1,eval(tmp2t[2])))
		{
			flag = 2;		//2学期
		}
	}

	//３学期の場合（３学期開始日 ＜＝ 学籍処理日 ＜＝ ３学期終了日）
	if(new Date(eval(tmp3f[0]),eval(tmp3f[1])-1,eval(tmp3f[2])) <= new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])))
	{
		if(new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])) <= new Date(eval(tmp3t[0]),eval(tmp3t[1])-1,eval(tmp3t[2])))
		{
			flag = 3;		//3学期
		}
	}

	//学期期間に当てはまる場合、その学期を新しい学期に、当てはまらない場合リターン
	if ((flag >=1) && (flag<= 3))
	{
		document.forms[0].SEMESTER.value = flag;
	}
	else
	{
		alert("日付が学期範囲外です。");
		document.forms[0].DATE2.focus();
		return;
	}

	//処理日の値が正しく（ある学期の期間の範囲内にある）、かつ学期が変わった場合再読込する
	if (val_seme == document.forms[0].SEMESTER.value)
	{
		return;
	}
	else
	{
		document.forms[0].GRADE_HR_CLASS.length = 0;	//学期が変わると対象クラスをクリア
		document.forms[0].cmd.value = cmd;
		document.forms[0].submit();
		document.forms[0].DATE2.focus();
		return false;
	}

}

//*********************************************btn_submit終わり***********************************************************************

function newwin(SERVLET_URL){

	if (document.forms[0].category_selected.length == 0)
	{
		alert('{rval MSG916}');
		return;
	}
/*
	var obj1 = document.forms[0].DATE2;
	if(obj1.value == '')
	{
		alert("日付が不正です。");
		obj1.focus();
		return false;
	}
*/
/*****NO001
	var obj2 = document.forms[0].DATE;
	if (obj2.value == '')
	{
		alert("日付が不正です。");
		obj2.focus();
		return false;
	}
*****/
/*
	var val = document.forms[0].SEME_DATE.value;
	var tmp = val.split(',');
	var tmps = document.forms[0].DATE2.value.split('/'); //学籍処理日
	var tmp1f = tmp[0].split('/'); //学期開始日付（1学期）
	var tmp1t = tmp[1].split('/'); //学期終了日付（1学期）
	var tmp2f = tmp[2].split('/'); //学期開始日付（2学期）
	var tmp2t = tmp[3].split('/'); //学期終了日付（2学期）
	var tmp3f = tmp[4].split('/'); //学期開始日付（3学期）
	var tmp3t = tmp[5].split('/'); //学期終了日付（3学期）

	//学期フラグ初期化
	var flag = 0;

	//学期コード取得
	var val_seme = "";
	val_seme = document.forms[0].SEMESTER.value;
	document.forms[0].SEMESTER.value = "";

	//１学期の場合（１学期開始日 ＜＝ 学籍処理日 ＜＝ １学期終了日）
	if(new Date(eval(tmp1f[0]),eval(tmp1f[1])-1,eval(tmp1f[2])) <= new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])))
	{
		if(new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])) <= new Date(eval(tmp1t[0]),eval(tmp1t[1])-1,eval(tmp1t[2])))
		{
			flag = 1;		//1学期
		}
	}

	//２学期の場合（２学期開始日 ＜＝ 学籍処理日 ＜＝ ２学期終了日）
	if(new Date(eval(tmp2f[0]),eval(tmp2f[1])-1,eval(tmp2f[2])) <= new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])))
	{
		if(new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])) <= new Date(eval(tmp2t[0]),eval(tmp2t[1])-1,eval(tmp2t[2])))
		{
			flag = 2;		//2学期
		}
	}

	//３学期の場合（３学期開始日 ＜＝ 学籍処理日 ＜＝ ３学期終了日）
	if(new Date(eval(tmp3f[0]),eval(tmp3f[1])-1,eval(tmp3f[2])) <= new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])))
	{
		if(new Date(eval(tmps[0]),eval(tmps[1])-1,eval(tmps[2])) <= new Date(eval(tmp3t[0]),eval(tmp3t[1])-1,eval(tmp3t[2])))
		{
			flag = 3;		//3学期
//			flag = 1;		//1学期
		}
	}

	//学期期間に当てはまる場合、その学期を新しい学期に、当てはまらない場合リターン
	if (1 <= flag <= 3)
	{
		document.forms[0].SEMESTER.value = flag;
	}
	else
	{
		alert("学籍処理日が学期の範囲外です。");
		return;
	}

	//処理日の値が正しく（ある学期の期間の範囲内にある）、かつ学期が変わった場合再読込する
	cmd = "knjg030"; //修正
	if (val_seme != document.forms[0].SEMESTER.value)
	{
		document.forms[0].GRADE_HR_CLASS.length = 0;	//学期が変わると対象クラスをクリア
		document.forms[0].cmd.value = cmd;
		document.forms[0].submit();
		document.forms[0].DATE2.focus();
		return false;
	}

	if (document.forms[0].GRADE_HR_CLASS.value == "")
	{
		alert("対象クラスが不正です。");
		document.forms[0].GRADE_HR_CLASS.focus();
		return;
	}
*/

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

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJG";
	document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//***************************************************newwin終わり**********************************************************************
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
		tempaa[y] = String(attribute2.options[i].text).substr(temp1[y].length+1,3)+","+y; // 2004/01/23 //2004/11/17:sort修正(8桁対応)m-yama
	}

	//assign new values to arrays
	for (var i = 0; i < attribute1.length; i++)
	{   
		if ( attribute1.options[i].selected )
		{  
			y=current1++
			temp1[y] = attribute1.options[i].value;
			tempa[y] = attribute1.options[i].text; 
			tempaa[y] = String(attribute1.options[i].text).substr(temp1[y].length+1,3)+","+y; // 2004/01/23 //2004/11/17:sort修正(8桁対応)m-yama
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
		tempaa[z] = String(attribute6.options[i].text).substr(temp5[z].length+1,3)+","+z; // 2004/01/23 //2004/11/17:sort修正(8桁対応)m-yama
	}

	//assign new values to arrays
	for (var i = 0; i < attribute5.length; i++)
	{   
		z=current5++
		temp5[z] = attribute5.options[i].value;
		tempc[z] = attribute5.options[i].text; 
		tempaa[z] = String(attribute5.options[i].text).substr(temp5[z].length+1,3)+","+z; // 2004/01/23 //2004/11/17:sort修正(8桁対応)m-yama
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
