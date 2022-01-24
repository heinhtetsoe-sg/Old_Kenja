function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//実行
function doSubmit() {
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG301}\n( 入試制度 )');
        return true;
    }

    if (!confirm('すでに実行し作成したデータは削除されます。\nよろしいですか？')) {
        return false;
    }

    document.forms[0].cmd.value = 'exec';
    document.forms[0].submit();
    return false;
}
