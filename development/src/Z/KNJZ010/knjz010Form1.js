function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
	document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_submit(cmd)
{    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}