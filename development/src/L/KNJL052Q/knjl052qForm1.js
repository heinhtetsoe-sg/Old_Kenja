function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        alert('{rval MSG303}');
        return false;
    }

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

function Setflg(obj, receptno) {
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;

    document.getElementById('ROWID' + receptno).style.background="yellow";
    obj.style.background="yellow";
}
//高校入試、分散入力用
function clickUpdate(obj, receptno) {
    //更新処理用（得点、受験番号セット）
    document.forms[0].HID_UP_RECEPTNO.value = receptno;
    document.forms[0].HID_UP_INTERVIEW_VALUE.value = obj.value;

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
