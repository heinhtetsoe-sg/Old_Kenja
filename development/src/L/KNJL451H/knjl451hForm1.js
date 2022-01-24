function btn_submit(cmd) {
    if (cmd == "sim" || cmd == "decision") {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert("{rval MSG301}\n( 入試制度 )");
            return;
        }
        if (document.forms[0].TESTDIV.value == "") {
            alert("{rval MSG301}\n( 試験回数 )");
            return;
        }
        if (document.forms[0].EXAMCOURSECD.value == "") {
            alert("{rval MSG301}\n( 志望コース )");
            return;
        }
        if (document.forms[0].EXAM_TYPE.value == "") {
            alert("{rval MSG301}\n( 受験型 )");
            return;
        }
        if (document.forms[0].SHDIV.value == "") {
            alert("{rval MSG301}\n( 出願区分 )");
            return;
        }
        if (document.forms[0].BORDER_SCORE.value == "") {
            alert("{rval MSG301}\n( 合格点 )");
            return;
        }
    }
    if ((cmd == "sim" || cmd == "decision") && !confirm("{rval MSG101}")) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
