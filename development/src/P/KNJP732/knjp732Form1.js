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
            var e = document.getElementById('marq_msg');
            e.style.color = '#FF0000';
            e.style.fontWeight = '400';
            e.innerHTML = '処理中です...しばらくおまちください';

            document.forms[0].btn_exec.disabled = true;
            document.forms[0].btn_end.disabled = true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
