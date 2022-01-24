function btn_submit(cmd) {
    if (cmd == 'copy'){
        if (!confirm('{rval MSG101}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
