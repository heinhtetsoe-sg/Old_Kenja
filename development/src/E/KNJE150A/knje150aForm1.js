function btn_submit(cmd) {
    //印刷
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
