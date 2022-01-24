function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closing_window(MSGCD) {
    if (MSGCD == 'MSG300') {
        alert('{rval MSG300}');
    }
    closeWin();
    return true;
}
