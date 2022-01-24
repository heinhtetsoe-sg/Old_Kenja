function btn_submit(cmd) {
    if (cmd == 'reset'){
        result = confirm('{rval MSG106}');
        if (result == false)
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
