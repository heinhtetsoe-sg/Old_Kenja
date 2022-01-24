function btn_submit(cmd) {
    //漢字
    if (confirm('{rval MSG101}')) {
    } else {
        return;
    }
    document.all('marq_msg').style.color = '#FF0000';

    document.forms[0].btn_exec.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
