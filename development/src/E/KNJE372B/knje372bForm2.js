function btn_submit(cmd) {
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
