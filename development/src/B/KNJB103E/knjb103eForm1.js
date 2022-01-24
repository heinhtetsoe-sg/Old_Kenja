function btn_submit(cmd) {
    if (cmd == "csv") {
        //共通チェック：印刷およびＣＳＶ出力
        if (!checkPrintCsv()) {
            return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
// 印刷
function newwin(SERVLET_URL) {
    //共通チェック：印刷およびＣＳＶ出力
    if (!checkPrintCsv()) {
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
// 共通チェック：印刷およびＣＳＶ出力
function checkPrintCsv() {
    var tergetDate = document.forms[0].TERGET_DATE.value;
    var ctrlYear = document.forms[0].CTRL_YEAR.value;
    var nextYear = parseInt(ctrlYear) + 1;
    var sDate = ctrlYear + "/04/01";
    var eDate = nextYear + "/03/31";
    if (tergetDate == "") {
        alert("対象日付を入力して下さい。");
        return false;
    }
    if (tergetDate < sDate || tergetDate > eDate) {
        alert("対象日付は年度範囲内を入力して下さい。");
        return false;
    }
    return true;
}
