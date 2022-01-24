function btn_submit(cmd) {
    //必須チェック
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    //取消
    if (cmd == 'subform2_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    /*if (cmd == 'subform1'){
        if (!confirm('{rval MSG108}')) {
            return false;
        }
    }*/

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
