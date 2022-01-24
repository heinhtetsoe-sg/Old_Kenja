function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
        return false;
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function copy(mode){
    if (mode == 1) {
        if (document.forms[0].HID_NOT.value == "1") {
            document.forms[0].GUARD_NAME.value   = document.forms[0].HID_GUARD_NAME.value;
            document.forms[0].GUARD_KANA.value   = document.forms[0].HID_GUARD_KANA.value;
            document.forms[0].GUARD_ZIPCD.value  = document.forms[0].HID_GUARD_ZIPCD.value;
            document.forms[0].GUARD_ADDR1.value  = document.forms[0].HID_GUARD_ADDR1.value;
            document.forms[0].GUARD_ADDR2.value  = document.forms[0].HID_GUARD_ADDR2.value;
            document.forms[0].GUARD_TELNO.value  = document.forms[0].HID_GUARD_TELNO.value;
            document.forms[0].GUARD_TELNO2.value = document.forms[0].HID_GUARD_TELNO2.value;
        } else {
            alert('{rval MSG303}');
            return false;
        }
    }
    if (mode == 2) {
        if (document.forms[0].HID2_NOT.value == "1") {
            document.forms[0].GUARD_NAME.value      = document.forms[0].HID2_GUARD_NAME.value;
            document.forms[0].GUARD_KANA.value      = document.forms[0].HID2_GUARD_KANA.value;
            document.forms[0].GUARD_ZIPCD.value     = document.forms[0].HID2_GUARD_ZIPCD.value;
            document.forms[0].GUARD_ADDR1.value     = document.forms[0].HID2_GUARD_ADDR1.value;
            document.forms[0].GUARD_ADDR2.value     = document.forms[0].HID2_GUARD_ADDR2.value;
            document.forms[0].GUARD_TELNO.value     = document.forms[0].HID2_GUARD_TELNO.value;
            document.forms[0].GUARD_TELNO2.value    = document.forms[0].HID2_GUARD_TELNO2.value;
            document.forms[0].BRANCHCD.value        = document.forms[0].HID2_BRANCHCD.value;
            document.forms[0].BRANCH_POSITION.value = document.forms[0].HID2_BRANCH_POSITION.value;
            document.forms[0].SEND_NAME.value       = document.forms[0].HID2_SEND_NAME.value;
            document.forms[0].RESIDENTCD.value      = document.forms[0].HID2_RESIDENTCD.value;
        } else {
            alert('{rval MSG303}');
            return false;
        }
    }
}
function Page_jumper(jump,sno,nd) {
    var cd;
    var cd2;
    cd = '?SCHREGNO=';
    cd2 = '&NEWAD=';
    if(sno == ''){
        alert('{rval MSG304}');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.right_frame.location.replace(jump + cd + sno + cd2 + nd);
}
