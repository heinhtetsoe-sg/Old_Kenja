function btn_submit(cmd) {
    //削除
    if (cmd == 'delete' && !confirm('{rval MSG103}'+'\n\n注意：この生徒の関連データも全て削除されます！')){
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
