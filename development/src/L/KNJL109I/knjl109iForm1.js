function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        closeWin();
    }

    if (cmd == 'huban') {
        if (document.forms[0].CHG_FLG.value == "1") {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeValue() {
    document.forms[0].CHG_FLG.value = "1";
}