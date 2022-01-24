function btn_submit(cmd) 
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(link)
{
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

