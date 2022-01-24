function btn_submit(cmd) {
    //取消
    if (cmd == 'subform1_clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //更新
    if (cmd == 'subform1_update') {
        if (!confirm('{rval MSG102}')) {
            return false;
        }
    }
    //削除
    if (cmd == 'subform1_delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
