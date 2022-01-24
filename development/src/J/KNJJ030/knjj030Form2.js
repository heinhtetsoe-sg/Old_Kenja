function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){ 
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    } else {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}

//セキュリティチェック
function OnAuthError()
{
	alert('{rval MSG300}');
	closeWin();
}
