function btn_submit(cmd) {
    //取消確認
    if (cmd == "clear" && !confirm('{rval MSG106}')) {
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function closing_window() {
    alert('{rval MSG300}');
    closeWin();
    return true;
}
