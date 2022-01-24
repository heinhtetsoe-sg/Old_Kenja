function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//理由コンボをdisabledにする
function disReason(testdiv) {
    brotherFlg = eval("document.forms[0][\"BROTHER_FLG" + testdiv + "\"]");
    brotherReason = eval("document.forms[0][\"BROTHER_REASON" + testdiv + "\"]");
    if (brotherFlg.checked) {
        brotherReason.disabled = false;
    } else {
        brotherReason.disabled = true;
    }
}
