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

    if ((document.forms[0].TAISYOU3.checked == true)
        && !document.forms[0].F_EXAMNO.value == ''
        && !document.forms[0].T_EXAMNO.value == '') {
        var fNo = Number(document.forms[0].F_EXAMNO.value);
        var tNo = Number(document.forms[0].T_EXAMNO.value);
        if (fNo > tNo) {
            alert('{rval MSG901}\n( 受験番号指定 )');
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
