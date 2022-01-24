function btn_submit(cmd) {

    if (cmd == 'csv') {
        if (document.forms[0].FROM_MONTH.value == '' || document.forms[0].TO_MONTH.value == '') {
            alert('出力期間を指定して下さい。');
            return false;
        }
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
