function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( 入試制度 )');
        return;
    }
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 入試区分 )');
        return;
    }

    if (document.forms[0].SCHOOL_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    for (var i = 0; i < document.forms[0].SCHOOL_NAME.length; i++) {
        document.forms[0].SCHOOL_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].SCHOOL_SELECTED.length; i++) {
        document.forms[0].SCHOOL_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
