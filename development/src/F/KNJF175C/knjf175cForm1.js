function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var sdate = document.forms[0].SDATE;
    var edate = document.forms[0].EDATE;
    var chk_sdate = document.forms[0].YEAR.value + "/04/01";
    var chk_edate = parseInt(document.forms[0].YEAR.value) + 1 + "/03/31";
    var irekae = "";

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
        irekae = sdate.value;
        sdate.value = edate.value;
        edate.value = irekae;
    }

    //日付範囲チェック
    if (sdate.value < chk_sdate || sdate.value > chk_edate) {
        sdate.focus();
        alert("{rval MSG916}\n" + chk_sdate + "～" + chk_edate);
        return false;
    }
    if (edate.value < chk_sdate || edate.value > chk_edate) {
        edate.focus();
        alert("{rval MSG916}\n" + chk_sdate + "～" + chk_edate);
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + "/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
