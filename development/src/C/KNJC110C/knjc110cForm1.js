function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var obj1 = document.forms[0].SDATE;
    if (obj1.value == '') {
        alert("日付が不正です。");
        obj1.focus();
        return false;
    }
    var obj2 = document.forms[0].EDATE;
    if (obj2.value == '') {
        alert("日付が不正です。");
        obj2.focus();
        return false;
    }

    //日付の年度内チェック
    var sdate = document.forms[0].SDATE.value;
    var edate = document.forms[0].EDATE.value;
    var chk_sdate = document.forms[0].CHK_SDATE.value;
    var chk_edate = document.forms[0].CHK_EDATE.value;

    if (sdate > edate) {
        alert("日付の大小が不正です。");
        return false;
    }

    if((sdate < chk_sdate) || (edate > chk_edate)){
        alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function calendarAbled(outputDiv) {
    if (outputDiv == "1") {
        document.forms[0].SDATE.disabled      = false;
        document.forms[0].SDATE_btn.disabled  = false;
        document.forms[0].EDATE.disabled      = false;
        document.forms[0].EDATE_btn.disabled  = false;
        document.forms[0].SMONTH.disabled     = true;
        document.forms[0].EMONTH.disabled     = true;
    } else if (outputDiv == "2") {
        document.forms[0].SDATE.disabled      = true;
        document.forms[0].SDATE_btn.disabled  = true;
        document.forms[0].EDATE.disabled      = true;
        document.forms[0].EDATE_btn.disabled  = true;
        document.forms[0].SMONTH.disabled     = false;
        document.forms[0].EMONTH.disabled     = false;
    } else {
        document.forms[0].SDATE.disabled      = true;
        document.forms[0].SDATE_btn.disabled  = true;
        document.forms[0].EDATE.disabled      = true;
        document.forms[0].EDATE_btn.disabled  = true;
        document.forms[0].SMONTH.disabled     = true;
        document.forms[0].EMONTH.disabled     = true;
    }
}
