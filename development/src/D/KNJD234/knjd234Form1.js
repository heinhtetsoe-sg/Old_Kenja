function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){


    if (document.forms[0].ASSESS1.value == ""){
        alert('評定平均（成績優良者）を指定してください。');
        return false;
    }
    if (document.forms[0].ASSESS2.value == ""){
        alert('評定平均（成績不振者）を指定してください。');
        return false;
    }
    if (document.forms[0].DATE.value == "")
    {
        alert('日付を指定してください。');
        return false;
    }
    if (document.forms[0].DATE.value < document.forms[0].SDATE.value || document.forms[0].DATE.value > document.forms[0].EDATE.value) {
        alert("日付が学期範囲外です。");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
       document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
