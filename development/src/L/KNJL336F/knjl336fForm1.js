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
    if (document.forms[0].APPLICANTDIV.value == '2' && document.forms[0].TESTDIV.value != "ALL" && document.forms[0].TESTDIV0.value == '') {
        alert('{rval MSG310}\n( 入試回数 )');
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

//disabled
function OptionUse(obj) {
    if(document.forms[0].OUTPUT[0].checked == true) {
        document.forms[0].JUDGE[0].disabled = false;
        document.forms[0].JUDGE[1].disabled = false;
        document.forms[0].JUDGE[2].disabled = false;
        document.forms[0].JUDGE[3].disabled = false;
    } else {
        document.forms[0].JUDGE[0].disabled = true;
        document.forms[0].JUDGE[1].disabled = true;
        document.forms[0].JUDGE[2].disabled = true;
        document.forms[0].JUDGE[3].disabled = true;
    }
}
