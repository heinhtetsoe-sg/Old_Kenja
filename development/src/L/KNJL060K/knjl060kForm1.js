function btn_submit(cmd)
{
    if(cmd == 'exec'){
        if (!confirm('{rval MSG101}')){
            return false;
        }
    }

    document.all('marq_msg').style.color = '#FF0000';   //alp m-yama 2005/09/03

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
