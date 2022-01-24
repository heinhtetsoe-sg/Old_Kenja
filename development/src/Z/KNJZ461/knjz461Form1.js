function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Sending() {
    var str = 'knjz461index.php?cmd=edit&COMBO_SCHKIND=' + document.forms[0].SCHKIND.value;
    btn_submit('list');
    window.open(str,'right_frame');
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
