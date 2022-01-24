function btn_submit(cmd) {
    if (confirm('{rval MSG101}')) {
    } else {
        return;
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
