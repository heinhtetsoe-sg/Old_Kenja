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

function setDisabled(obj) {
    //削除
    if (obj.value == '03') {
        document.forms[0].TO_SCHOOLCD.disabled = true;
        document.forms[0].TO_COURSECD.disabled = true;
    } else {
        document.forms[0].TO_SCHOOLCD.disabled = false;
        document.forms[0].TO_COURSECD.disabled = false;
    }
}
