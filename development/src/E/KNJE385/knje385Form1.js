function btn_submit(cmd) {
    if (cmd == 'exec') {
        if (!confirm('処理を開始します。よろしいでしょうか？')) {
            document.forms[0].cmd.value
            return false;
        }
        if (document.forms[0].OUTPUT[1].checked == false) {
            cmd = 'csv';
        } else {
            //読み込み中は、実行ボタンはグレーアウト
            document.forms[0].btn_exec.disabled = true;
            document.getElementById('marq_msg').style.color = '#FF0000';
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
