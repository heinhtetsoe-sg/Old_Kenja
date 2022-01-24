function btn_submit(cmd){

    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'tuutihyou'){  //通知票所見参照
        loadwindow('knja120bindex.php?cmd=tuutihyou',0,0,750,450);
        return true;

    } else if (cmd == 'tyousasyo'){  //調査書(進学用)出欠の記録参照
        loadwindow('knja120bindex.php?cmd=tyousasyo',0,0,400,200);
        return true;
    } else if (cmd == 'shokenlist1'){  //既入力内容を参照（特別活動所見）
        loadwindow('knja120bindex.php?cmd=shokenlist1',0,0,700,350);
        return true;
    } else if (cmd == 'shokenlist2'){  //既入力内容を参照（総合所見）
        loadwindow('knja120bindex.php?cmd=shokenlist2',0,0,700,350);
        return true;
    } else if (cmd == 'shokenlist3'){  //既入力内容を参照（出欠の記録備考）
        loadwindow('knja120bindex.php?cmd=shokenlist3',0,0,700,350);
        return true;
    }
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    if (cmd == 'execute') {
        document.forms[0].encoding = "multipart/form-data";
    }

    updBtnNotDisp();

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){
    var yearsemester = top.left_frame.document.forms[0].EXP_YEAR.value.split('-');
    top.right_frame.document.forms[0].PRINT_YEAR.value = yearsemester[0];
    top.right_frame.document.forms[0].PRINT_SEMESTER.value = yearsemester[1];
    var gradehrclass = top.left_frame.document.forms[0].GRADE.value.split('-');
    top.right_frame.document.forms[0].GRADE_HR_CLASS.value = gradehrclass[0] + gradehrclass[1];
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function updBtnNotDisp() {
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
}
