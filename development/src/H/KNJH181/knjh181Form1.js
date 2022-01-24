function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL, cmd){

    if (document.forms[0].ROSEN.value == "") {
        alert('路線を選択して下さい。');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;
    oldcmd = document.forms[0].cmd;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJH";
    document.forms[0].target = "_blank";
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = oldcmd;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
