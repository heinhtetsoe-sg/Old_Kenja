function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {

    var obj1 = document.forms[0].SDATE;
    var obj2 = document.forms[0].EDATE;
    var tmp2 = obj1.value.split('/'); //印刷範囲開始日付
    var tmp3 = obj2.value.split('/'); //印刷範囲終了日付
    var tmp4 = document.forms[0].STARTDAY.value.split('/'); //学期開始日付
    var tmp5 = document.forms[0].ENDDAY.value.split('/');   //学期終了日付
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
        alert("指定範囲が年度範囲外です。");
        obj1.focus();
        return;
    }
    if (flag2 != 1) {
        alert("指定範囲が年度範囲外です。");
        obj2.focus();
        return;
    }

    var checkSdate = tmp2[0] + tmp2[1] + tmp2[2];
    var checkEdate = tmp3[0] + tmp3[1] + tmp3[2];
    document.forms[0].SSEMESTER.value = "";
    document.forms[0].ESEMESTER.value = "";
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var obj_check = document.forms[0].elements[i];
        re = new RegExp("^CHECK_SEM");
        if (obj_check.name.match(re)) {
            var check = obj_check.value.split(':');
            if (check[1] <= checkSdate && check[2] >= checkSdate) {
                document.forms[0].SSEMESTER.value = check[0];
            }
            if (check[1] <= checkEdate && check[2] >= checkEdate) {
                document.forms[0].ESEMESTER.value = check[0];
            }
        }
    }
    if (document.forms[0].SSEMESTER.value == '' || document.forms[0].ESEMESTER.value == '') {
        alert('日付が学期範囲外です。');
        return;
    }


    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;

}
