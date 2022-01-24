function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'subform1') {
        loadwindow('knjd131index.php?cmd=subform1',0,0,700,300);
        return true;
    } else if (cmd == 'subform2') {
        loadwindow('knjd131index.php?cmd=subform2',0,0,700,300);
        return true;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    var gradehrclass = top.main_frame.left_frame.document.forms[0].GRADE.value.split('-');
    top.main_frame.right_frame.document.forms[0].GRADE_HR_CLASS.value = gradehrclass[0] + gradehrclass[1];
    action = document.forms[0].action;
    target = document.forms[0].target;

    if (document.forms[0].PATARN_DIV[0].checked) {
        document.forms[0].FORMNAME.value = "KNJD177A";
    } else if (document.forms[0].PATARN_DIV[1].checked) {
        document.forms[0].FORMNAME.value = "KNJD177B";
    } else if (document.forms[0].PATARN_DIV[2].checked) {
        document.forms[0].FORMNAME.value = "KNJD177C";
    } else if (document.forms[0].PATARN_DIV[3].checked) {
        document.forms[0].FORMNAME.value = "KNJD177D";
    } else {
        document.forms[0].FORMNAME.value = "KNJD177E";
    }

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//一括更新画面へ
function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG308}');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    link = link + "&SCHREGNOS=" + top.main_frame.left_frame.document.forms[0].SCHREGNO.value;
    link = link + "&GRADE_HRCLASS=" + top.main_frame.left_frame.document.forms[0].GRADE.value;

    parent.location.href=link;
}

