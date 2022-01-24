function btn_submit(cmd) {
    if (cmd == "") {
        confirm("{rval MSG106}");
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window() {
    alert("{rval MSG300}");
    closeWin();
    return true;
}
