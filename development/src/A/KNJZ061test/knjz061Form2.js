function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closeMethod() {
    if (document.forms[0].callId.value == "KNJE066") {
        top.opener.parent.edit_frame.btn_submit('edit_src');
    } else {
        top.opener.parent.right_frame.btn_submit('list');
        top.opener.parent.right_frame.btn_submit('edit2');
    }
    closeWin();
}

function EnableBtns(e){
    document.forms[0].btn_add.disabled    = false;
    document.forms[0].btn_udpate.disabled = false;
    document.forms[0].btn_reset.disabled  = false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
