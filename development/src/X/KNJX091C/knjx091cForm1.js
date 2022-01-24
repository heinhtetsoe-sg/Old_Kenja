function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[1].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
    }
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = 'head';
        }
        if (document.forms[0].OUTPUT[2].checked == true) {
            cmd = 'error';
        }
        if (document.forms[0].OUTPUT[3].checked == true) {
            cmd = 'data';
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
