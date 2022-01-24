function btn_submit(cmd) {
    //コピー確認
    if (cmd == 'copy' && !confirm('{rval MSG101}')){
        return false;
    }

    //学期変更
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
