function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    //読み込み時の受験番号チェック
    if (cmd == 'back' || cmd == 'now' || cmd == 'next') {
        if (document.forms[0].S_EXAMNO.value == '') {
            alert('{rval MSG301}' + ' \n( 受験番号 )');
        }
    }

    //更新
    if (cmd == 'update') {
        if (!confirm('{rval MSG102}')) {
            return false;
        }
    }

    if (cmd == 'update') {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

