window.onload = init;
function init() {
    document.forms[0].SIMEBI.value = getSimebi();
}
function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closing_window()
{
    alert('{rval MSG300}');
    closeWin();
    return true;
}
