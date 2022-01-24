function btn_submit(cmd) {

    if (cmd == 'csv') {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert('{rval MSG301}' + '\n入試制度');
            return false;
        }
        if (document.forms[0].TESTDIV.value == "") {
            alert('{rval MSG301}' + '\n入試区分');
            return false;
        }
        if (document.forms[0].JUDGEDIV.value == "") {
            alert('{rval MSG301}' + '\n合否');
            return false;
        }

        //確認メッセージ
        if (!confirm('処理を開始します。よろしいでしょうか？')) {
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
