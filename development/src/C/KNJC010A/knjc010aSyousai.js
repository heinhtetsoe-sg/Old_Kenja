function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) {
            return false;
        }
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function closeMethod() {
    //alert(top.frames.name);
    //alert(parent.document.forms[0].cmd.value);
    //alert(document.forms[0].SYOUSAI_HIDDEN_SCHREG.value);
    //alert(document.forms[0].SYOUSAI_SCHREGNO.value);
    parent.document.forms[0].submit();
    top.closeit();
}
