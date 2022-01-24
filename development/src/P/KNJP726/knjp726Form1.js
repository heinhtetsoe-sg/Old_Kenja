function btn_submit(cmd) {
    if (cmd == 'exec') {
        //ヘッダ出力
        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = 'head';

        //エラー出力
        } else if (document.forms[0].OUTPUT[2].checked == true) {
            cmd = 'error';

        //データ出力
        } else if (document.forms[0].OUTPUT[3].checked == true) {
            cmd = 'data';

        //データ取込
        } else if (document.forms[0].OUTPUT[1].checked == true) {
        }
    }

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
