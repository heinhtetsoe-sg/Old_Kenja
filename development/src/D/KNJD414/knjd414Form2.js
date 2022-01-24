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
