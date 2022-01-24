function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function chkAll() {
    for (var cnt = 1;cnt <= 9;cnt++) {
        if (document.forms[0].CHKALL.checked == true) {
            document.forms[0]["CHKBOX" + cnt].checked = true;
        } else {
            document.forms[0]["CHKBOX" + cnt].checked = false;
        }
    }
}