function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Setflg(obj) {
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_EXAMCOURSE.value = document.forms[0].EXAMCOURSE.options[document.forms[0].EXAMCOURSE.selectedIndex].value;
    document.forms[0].HID_SHDIV.value = document.forms[0].SHDIV.options[document.forms[0].SHDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].EXAMCOURSE.disabled = true;
    document.forms[0].SHDIV.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background = "yellow";
    obj.style.background = "yellow";
}
//合否を表示
function setName(obj, rowid, flg) {
    var idx = obj.value;
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('JUDGEMENT_NAME' + rowid, '');
        }
        return;
    }
    if (flg == '0') {
        if (typeof judgement_name[idx] != "undefined") {
            outputLAYER('JUDGEMENT_NAME' + rowid, judgement_name[idx]);
        } else {
            alert('{rval MSG901}');
            outputLAYER('JUDGEMENT_NAME' + rowid, '');
            obj.value = '';
        }
    }
}
