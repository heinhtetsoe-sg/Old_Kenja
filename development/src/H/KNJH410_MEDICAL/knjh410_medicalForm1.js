function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_reset() {
    //ポートフォリオ画面が動かなくなるのを回避
    window.opener.document.forms[0].cmd.value = 'radio';
    window.opener.document.forms[0].submit();
    closeWin();
    
}
