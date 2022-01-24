function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//チェックON/OFFで背景色を黄色表示
function bgcolorYellow(obj, schregNo, testCd) {

    document.forms[0].HID_SIKAKUCD.value       = document.forms[0].SIKAKUCD.options[document.forms[0].SIKAKUCD.selectedIndex].value;
    document.forms[0].HID_GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS.options[document.forms[0].GRADE_HR_CLASS.selectedIndex].value;
    document.forms[0].HID_TEST_DATE.value      = document.forms[0].TEST_DATE.value;

    document.getElementById('ROWID' + schregNo + testCd).style.background = obj.checked ? "yellow" : "white";

    var flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "CHK_DATA" && e.disabled == false) {
            if (e.checked) flg = true;
        }
    }

    if (flg) {
        document.forms[0].SIKAKUCD.disabled       = true;
        document.forms[0].GRADE_HR_CLASS.disabled = true;
        document.forms[0].TEST_DATE.disabled      = true;
    } else {
        document.forms[0].SIKAKUCD.disabled       = false;
        document.forms[0].GRADE_HR_CLASS.disabled = false;
        document.forms[0].TEST_DATE.disabled      = false;
    }
}

//全チェック選択（チェックボックスon/off）
function chkDataALL(obj) {

    document.forms[0].HID_SIKAKUCD.value       = document.forms[0].SIKAKUCD.options[document.forms[0].SIKAKUCD.selectedIndex].value;
    document.forms[0].HID_GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS.options[document.forms[0].GRADE_HR_CLASS.selectedIndex].value;
    document.forms[0].HID_TEST_DATE.value      = document.forms[0].TEST_DATE.value;

    var flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "CHK_DATA" && e.disabled == false) {
            document.forms[0].elements[i].checked = obj.checked;
            document.getElementById('ROWID' + nam.split("-")[1] + nam.split("-")[2]).style.background = obj.checked ? "yellow" : "white";
            if (obj.checked) flg = true;
        }
    }

    if (flg) {
        document.forms[0].SIKAKUCD.disabled       = true;
        document.forms[0].GRADE_HR_CLASS.disabled = true;
        document.forms[0].TEST_DATE.disabled      = true;
    } else {
        document.forms[0].SIKAKUCD.disabled       = false;
        document.forms[0].GRADE_HR_CLASS.disabled = false;
        document.forms[0].TEST_DATE.disabled      = false;
    }
}
