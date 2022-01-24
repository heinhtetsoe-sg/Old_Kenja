function btn_submit(cmd) {
    if (cmd == "copy") {
        if (document.forms[0].YEAR_CNT.value > 0) {
            alert('今年度データが存在します。');
            return false;
        }
        if (document.forms[0].LAST_YEAR_CNT.value <= 0) {
            alert('前年度データが存在しません。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}
