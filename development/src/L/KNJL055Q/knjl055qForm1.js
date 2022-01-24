function btn_submit(cmd) {
    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled)) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//高校入試、分散入力用
function clickUpdate(obj, receptno) {
    //更新処理用（得点、受験番号セット）
    document.forms[0].HID_UP_RECEPTNO.value = receptno;
    document.forms[0].HID_UP_COMPOSITION_VALUE.value = obj.value;

    //スクロール量保持
    var y = document.getElementById("mainDiv").scrollTop;
    document.forms[0].SET_SCROLL_VAL.value = y;

    document.forms[0].cmd.value = 'update_H';
    document.forms[0].submit();

    return false;
}
//カーソルセット
function setScroll(setval) {
    document.getElementById("mainDiv").scrollTop = setval;

    return false;
}
