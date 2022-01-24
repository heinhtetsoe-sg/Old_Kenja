//サブミット
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL, cmd){
    if (document.forms[0].COMMITTEE_FLG.value == '') {
        alert("委員会区分が選択されていません。");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;
    var oldcmd = document.forms[0].cmd.value;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJJ";
    document.forms[0].target = "_blank";
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = oldcmd.value;
}
