function btn_submit(cmd) {
    //コピー確認
    if (cmd == 'copy'){
        if (!confirm('{rval MSG102}\n（前年度コピー）'))
            return false;
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
