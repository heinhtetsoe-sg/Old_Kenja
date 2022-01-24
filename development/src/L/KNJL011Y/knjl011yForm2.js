function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//理由コンボをdisabledにする
function disReason(testdiv) {
    attendFlg = eval("document.forms[0][\"ATTEND_FLG" + testdiv + "\"]");
    attendReason = eval("document.forms[0][\"ATTEND_REASON" + testdiv + "\"]");
    if (attendFlg.checked) {
        attendReason.disabled = false;
    } else {
        attendReason.disabled = true;
    }
}
