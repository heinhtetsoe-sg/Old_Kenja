function btn_submit(cmd) {
    //削除
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
