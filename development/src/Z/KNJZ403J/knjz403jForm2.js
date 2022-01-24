function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    }
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
