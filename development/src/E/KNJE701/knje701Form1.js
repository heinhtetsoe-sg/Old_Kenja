function btn_submit(cmd) {
    //前年度コピー確認
    if (cmd == "copy") {
        if (confirm("{rval MSG101}") == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function OnPreError(cd) {
    alert("{rval MSG305}" + "\n(" + cd + ")");
    closeWin();
}
