function btn_submit(cmd) {
    
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm('{rval MSG106}'))
        return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

