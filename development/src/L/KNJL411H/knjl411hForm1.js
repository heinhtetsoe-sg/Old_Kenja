function btn_submit(cmd) {
    if (cmd == 'csvExec') {
        if (document.forms[0].OUTPUT3.checked) {
            cmd = 'csvDownload';
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
