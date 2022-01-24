function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function Sending() {
    var str  = 'knjj212index.php?cmd=edit&YEAR=' + document.forms[0].YEAR.value + '&UNSETCD=1';
    btn_submit('list');
    window.open(str,'right_frame');
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
