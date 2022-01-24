function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( 受験校種 )');
        return;
    }
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 試験区分 )');
        return;
    }

    if (document.forms[0].RECEPTNO_FROM.value > 0 && document.forms[0].RECEPTNO_TO.value > 0) {
        select1 = document.forms[0].RECEPTNO_FROM;
        select2 = document.forms[0].RECEPTNO_TO;
        wrkindex = 0;

        if ( select1.value > select2.value ) {
            wrkindex = select1.value;
            select1.value = select2.value;
            select2.value = wrkindex;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
