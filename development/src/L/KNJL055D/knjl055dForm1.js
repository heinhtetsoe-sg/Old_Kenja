// kanji=漢字
function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('{rval MSG101}')) {
        return;
    }
    if (cmd == 'exec') {
        document.getElementById('marq_msg').style.color = '#FF0000';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
