function btn_submit(cmd) {
    if (cmd == "clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    } else if (cmd == "copy") {
        if (document.forms[0].PRE_YEAR_CNT.value <= 0) {
            alert('前年度のデータが存在しません。');
            return false;
        }
        if (document.forms[0].THIS_YEAR_CNT.value > 0) {
            if (!confirm('今年度のデータは破棄されます。コピーしてもよろしいですか？')) {
                return false;
            }
        } else {
            if (!confirm('{rval MSG101}')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
