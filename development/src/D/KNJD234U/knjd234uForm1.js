function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
    /******************/
    /* 日付のチェック */
    /******************/
    if (document.forms[0].EDATE.value == '') {
        alert("日付が不正です。");
        document.forms[0].EDATE.focus();
        return false;
    }

    var day   = document.forms[0].EDATE.value;      //印刷範囲日付
    var sdate = document.forms[0].SEME_SDATE.value; //学期開始日付
    var edate = document.forms[0].SEME_EDATE.value; //学期終了日付

    if (sdate > day || edate < day) {
        alert("日付が学期の範囲外です");
        return;
    }

    document.forms[0].DATE.value = document.forms[0].EDATE.value;
    document.forms[0].cmd.value = cmd;

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

