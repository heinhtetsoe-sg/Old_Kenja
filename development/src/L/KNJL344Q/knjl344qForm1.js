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
    if (document.forms[0].TESTDIV.value == '9') {
        if (   document.forms[0].TAISYOU1.checked == false
            && document.forms[0].TAISYOU3.checked == false
            && document.forms[0].TAISYOU4.checked == false) {
            alert('{rval MSG310}\n( 帳票種類 )');
            return;
        }
    } else if (document.forms[0].TESTDIV.value != '9') {
        if (   document.forms[0].TAISYOU1.checked == false
            && document.forms[0].TAISYOU2.checked == false
            && document.forms[0].TAISYOU4.checked == false) {
            alert('{rval MSG310}\n( 帳票種類 )');
            return;
        }
    }

    if (document.forms[0].OUTPUT2.checked && document.forms[0].EXAMNO.value == '') {
        alert('{rval MSG310}\n( 受験番号 )');
        return;
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
