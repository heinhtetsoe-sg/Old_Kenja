function btn_submit(cmd) {
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].CLASSIFY_CD.value == '') {
            alert('{rval MSG301}' + '\n(類別コード)');
            return false;
        }
        if (document.forms[0].CLASSIFY_NAME.value == '') {
            alert('{rval MSG301}' + '\n(類別)');
            return false;
        }

    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
