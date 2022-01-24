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
        //CSV出力
        if (document.forms[0].OUTPUT[3].checked == true) {
            cmd = 'csv';
        }
        if (document.forms[0].OUTPUT[4].checked == true || document.forms[0].OUTPUT[5].checked == true) {
            cmd = 'csv2';
        }
    }
    if (cmd == 'exec' || cmd == 'head' || cmd == 'csv') {
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert('{rval MSG310}' + '\n学校種別');
            return false;
        }
        if (document.forms[0].TESTDIV.value == "") {
            alert('{rval MSG310}' + '\n入試種別');
            return false;
        }
        if (document.forms[0].EXAM_TYPE.value == "") {
            alert('{rval MSG310}' + '\n入試方式');
            return false;
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
            document.getElementById('marq_msg').style.color = '#FF0000';
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
