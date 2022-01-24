function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    if (document.forms[0].PRE_TESTDIV.value == '') {
        alert('プレテスト区分を指定して下さい。');
        return false;
    }


//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
