function btn_submit(cmd) {
    if (cmd == 'exec') {
        //エラー出力
        if (document.forms[0].OUTPUT[1].checked == true) {
            cmd = 'error';

        //データ取込
        } else if (document.forms[0].OUTPUT[0].checked == true) {
            if (document.forms[0].YEAR.value == "") {
                alert('{rval MSG310}' + '\n（年度）');
                return false;
            }
            if (document.forms[0].SEMESTER.value == "") {
                alert('{rval MSG310}' + '\n（学期）');
                return false;
            }
            if (document.forms[0].BSCSEQ.value == "") {
                alert('{rval MSG310}' + '\n（基本時間割）');
                return false;
            }
            if (document.forms[0].BSCSEQ.value == "NEW" && document.forms[0].TITLE.value == "") {
                alert('{rval MSG301}' + '\n（タイトル）');
                return false;
            }
        }
    }

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
