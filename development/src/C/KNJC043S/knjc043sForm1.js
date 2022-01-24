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
    document.forms[0].cmd.value = "semechg";
    document.forms[0].submit();
    return;         //学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
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
    //年度範囲チェック
    if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) >= new Date(eval(chk_sdate[0]),eval(chk_sdate[1])-1,eval(chk_sdate[2])) && 
       new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(chk_edate[0]),eval(chk_edate[1])-1,eval(chk_edate[2])))
    {
    } else {
        alert("日付が年度範囲外です。");
        return;
    }
    if (document.forms[0].GRADE_HR_CLASS.value == "") {
        alert("対象クラスが不正です。");
        document.forms[0].GRADE_HR_CLASS.focus();
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function Check(obj)    //add  05/06/07  yamauchi
{
    if(document.forms[0].RADIO1[1].checked == true)
    {
        flag1 = false;
    }
    else
    {
        flag1 = true;
    }
    document.forms[0].CHECK1.disabled = flag1;

    if(document.forms[0].OUTPUT2.checked == true)
    {
        flag2 = false;
    }
    else
    {
        flag2 = true;
    }
    document.forms[0].CHECK2.disabled = flag2;

}


