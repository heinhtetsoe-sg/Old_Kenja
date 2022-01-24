function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {

    var ism = parseInt(document.forms[0].SMONTH.value);
    var iem = parseInt(document.forms[0].EMONTH.value);
    if (ism + (ism <= 3 ? 12 : 0) > iem + (iem <= 3 ? 12 : 0)) {
        alert("開始月と終了月の範囲の指定が不正です。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;
    oldcmd = document.forms[0].cmd;
    document.forms[0].cmd.value = cmd;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = oldcmd;
}
