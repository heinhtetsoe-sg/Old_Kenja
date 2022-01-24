function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

//disabled
function OptionUse(obj) {
    if (document.forms[0].check.checked == true) {
        document.forms[0].grd_div.disabled = false;
    } else {
        document.forms[0].grd_div.disabled = true;
    }
}
