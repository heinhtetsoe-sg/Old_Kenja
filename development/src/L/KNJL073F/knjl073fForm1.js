function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == 'update') {
        if (document.forms[0].JUDGEDIV.value == '1' || document.forms[0].JUDGEDIV.value == '3') {
            if (document.forms[0].SUC_COURSE.value == '') {
                alert('合格コースを指定して下さい。');
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//欠席チェックON/OFFで背景色を黄色表示
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

    document.getElementById('ROWID' + examno).style.background = obj.checked ? "yellow" : "white";
}

//全チェック選択（チェックボックスon/off）
function chkDataALL(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "CHK_DATA" && e.disabled == false) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
