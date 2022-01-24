function btn_submit(cmd) {
    if (cmd == 'exec') {
        //ヘッダ出力
        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = 'head';

        //エラー出力
        } else if (document.forms[0].OUTPUT[2].checked == true) {
            cmd = 'error';

        //データ出力・取込
        } else if (document.forms[0].OUTPUT[3].checked == true) {
            if (document.forms[0].APPLICANTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試制度');
                return false;
            }
            if (document.forms[0].SCHOOL_KIND.value == "J" || document.forms[0].SCHOOL_KIND.value == "P") {
                if (document.forms[0].TESTDIV.value == "") {
                    alert('{rval MSG301}' + '\n入試区分');
                    return false;
                }
            } else {
                if (document.forms[0].TESTDIV0.value == "") {
                    alert('{rval MSG301}' + '\n入試区分');
                    return false;
                }
            }
            if (document.forms[0].DATADIV.value == "") {
                alert('{rval MSG301}' + '\nデータ種類');
                return false;
            }
            cmd = 'data';

        //データ取込
        } else if (document.forms[0].OUTPUT[1].checked == true) {
            if (document.forms[0].APPLICANTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試制度');
                return false;
            }
            if (document.forms[0].SCHOOL_KIND.value == "J" || document.forms[0].SCHOOL_KIND.value == "P") {
                if (document.forms[0].TESTDIV.value == "") {
                    alert('{rval MSG301}' + '\n入試区分');
                    return false;
                }
            } else {
                if (document.forms[0].TESTDIV0.value == "") {
                    alert('{rval MSG301}' + '\n入試区分');
                    return false;
                }
            }
            if (document.forms[0].DATADIV.value == "") {
                alert('{rval MSG301}' + '\nデータ種類');
                return false;
            }
        }

        //ヘッダ出力・データ取込
        if (document.forms[0].OUTPUT[0].checked == true || document.forms[0].OUTPUT[1].checked == true) {
            if (document.forms[0].SCHOOL_KIND.value == "J" && document.forms[0].TESTDIV.value == "99") {
                alert('入試区分は「全て」以外を選択してください');
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
