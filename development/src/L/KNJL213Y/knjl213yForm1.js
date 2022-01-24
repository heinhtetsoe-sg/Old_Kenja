function btn_submit(cmd) {
    if (cmd == 'exec') {
        if (!confirm('{rval MSG101}' + '\n\n注意！！\n\n一度すべて削除してから受付番号を生成します。\n\n削除データ：受付データおよび得点データ')) {
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
