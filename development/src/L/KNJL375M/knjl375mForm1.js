function btn_submit() {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].PASS_SCORE.value == '') {
        alert('{rval MSG301}');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
