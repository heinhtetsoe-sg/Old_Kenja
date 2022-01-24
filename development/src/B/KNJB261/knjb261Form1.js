function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/************************************** 帳票 *******************************************/
function newwin(SERVLET_URL) {
    if (document.forms[0].DATE1.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE1.focus();
        return;
    }

    if (document.forms[0].DATE2.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE2.focus();
        return;
    }

    var chk_sdate = document.forms[0].CHK_SDATE.value; //年度開始日付
    var chk_edate = document.forms[0].CHK_EDATE.value; //年度終了日付

    var date1 = document.forms[0].DATE1.value; //印刷範囲開始日付
    var date2 = document.forms[0].DATE2.value; //印刷範囲終了日付

    if (date1 > date2) {
        alert("日付の大小が不正です。");
        return;
    }

    if((date1 < chk_sdate) || (date2 > chk_edate)){
        alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
        return;
    }

    var attend = "";
    var sep = "";
    var j;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    document.forms[0].action = action;
    document.forms[0].target = target;

    return false;
}

