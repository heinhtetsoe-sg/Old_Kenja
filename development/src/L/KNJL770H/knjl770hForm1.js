function btn_submit(cmd) {
    if (cmd == "sim" || cmd == "decision" || cmd == "clear") {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert("{rval MSG301}\n( 学校種別 )");
            return;
        }
        if (document.forms[0].TESTDIV.value == "") {
            alert("{rval MSG301}\n( 入試種別 )");
            return;
        }
        if (document.forms[0].SEX_OPT.value == "") {
            alert("{rval MSG301}\n( 性別 )");
            return;
        }
        if (cmd == "sim" || cmd == "decision") {
            if (document.forms[0].BORDER_SCORE.value == "") {
                alert("{rval MSG301}\n( 合格点 )");
                return;
            }
        }
    }
    if ((cmd == "sim" || cmd == "decision") && !confirm("{rval MSG101}")) {
        return true;
    }
    if (cmd == "clear" && !confirm("{rval MSG106}")) {
        return true;
    }
    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
