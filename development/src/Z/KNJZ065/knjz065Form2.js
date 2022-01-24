function btn_submit(cmd){
    //必須チェック
    if (cmd == 'edit2') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'update') {
        //必須チェック
        if (document.forms[0].UNITCD.value == "") {
            alert('{rval MSG301}' + '(単元コード)');
            return true;
        }
        if (document.forms[0].UNITNAME.value == "") {
            alert('{rval MSG301}' + '(単元名（指導目標）)');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
