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
        if (document.forms[0].TESTSUBCLASSCD.value == '') {
            alert('{rval MSG301}' + '\n(試験科目)');
            return false;
        }
        if (document.forms[0].PERFECT.value == '') {
            alert('{rval MSG301}' + '\n(満点)');
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
