function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].HID_EXAMNO.value.length == 0 || confirm('{rval MSG108}')) {
            closeWin();
        }
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

