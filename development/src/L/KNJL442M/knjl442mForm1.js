function btn_submit(cmd) {
    if (cmd == "exec"){
        //必須チェック
        if (document.forms[0].EXAM_SCHOOL_KIND.value == "") {
            alert('{rval MSG310}' + "\n(校種)");
            return;
        } else if (document.forms[0].APPLICANT_DIV.value == "") {
            alert('{rval MSG310}' + "\n(入試区分)");
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
