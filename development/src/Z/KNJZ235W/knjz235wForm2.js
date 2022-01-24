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
    if (document.forms[0].SEQ0011.checked) {
        document.forms[0].SEQ016.disabled = false;
        document.forms[0].SEQ008.disabled = false;
        document.forms[0].SEQ007.disabled = false;
        document.forms[0].SEQ017.disabled = false;
        document.forms[0].SEQ018.disabled = true;
        document.forms[0].SEQ019.disabled = true;
        document.forms[0].SEQ020.disabled = true;
        document.forms[0].SEQ021.disabled = true;
    }
    if (document.forms[0].SEQ0012.checked) {
        document.forms[0].SEQ016.disabled = false;
        document.forms[0].SEQ008.disabled = false;
        document.forms[0].SEQ007.disabled = false;
        document.forms[0].SEQ017.disabled = false;
        document.forms[0].SEQ018.disabled = true;
        document.forms[0].SEQ019.disabled = true;
        document.forms[0].SEQ020.disabled = true;
        document.forms[0].SEQ021.disabled = true;
    }
    if (document.forms[0].SEQ0013.checked) {
        document.forms[0].SEQ016.disabled = true;
        document.forms[0].SEQ008.disabled = true;
        document.forms[0].SEQ007.disabled = true;
        document.forms[0].SEQ017.disabled = false;
        document.forms[0].SEQ018.disabled = false;
        document.forms[0].SEQ019.disabled = false;
        document.forms[0].SEQ020.disabled = true;
        document.forms[0].SEQ021.disabled = true;
    }
    if (document.forms[0].SEQ0014.checked) {
        document.forms[0].SEQ016.disabled = true;
        document.forms[0].SEQ008.disabled = true;
        document.forms[0].SEQ007.disabled = true;
        document.forms[0].SEQ017.disabled = false;
        document.forms[0].SEQ018.disabled = false;
        document.forms[0].SEQ019.disabled = true;
        document.forms[0].SEQ020.disabled = true;
        document.forms[0].SEQ021.disabled = true;
    }
    if (document.forms[0].SEQ0015.checked) {
        document.forms[0].SEQ016.disabled = true;
        document.forms[0].SEQ008.disabled = true;
        document.forms[0].SEQ007.disabled = true;
        document.forms[0].SEQ017.disabled = true;
        document.forms[0].SEQ018.disabled = true;
        document.forms[0].SEQ019.disabled = true;
        document.forms[0].SEQ020.disabled = false;
        document.forms[0].SEQ021.disabled = false;
    }
    if (document.forms[0].SEQ0016.checked) {
        document.forms[0].SEQ016.disabled = true;
        document.forms[0].SEQ008.disabled = false;
        document.forms[0].SEQ007.disabled = false;
        document.forms[0].SEQ017.disabled = true;
        document.forms[0].SEQ018.disabled = true;
        document.forms[0].SEQ019.disabled = true;
        document.forms[0].SEQ020.disabled = true;
        document.forms[0].SEQ021.disabled = true;
    }
}
