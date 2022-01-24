function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//チェックON/OFFで背景色を黄色表示
function bgcolorYellow(obj, examno) {
    change_flg = true;

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV0.value = document.forms[0].TESTDIV0.options[document.forms[0].TESTDIV0.selectedIndex].value;
    document.forms[0].HID_EXAMCOURSE1.value = document.forms[0].EXAMCOURSE1.options[document.forms[0].EXAMCOURSE1.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTDIV0.disabled = true;
    document.forms[0].EXAMCOURSE1.disabled = true;

    document.getElementById('ROWID' + examno).style.background = "yellow";
}
