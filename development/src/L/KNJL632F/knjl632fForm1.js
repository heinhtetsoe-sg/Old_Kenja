function btn_submit(cmd) {
    if (cmd == 'exec') {
        //ヘッダー出力
        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = 'head';
        }
        //エラー出力
        if (document.forms[0].OUTPUT[2].checked == true) {
            cmd = 'error';
        }
    }

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (cmd == 'exec') {
        //データ取込
        if (document.forms[0].OUTPUT[1].checked == true) {
            //読み込み中は、実行ボタンはグレーアウト
            document.forms[0].btn_exec.disabled = true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
