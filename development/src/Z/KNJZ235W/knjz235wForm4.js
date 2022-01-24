function btn_submit(cmd) {
    //前年度コピー確認
    if (cmd == "copy") {
        if (!confirm("{rval MSG101}")) {
            return false;
        }
    }
    //取消確認
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function setPattern() {
    if (document.forms[0].SEQ001[1].checked) {
        document.forms[0].SEQ006.disabled = true;
        if (document.forms[0].SEQ006.checked) {
            document.forms[0].SEQ006.checked = false;
        }
    } else {
        document.forms[0].SEQ006.disabled = false;
    }

    return false;
}
