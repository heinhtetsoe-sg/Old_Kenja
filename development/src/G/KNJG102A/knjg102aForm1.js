function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){
    //入力チェック
    if (!document.forms[0].KAIKIN_KESSEKI.value) {
        alert('{rval MSG916}');
        document.forms[0].KAIKIN_KESSEKI.focus();
        return false;
    }

    if (!document.forms[0].SEIKIN_KESSEKI.value) {
        alert('{rval MSG916}');
        document.forms[0].SEIKIN_KESSEKI.focus();
        return false;
    }

    //日付チェック
    if (document.forms[0].DATE.value == "") {
        alert('日付を指定してください。');
        return false;
    }
    if (document.forms[0].DATE.value < document.forms[0].SDATE.value ||
        document.forms[0].DATE.value > document.forms[0].EDATE.value) {
        alert("日付が学期範囲外です。");
        return;
    }

    //日付チェック
    if (document.forms[0].KISAI_DATE.value == "") {
        alert('記載日付を指定してください。');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJG";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

