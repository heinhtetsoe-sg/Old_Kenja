function btn_submit(cmd) {

    //削除
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
