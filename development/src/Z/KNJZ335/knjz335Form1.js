function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MZ0003}'))
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
