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

    document.getElementById('ROWID' + examno).style.background = obj.checked ? "yellow" : "white";

    var flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "CHK_DATA" && e.disabled == false) {
            if (e.checked) flg = true;
        }
    }
    if (flg) {
        document.forms[0].APPLICANTDIV.disabled = true;
        document.forms[0].TESTDIV.disabled = true;
    } else {
        document.forms[0].APPLICANTDIV.disabled = false;
        document.forms[0].TESTDIV.disabled = false;
    }
}

//全チェック選択（チェックボックスon/off）
function chkDataALL(obj) {
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    var flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "CHK_DATA" && e.disabled == false) {
            document.forms[0].elements[i].checked = obj.checked;
            document.getElementById('ROWID' + nam.split("-")[1]).style.background = obj.checked ? "yellow" : "white";
            if (obj.checked) flg = true;
        }
    }

    if (flg) {
        document.forms[0].APPLICANTDIV.disabled = true;
        document.forms[0].TESTDIV.disabled = true;
    } else {
        document.forms[0].APPLICANTDIV.disabled = false;
        document.forms[0].TESTDIV.disabled = false;
    }
}
