function btn_submit(cmd) {
    if (cmd == 'exec') {
        //エラー出力
        if (document.forms[0].OUTPUT[1].checked == true) {
            cmd = 'error';
        }
    }

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (cmd == 'exec') {
        //データ取込
        if (document.forms[0].OUTPUT[0].checked == true) {
            document.getElementById('marq_msg').style.color = '#FF0000';
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
