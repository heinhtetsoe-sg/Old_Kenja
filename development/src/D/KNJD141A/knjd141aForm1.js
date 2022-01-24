function btn_submit(cmd) {
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function attentoinScoreCheck(obj, value) {
    value = parseInt(value);
    if (value < 0 || value > 100) {
        alert("値が範囲外です。");
        obj.value = '';
        obj.focus();
    }
}
