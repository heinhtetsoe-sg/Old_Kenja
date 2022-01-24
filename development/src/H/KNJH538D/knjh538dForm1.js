function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//リンク
function Page_jumper(link) {
    parent.location.href=link;
}
