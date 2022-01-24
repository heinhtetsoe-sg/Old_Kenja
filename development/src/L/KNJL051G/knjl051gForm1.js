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
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].SHDIV.disabled) || (document.forms[0].EXAMCOURSECD.disabled) || (document.forms[0].EXAMHALLCD.disabled)) {
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

//文字数チェック
function CheckLength(obj) {
    str = obj.value;
    var count = 0,
    c = '';

    for (var i = 0, len = str.length; i < len; i++) {
        c = str.charCodeAt(i);
        if ((c >= 0x0 && c < 0x81) || (c == 0xf8f0) || (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)) {
            count += 1;
        } else {
            count += 3;
        }
    }

    if (count > 90) {
        alert('{rval MSG915}'+ '\n全角30文字までです。');
        obj.focus();
        return;
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_SHDIV.value           = document.forms[0].SHDIV.options[document.forms[0].SHDIV.selectedIndex].value;
    document.forms[0].HID_EXAMCOURSECD.value    = document.forms[0].EXAMCOURSECD.options[document.forms[0].EXAMCOURSECD.selectedIndex].value;
    document.forms[0].HID_EXAMHALLCD.value      = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].SHDIV.disabled            = true;
    document.forms[0].EXAMCOURSECD.disabled     = true;
    document.forms[0].EXAMHALLCD.disabled       = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}
