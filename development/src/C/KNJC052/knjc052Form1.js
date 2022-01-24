function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function opener_submit(SERVLET_URL){

    var obj1 = document.forms[0].DATE;
    var obj2 = document.forms[0].DATE2;
    var tmp2 = obj1.value.split('/'); //印刷範囲開始日付
    var tmp3 = obj2.value.split('/'); //印刷範囲終了日付
    var tmp4 = document.forms[0].STARTDAY.value.split('/'); //学期開始日付
    var tmp5 = document.forms[0].ENDDAY.value.split('/'); //学期終了日付
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
    if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) > new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2]))) {
        alert("指定範囲が正しく有りません。");
        return;
    }

    var flag1 = 0;
    var flag2 = 0;
    if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2]))) {
        if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2]))) {
            flag1 = 1;
        }
    }
    if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2]))) {
        if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2]))) {
            flag2 = 1;
        }
    }
    if (flag1 != 1) {
        alert("指定範囲が学期範囲外です。");
        obj1.focus();
        return;
    }
    if (flag2 != 1) {
        alert("指定範囲が学期範囲外です。");
        obj2.focus();
        return;
    }

        action = document.forms[0].action;
        target = document.forms[0].target;

        document.forms[0].action = SERVLET_URL +"/KNJC";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
//        document.forms[0].SEMESTER.value = val;

        return false;
}
