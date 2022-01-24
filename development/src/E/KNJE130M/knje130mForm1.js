function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
    var hyoutei = document.forms[0].HYOUTEI;
    var kesseki = document.forms[0].KESSEKI;

    if (hyoutei.value == "") {
        alert('{rval MSG301}');
        return false;
    }

    if (kesseki.value == "") {
        alert('{rval MSG301}');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

