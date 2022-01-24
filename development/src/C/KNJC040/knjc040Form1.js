function btn_submit()
{
    var obj1 = document.forms[0].DATE1;
    var obj2 = document.forms[0].DATE2;
    if (obj1.value == '') {
        alert("日付が不正です。");
        obj1.focus();
        return false;
	}
    if (obj2.value == '') {
        alert("日付が不正です。");
        obj2.focus();
        return false;
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
/*	var flag1 = 0;
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
*/
//	if(val_seme != flag1)
//	{
    	document.forms[0].cmd.value = "semechg";
    	document.forms[0].submit();
		return;			//学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
//	}
}


function newwin(SERVLET_URL){

    var obj1 = document.forms[0].DATE1;
    var obj2 = document.forms[0].DATE2;
    if (obj1.value == '') {
        alert("日付が不正です。");
        obj1.focus();
        return false;
	}
    if (obj2.value == '') {
        alert("日付が不正です。");
        obj2.focus();
        return false;
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

	var chk_sdate = document.forms[0].CHK_SDATE.value.split('/'); //年度開始日
	var chk_edate = document.forms[0].CHK_EDATE.value.split('/'); //年度終了日

	if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) > new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
	{
		alert("日付の大小が不正です。");
		return;
	}
	//年度範囲チェック//2004/07/07 add by nakamoto
	if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) >= new Date(eval(chk_sdate[0]),eval(chk_sdate[1])-1,eval(chk_sdate[2])) && 
	   new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(chk_edate[0]),eval(chk_edate[1])-1,eval(chk_edate[2])))
	{
	} else {
		alert("日付が年度範囲外です。");
		return;
	}
/*	var flag1 = 0;
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

	if(val_seme != flag1)
	{
//		document.forms[0].GRADE_HR_CLASS.length = 0;	//学期が変わると対象クラスをクリア
    	document.forms[0].cmd.value = "semechg";
    	document.forms[0].submit();
		return;			//学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
	}
*/

//
	if (document.forms[0].GRADE_HR_CLASS.value == "") {
        alert("対象クラスが不正です。");
    	document.forms[0].GRADE_HR_CLASS.focus();
        return;
	}
//

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
