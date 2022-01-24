function btn_submit(cmd){
    //必須チェック
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')){
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新
function doSubmit(cmd) {
    //必須チェック
    if (document.forms[0].CONDITION.value == "") {
        alert('{rval MSG301}' + '(状態区分)');
        return true;
    }
    if (document.forms[0].GROUPCD.value == "") {
        alert('{rval MSG301}' + '(科目グループ名)');
        return true;
    }
    if (document.forms[0].SET_SUBCLASSCD.value == "") {
        alert('{rval MSG301}' + '(科目)');
        return true;
    }
    if (document.forms[0].UNITCD.value == "") {
        alert('{rval MSG301}' + '(単元コード)');
        return true;
    }
    if (document.forms[0].UNITNAME.value == "") {
        alert('{rval MSG301}' + '(単元名)');
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
