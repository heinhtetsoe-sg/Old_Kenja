function btn_submit(cmd) {
    if (cmd == 'csv' || cmd == 'exec') {
        if (document.forms[0].TESTDIV0.value == '') {
            alert('{rval MSG301}' + '\n ( 学科 )');
            return false;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '\n ( 試験区分 )');
            return false;
        }
    }
    
    if (cmd == 'exec' && !confirm("処理を開始します。よろしいでしょうか？")) {
        return;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

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
