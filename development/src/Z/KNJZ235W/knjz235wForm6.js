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

function setPattern(key) {
    //選択されたパターンを保持
    document.forms[0].HID_FRM_PATERN.value = document.forms[0].FRM_PATERN[key - 1].value;
}
