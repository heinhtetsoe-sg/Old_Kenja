function btn_submit(cmd) {
    if (cmd == 'csv') {
        if (!confirm('CSV出力（データ確認）を実行します。よろしいですか？')) {
            return false;
        }
    }
    if (cmd == 'houkoku') {
        if (!confirm('県へのデータ提出を実行します。よろしいですか？')) {
            return false;
        }
        if (document.forms[0].EXEC_DATE_ERRCHECK.value != '') {
            if (!confirm('すでに提出していますが、上書きしますか？')) {
                return false;
            }
        }
        //データ提出実行中
        document.getElementById('marq_msg').style.color = '#FF0000';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
