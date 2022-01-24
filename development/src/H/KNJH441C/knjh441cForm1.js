function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";

    document.forms[0].cmd.value = cmd;

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

