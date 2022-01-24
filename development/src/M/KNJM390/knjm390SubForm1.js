function btn_submit(cmd)
{
    if (cmd == 'update' || cmd == 'reset'){
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
    
    top.main_frame.closeit();
    top.main_frame.document.forms[0].cmd.value = '';
    top.main_frame.document.forms[0].submit();
    return false;
}
function setcmd()
{
    document.forms[0].cmd.value = '';
    document.forms[0].submit();
    return false;
}
