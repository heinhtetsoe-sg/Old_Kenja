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

function Sending() {
    var str = 'knjz462index.php?cmd=edit&SCHKIND=' + document.forms[0].SCHKIND.value;
    btn_submit('list');
    window.open(str,'right_frame');
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
