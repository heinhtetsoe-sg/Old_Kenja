function btn_submit(cmd)
{

    if (cmd == 'exec') {
        if (confirm('{rval MSG101}')) {
            document.all('marq_msg').style.color = '#FF0000';
        } else {
            return;
        }

        document.forms[0].btn_exec.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
