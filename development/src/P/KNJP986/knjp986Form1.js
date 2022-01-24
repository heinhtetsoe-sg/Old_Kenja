function btn_submit(cmd) {
    if (document.forms[0].FROM_DATE.value == '') {
        alert('{rval MSG301}' + '\n(決済期間)');
        return;
    }
    if (document.forms[0].TO_DATE.value == '') {
        alert('{rval MSG301}' + '\n(決済期間)');
        return;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();

}
