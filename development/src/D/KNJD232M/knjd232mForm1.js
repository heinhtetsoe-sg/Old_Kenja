function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    //日付入力チェック
    var taisyou_date = document.forms[0].TAISYOU_DATE.value;
    var chk_sdate = document.forms[0].CHK_SDATE.value;
    var chk_edate = document.forms[0].CHK_EDATE.value;

    if (taisyou_date == "") {
        alert("日付が不正です。");
        document.forms[0].TAISYOU_DATE.focus();
        return false;
    }

    //日付の年度内チェック
    if((taisyou_date < chk_sdate) || (taisyou_date > chk_edate)){
        alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
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

