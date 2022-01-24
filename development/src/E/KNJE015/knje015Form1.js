function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == '') {
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'reset') {
        //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if (cmd == 'update') {
        //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
        document.forms[0].btn_update.disabled = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_check() {
    if (document.forms[0].SCHREGNO.value == '') {
        alert('{rval MSG304}');
        return true;
    }
    return false;
}
function btn_openerSubmit() {
    window.opener.btn_submit('edit');
    closeWin();
}
