function btn_submit() {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {

    var sdate = document.forms[0].SDATE;
    var edate = document.forms[0].EDATE;

    //日付入力チェック
    if (sdate.value == "") {
        alert("日付が不正です。");
        sdate.focus();
        return false;
    }
    if (edate.value == "") {
        alert("日付が不正です。");
        edate.focus();
        return false;
    }

    //日付の大小比較
    if (sdate.value > edate.value) {
        alert("日付が不正です。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
