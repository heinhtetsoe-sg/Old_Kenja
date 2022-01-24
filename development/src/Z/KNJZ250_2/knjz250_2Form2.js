function btn_submit(cmd) {
    if (cmd == 'delete') {
        result = confirm('{rval MSG103}');
        if (result == false)
            return false;
    }
    if (cmd == 'add' && document.forms[0].CERTIF_KINDCD.value > 99) {
        alert('証明書種類コードは\n99までです。');
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    if (cmd == 'reset'){
        result = confirm('{rval MSG106}');
        if (result == false)
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
