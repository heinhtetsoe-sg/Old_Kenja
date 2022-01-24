function btn_submit(cmd) {
    if ((cmd == 'exec') && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (cmd == 'exec') {
        document.forms[0].btn_exec.disabled  = true;
        document.getElementById('marq_msg').style.color = '#FF0000';
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
