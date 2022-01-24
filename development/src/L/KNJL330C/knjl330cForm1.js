function btn_submit(cmd) {
    //ＣＳＶ出力
    if (cmd == "csv") {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG310}\n( 入試制度 )');
            return;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG310}\n( 入試区分 )');
            return;
        }
        if (document.forms[0].SHDIV.value == '') {
            alert('{rval MSG310}\n( 専併区分 )');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
