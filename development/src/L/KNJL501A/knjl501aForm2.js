function btn_submit(cmd) {
    //チェックを先に実施。メッセージ確認はその後。
    if (cmd == 'update' || cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].HOPE_COURSECODE.value == '' || document.forms[0].COURSECODE.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
