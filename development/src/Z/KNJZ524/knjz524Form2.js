function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //必須入力チェック
    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].CERTIFNAME_CD.value == '') {
            alert('{rval MSG301}\n　　　　　(　受給者証コード　)');
            return false;
        }
        if (document.forms[0].CERTIFNAME.value == '') {
            alert('{rval MSG301}\n　　　　　(　受給者証名称　)');
            return false;
        }
    } else if (cmd == 'delete') {
        if (document.forms[0].CERTIFNAME_CD.value == '') {
            alert('{rval MSG301}\n　　　　　(　受給者証コード　)');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
