function btn_submit(cmd) {
    if (cmd == 'delete') {
        result = confirm('{rval MSG103}');
        if (result == false)
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closeMethod() {
    top.opener.parent.edit_frame.btn_submit('edit_src');
    closeWin();
}

function Btn_reset(cmd) {
    if (cmd == 'reset'){
        result = confirm('{rval MSG106}');
        if (result == false)
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
