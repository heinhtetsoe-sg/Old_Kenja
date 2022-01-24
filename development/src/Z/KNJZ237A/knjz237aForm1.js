function btn_submit(cmd) {

    if (cmd == 'copy' && !confirm('{rval MSG101}')){
        return false;
    }
    if (cmd == 'list_gakki') {
        cmd = 'list';
        document.forms[0].TESTKINDCD.value = "";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
