function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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

function OnPreError(cd)
{
    alert('{rval MSG305}' + '\n('+cd+')');
    closeWin();
}
