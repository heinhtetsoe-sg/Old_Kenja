function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    if (document.forms[0].TESTID.value == "") {
        alert("学力テストを指定して下さい");
        return;
    }
    action = document.forms[0].action;
    target = document.forms[0].target;

    url = location.hostname;
    //document.forms[0].action = "http://" + url + "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJH";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
