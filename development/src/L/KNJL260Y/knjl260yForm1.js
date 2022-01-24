// kanji=漢字
function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('{rval MSG101}')) {
        return;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
