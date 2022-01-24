function btn_submit(cmd) {
    if (cmd == 'execute1' || cmd == 'execute2') {
        if (!confirm('{rval MSG101}')) {
            return true;
        }
    }
    document.forms[0].btn_ok1.disabled = true;
    document.forms[0].btn_ok2.disabled = true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
