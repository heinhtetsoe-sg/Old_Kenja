function btn_submit(cmd) {
    if (cmd == "work1_delete" && !confirm("{rval MSG103}")) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}
