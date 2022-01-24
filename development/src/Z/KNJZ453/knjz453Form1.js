function btn_submit(cmd) {
    if (cmd == 'list') {
        parent.right_frame.location.href='knjz453index.php?cmd=edit&chFlg=1&YEAR=' + document.forms[0].YEAR.value + '&SUBCLASS=' + document.forms[0].SUBCLASS.value + '&SIKAKUCD=' + document.forms[0].SIKAKUCD.value + '&GRADE=' + document.forms[0].GRADE.value;
    }

    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
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
