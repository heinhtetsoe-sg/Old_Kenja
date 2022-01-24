function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].target = "_self";
    document.forms[0].action = "knjd627findex.php";
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    document.forms[0].target = "_blank";
    //document.forms[0].action = "http://" + location.hostname +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].submit();
}
