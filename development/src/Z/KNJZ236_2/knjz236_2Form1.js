function btn_submit(cmd)
{
    if (cmd == 'clear'){
        if (!confirm('{rval MZ0003}'))
            return false;
    }

    window.open('knjz236_2index.php?cmd=sel&PROGRAMID=KNJZ236_2','right_frame');

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
