function btn_submit(cmd) {
    if (cmd == 'csv' && document.forms[0].DIRECT_DEBIT.value == '') {
        alert('{rval MSG301}' + '\n(作成日)');
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
