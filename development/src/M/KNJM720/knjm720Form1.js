function btn_submit(cmd) {
    if (cmd == 'copy') {
        var msg = '';
        if (document.forms[0].lastCnt.value == 0) {
            msg = "前年度のデータがありません。\n";
        }
        if (document.forms[0].ctrlCnt.value > 0) {
            msg += "指定年度のデータが存在しています。";
        }
        if (msg != '') {
            alert(msg);
            return false;
        }
        result = confirm('{rval MSG102}');
        if (result == false) {
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
