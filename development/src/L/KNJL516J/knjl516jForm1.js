function btn_submit(cmd) {
    if (cmd == 'exec') {
        //データ出力
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert('{rval MSG301}' + '\n学校種別');
            return false;
        }
        if (document.forms[0].TESTDIV.value == "") {
            alert('{rval MSG301}' + '\n入試種別');
            return false;
        }
        cmd = 'data';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
