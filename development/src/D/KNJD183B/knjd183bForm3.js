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
    var isA = document.forms[0].SEQ0021.checked;
    var isC = document.forms[0].SEQ0023.checked;
    var isD = document.forms[0].SEQ0024.checked;
    var isE = document.forms[0].SEQ0025.checked;
    document.forms[0].SEQ0041.disabled = !isA;
    document.forms[0].SEQ0042.disabled = !isA;
    document.forms[0].SEQ005.disabled = isA;
    document.forms[0].SEQ0061.disabled = !isA;
    document.forms[0].SEQ0062.disabled = !isA;
    document.forms[0].SEQ008.disabled = !isC;
    document.forms[0].SEQ010.disabled = !isE;
    document.forms[0].SEQ012.disabled = !isE;
}
