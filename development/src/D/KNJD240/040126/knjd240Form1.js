function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(){

    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = "http://" + url +"/servlet/KNJD";
    document.forms[0].target = "aaa";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
