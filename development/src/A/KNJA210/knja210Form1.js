function newwin(SERVLET_URL){
    var obj2 = document.forms[0].DATE;
    if (obj2.value == '')
    {
        alert("日付が不正です。");
        obj2.focus();
        return false;
    }
    if (document.forms[0].GRADE_HR_CLASS.value == "") {
        alert("対象クラスが不正です。");
        document.forms[0].GRADE_HR_CLASS.focus();
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//      url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function btn_submit(cmd) {
    var val = document.forms[0].DATEFT.value;
    var tmp = val.split(',');
    var tmp2 = document.forms[0].DATE.value.split('/'); //印刷範囲開始日付
    var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
    var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
    var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
    var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
    var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
    var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）

    var val_seme;
    val_seme = document.forms[0].SEMESTER.value;
    document.forms[0].SEMESTER.value = "";
    if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2]))) {
        if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2]))) {
            document.forms[0].SEMESTER.value = 1; //1学期
        }
    }
    if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2]))) {
        if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2]))) {
            document.forms[0].SEMESTER.value = 2; //2学期
        }
    }
    if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2]))) {
        if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2]))) {
            document.forms[0].SEMESTER.value = 3; //3学期
        }
    }
    if (val_seme == document.forms[0].SEMESTER.value) {
        return;
    } else {
        document.forms[0].GRADE_HR_CLASS.length = 0;    //学期が変わると対象クラスをクリア
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        document.forms[0].DATE.focus();
        return false;
    }
}
function toukei_submit() {
    var path = parent.left_frame.location.pathname;
    window.open(path+'?cmd=toukei2&SEL_SEMI='+document.forms[0].YEAR.value+","+document.forms[0].SEMESTER.value,'left_frame');
}
