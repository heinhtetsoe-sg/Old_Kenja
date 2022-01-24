function btn_submit(cmd) {
    //�O�N�x�R�s�[
    if (cmd == 'copy' && !confirm('{rval MSG101}')) {
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
