function btn_submit(cmd)
{
    if (cmd == 'next2' || cmd == 'back2') {
        if (change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm()
{
    if(confirm('{rval MSG106}')) return true;
    return false;
}

