function btn_submit(cmd) {
    if (cmd == "csv" && document.forms[0].EXAM_ID.value == "") {
        alert("{rval MSG301}" + "\n\n( 試験 )");
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
