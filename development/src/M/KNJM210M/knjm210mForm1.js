function btn_submit(cmd)
{
    if (document.forms[0].SCHREGNO.value == '' && 
        document.forms[0].NAME.value == '' && 
        document.forms[0].NAME_KANA.value == '')
    {
        alert('{rval MSG301}' + '最低一項目を指定してください。');
        return;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
