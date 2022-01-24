function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    
    if (cmd == 'update') {
        if (document.forms[0].GRADE_HR_CLASS.value == "") {
            alert('{rval MSG304}' + '\n(年組)');
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function checkRemark(counter) {
    if(document.forms[0]["YDAT_REMARK1_" + counter].checked == true){
        document.forms[0]["YDAT_REMARK10_" + counter].value = document.forms[0]["HID_YDAT_REMARK10_" + counter].value;
        document.forms[0]["YDAT_REMARK10_" + counter].disabled = false;
    } else {
        document.forms[0]["YDAT_REMARK10_" + counter].value = '';
        document.forms[0]["YDAT_REMARK10_" + counter].disabled = true;
    }
}