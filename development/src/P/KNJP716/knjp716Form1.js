function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function OnPreError(cd)
{
    alert('{rval MSG305}' + '\n('+cd+')');
    closeWin();
}
