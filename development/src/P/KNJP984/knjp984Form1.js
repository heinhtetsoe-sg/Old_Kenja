function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_ctrls(e) {
    document.forms[0].btn_add.disabled    = false;
    document.forms[0].btn_update.disabled = false;
    document.forms[0].btn_reset.disabled  = false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
