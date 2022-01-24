function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(jump)
{
        parent.location.replace(jump);
}

function showConfirm()
{
    if(confirm('{rval MB0107}')) return true;
    return false;
}
