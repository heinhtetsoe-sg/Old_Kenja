function btn_submit() {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    //日付入力チェック
    if (document.forms[0].SDATE.value == "") {
        alert("日付が不正です。");
        document.forms[0].SDATE.focus();
        return false;
    }
    if (document.forms[0].EDATE.value == "") {
        alert("日付が不正です。");
        document.forms[0].EDATE.focus();
        return false;
    }

    //日付の年度内チェック
    var sdate = document.forms[0].SDATE.value;
    var edate = document.forms[0].EDATE.value;
    var chk_ldate = document.forms[0].CHK_LDATE.value;
    var chk_sdate = document.forms[0].CHK_SDATE.value;
    var chk_edate = document.forms[0].CHK_EDATE.value;
    chk_sdate = chk_sdate.replace(/-/g,"/");
    chk_edate = chk_edate.replace(/-/g,"/");

    if ((sdate < chk_sdate) || (sdate > chk_edate) || (edate < chk_sdate) || (edate > chk_edate)){
        alert("指定範囲が不正です。\n（" + chk_sdate + "～" + chk_edate + "） ");
        return false;
    }
    if (sdate > edate) {
        alert("日付の大小が不正です。");
        return false;
    }

    if (!document.forms[0].DI_CD1.checked && !document.forms[0].DI_CD2.checked && !document.forms[0].DI_CD3.checked && !document.forms[0].DI_CD14.checked) {
        alert("出力対象を一つ以上選択してください。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
