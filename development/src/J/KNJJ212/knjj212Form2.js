function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].COMMITTEECD.value == '' || document.forms[0].NAME.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    } else if (cmd == 'delete') {
        if (document.forms[0].COMMITTEECD.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
