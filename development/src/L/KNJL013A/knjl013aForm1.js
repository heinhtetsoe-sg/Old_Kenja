function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//志願者入力画面へ
function openKogamen(URL) {
    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}
