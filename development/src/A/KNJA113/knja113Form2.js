function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    //削除
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    }

    //検索
    if (cmd == 'search') {
        if (document.forms[0].SCHREGNO.value == "") {
            alert("学籍番号が入力されていません。");
            return true;
        }
    }

    //一括入力
    if (cmd == 'Ikkatsu') {
        document.forms[0].RIGHT_FRAME.value = "Ikkatsu";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
