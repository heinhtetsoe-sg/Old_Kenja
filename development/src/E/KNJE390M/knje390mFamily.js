function btn_submit(cmd) {
    if (cmd == "family_delete" && !confirm("{rval MSG103}")) {
        return true;
    }
    if (cmd == "family_insert" || cmd == "family_update") {
        if (document.forms[0].RELANAME.value == "") {
            alert("{rval MSG301}" + "(氏名)");
            return false;
        } else if (document.forms[0].RELAKANA.value == "") {
            alert("{rval MSG301}" + "(氏名かな)");
            return false;
        }
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
