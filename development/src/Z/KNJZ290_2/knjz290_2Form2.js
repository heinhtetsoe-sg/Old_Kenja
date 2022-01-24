function btn_submit(cmd) {
    
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;

}

function chkSelSameValue() {
    var obj1 = document.forms[0].SHARENAME;
    var obj2 = document.forms[0].SHARENAME2;
    var obj3 = document.forms[0].HID_DUTYSHARECD;
    var obj4 = document.forms[0].HID_DUTYSHARECD_2;
    if (obj1.value == obj2.value && obj1.value != "") {
        alert('{rval MSG302}');
        obj1.value = obj3.value;
        obj2.value = obj4.value;
        return true;
    }
    obj3.value = obj1.value;
    obj4.value = obj2.value;
    return true;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
