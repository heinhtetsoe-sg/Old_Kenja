function btn_submit(cmd) {
    if (cmd == "resetJugyouNaiyou" && !confirm("{rval MSG106}")) {
        return false;
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
    parent.document.forms[0].submit();
    top.closeit();
}
