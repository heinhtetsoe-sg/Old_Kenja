function btn_submit(cmd) {
    //必須チェック
    if (cmd == 'add' || cmd == 'update' || cmd == 'delete') {
        if (document.forms[0].SCHREGNO.value == '') {
            alert('{rval MSG304}');
            return true;
        }
    }

    if (cmd == 'add') {
        //必須チェック
        if (document.forms[0].VISIT_DATE.value == '' || document.forms[0].VISIT_HOUR.value == '' || document.forms[0].VISIT_MINUTE.value == '') {
            alert('{rval MSG301}\n(来室日時)');
            return true;
        }
    }

    //削除確認
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }

    //取消確認
    if (cmd == 'clear' && !confirm('{rval MSG107}')) {
        return true;
    }

    //PDFファイル取込確認
    if (cmd == 'upload' && document.forms[0].CHECK_PDF.value == true) {
        if (!confirm('すでにファイルが存在されています。\n上書きしてもよろしいですか？')) {
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
