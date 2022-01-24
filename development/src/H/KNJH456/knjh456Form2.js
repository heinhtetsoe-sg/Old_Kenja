function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'update' || cmd == 'makeMock'){
        document.forms[0].btn_update.disabled = true;
        //document.forms[0].btn_del.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//模試データ作成不可
function btnMockDisabled()
{
    document.forms[0].btn_mock.disabled = true;
}
//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
