function btn_submit(cmd) {

    //CSV出力
    if (cmd == "csvOutput") {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試制度）');
            return true;
        }
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試区分）');
            return true;
        }
        if (document.forms[0].STATUS_PASS.value == ""){
            alert('{rval MSG301}'+ '\n（合格者）');
            return true;
        }
        //送付先：合否サイト
        if (document.forms[0].OUTPUT[0].checked){
            if (document.forms[0].STATUS_UNPASS.value == ""){
                alert('{rval MSG301}'+ '\n（不合格者）');
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}