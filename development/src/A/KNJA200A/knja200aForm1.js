function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){
    var obj2 = document.forms[0].DATE;
    if (obj2.value == '')
    {
        alert("日付が不正です。");
        obj2.focus();
        return false;
    }
    if (document.forms[0].DATA_CMB.value == "") {
        alert("対象データが不正です。");
        document.forms[0].DATA_CMB.focus();
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
