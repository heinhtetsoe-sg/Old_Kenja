function btn_submit(cmd) {

    if (cmd == 'copy'){
        result = confirm('{rval MSG101}');
        if (result == false)
            return false;
    }

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
