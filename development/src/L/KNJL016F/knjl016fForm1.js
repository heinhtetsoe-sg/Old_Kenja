function btn_submit(cmd) {
    if (cmd == 'exec') {
        //エラー出力
        if (document.forms[0].OUTPUT[1].checked == true) {
            cmd = 'error';

        //データ取込
        } else if (document.forms[0].OUTPUT[0].checked == true) {
            if (document.forms[0].APPLICANTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試制度');
                return false;
            }
            if (document.forms[0].WEB_TESTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試区分');
                return false;
            }
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
