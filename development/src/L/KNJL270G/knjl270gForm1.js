function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//欠席チェックON/OFFで背景色を黄色表示
function bgcolorYellow(obj, examno) {
    change_flg = true;

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value      = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_SHDIV.value        = document.forms[0].SHDIV.options[document.forms[0].SHDIV.selectedIndex].value;
    document.forms[0].HID_EXAMCOURSE.value   = document.forms[0].EXAMCOURSE.options[document.forms[0].EXAMCOURSE.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled      = true;
    document.forms[0].SHDIV.disabled        = true;
    document.forms[0].EXAMCOURSE.disabled   = true;

    document.getElementById('ROWID' + examno).style.background = obj.checked ? "yellow" : "white";
}

//全チェック選択（チェックボックスon/off）
function chkDataALL(obj) {

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value      = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_SHDIV.value        = document.forms[0].SHDIV.options[document.forms[0].SHDIV.selectedIndex].value;
    document.forms[0].HID_EXAMCOURSE.value   = document.forms[0].EXAMCOURSE.options[document.forms[0].EXAMCOURSE.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled      = true;
    document.forms[0].SHDIV.disabled        = true;
    document.forms[0].EXAMCOURSE.disabled   = true;

    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "CHK_DATA" && e.disabled == false) {
            document.forms[0].elements[i].checked = obj.checked;
            var examno = document.forms[0].elements[i].id.split('-')[1];
            if (obj.checked) {
                document.getElementById('ROWID' + examno).style.background = "yellow";
            } else {
                document.getElementById('ROWID' + examno).style.background = "white";
            }
        }
    }
}
