function btn_submit(cmd) {
    //チェックを先に実施。メッセージ確認はその後。
    if (cmd == 'update' || cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].HOPE_COURSECODE.value == '' || document.forms[0].COURSECODE.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
        if (document.forms[0].HID_USEYEAR_CNT.value > 0) {
            alert('{rval MSG305}'+"\nコードが利用されています。年度の登録を解除してから削除してください。");
            return false;
        }
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
