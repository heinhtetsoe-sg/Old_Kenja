function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限
function closing_window()
{
    alert('{rval MSG300}');
    closeWin();
    return true;
}
