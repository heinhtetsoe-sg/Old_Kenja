function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MZ0003}'))
            return false;
    }

    window.open('knjz236index.php?cmd=sel&PROGRAMID=KNJZ236','right_frame');

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
