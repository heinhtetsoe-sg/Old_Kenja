//サブミット
function btn_submit(cmd)
{
    if (cmd == 'update') {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }

    parent.closeit();
    parent.document.forms[0].cmd.value = '';
    parent.document.forms[0].submit();
    return false;
}
