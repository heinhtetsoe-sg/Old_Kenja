function btn_submit(cmd) {
    // 必須チェック
    if (cmd == 'actual2_insert' || cmd == 'actual2_update' || cmd == 'actual2_delete') {
        if (!document.forms[0].SUBCLASS.value) {
            alert('{rval MSG301}' + '\n教科・科目');
            return false;
        }
    }
    if (cmd == 'actual2_insert') {
        if (document.forms[0].SELECT_SUBCLASS.value == "1") {
            alert('{rval MSG918}');
            return false;
        }
    }
    if (cmd == 'actual2_update' || cmd == 'actual2_delete') {
        if (document.forms[0].SELECT_SUBCLASS.value == "0") {
            alert('{rval MSG303}');
            return false;
        }
    }
    if (cmd == 'actual2_delete' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
