function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){

    var obj1 = document.forms[0].DATE;
    if (obj1.value == '') {
        alert("日付が不正です。");
        obj1.focus();
        return false;
	}

	var val = document.forms[0].SEME_DATE.value;
	var tmp = val.split(',');
	var tmp2 = document.forms[0].DATE.value.split('/'); //処理日
	var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
	var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
	var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
	var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
	var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
	var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）

	var flag1 = 0;
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
	if ( flag1 == "" )
	{
		alert("指定範囲が学期外です。");
		return;
	} else {
    	document.forms[0].SEMESTER.value = flag1;
	}


    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
