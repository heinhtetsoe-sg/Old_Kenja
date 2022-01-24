function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'delete' || cmd == 'sendDel') {
        if (!confirm('{rval MSG101}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
