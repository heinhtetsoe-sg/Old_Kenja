function btn_submit(cmd) {
    //日付チェック
    if (document.forms[0].DATE.value == '') {
        alert('{rval MSG301}\n( 日付 )');
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    //日付チェック
    if (document.forms[0].DATE.value == '') {
        alert('{rval MSG301}\n( 日付 )');
        return true;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
