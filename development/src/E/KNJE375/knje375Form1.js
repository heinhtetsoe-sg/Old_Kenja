function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL) {

    if (document.forms[0].MIN_YEAR.value > document.forms[0].MAX_YEAR.value) {
        alert('{rval MSG916}');
        return false;
    }

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
