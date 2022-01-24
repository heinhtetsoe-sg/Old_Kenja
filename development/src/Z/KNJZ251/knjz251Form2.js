function btn_submit(cmd) {
    if (cmd == 'delete') {
        result = confirm('{rval MSG103}');
        if (result == false)
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    if (cmd == 'reset') {
        result = confirm('{rval MSG106}');
        if (result == false)
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function noadd(addMessage) {
    alert('{rval MSG918}\n' + addMessage);
    return false;
}

function nodata(addMessage) {
    alert('{rval MSG303}\n' + addMessage);
    return false;
}

function newwin(SERVLET_URL){
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJZ";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

