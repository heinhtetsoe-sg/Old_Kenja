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
    if (document.forms[0].ALLFLG2.checked == true) {
        if (document.forms[0].FINSCHOOLCD.value == '') {
            alert('{rval MSG301}\n( 出身学校コード )');
            return;
        }
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
function disAbled(obj) {
    if (obj.name == "ALLFLG") {
        if (obj.value == 1) {
            flgA = true;
        } else {
            flgA = false;
        }
        document.forms[0].FINSCHOOLCD.disabled = flgA;
        document.forms[0].JIZEN.disabled = !flgA;
    }
}
