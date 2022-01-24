function btn_submit(cmd) {
    if (cmd == "search") {
        parent.right_frame.document.forms[0].cmd.value = "edit";
        parent.right_frame.document.forms[0].submit();
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
