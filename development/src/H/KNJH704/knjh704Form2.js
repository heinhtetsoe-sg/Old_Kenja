function btn_submit(cmd) {
    if (cmd == "reset") {
        //取消確認
        if (confirm("{rval MSG106}") == false) {
            return false;
        }
    }
    if (cmd == "delete") {
        //削除確認
        if (confirm("{rval MSG103}") == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

//テキスト内でEnterを押してもsubmitされないようにする
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}
