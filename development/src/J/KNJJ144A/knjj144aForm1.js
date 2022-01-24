function btn_submit(cmd)
{
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
