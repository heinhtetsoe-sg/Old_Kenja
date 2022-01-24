function btn_submit(cmd) {
    if (document.forms[0].ITEM_CNT.value == "" || document.forms[0].ITEM_CNT.value == 0) {
        alert('総項目数は1以上の整数を入力してください');
        return false;
    }
    if (cmd == 'read') {
        if (!confirm('{rval MSG105}')) {
            return false;
        }
    }
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
