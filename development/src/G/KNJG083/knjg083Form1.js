function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    
    if (cmd == 'update') {
        if (document.forms[0].GROUP_DIV.value == "") {
            alert('{rval MSG304}' + '\n(グループ)');
            return false;
        }
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

function checkAll(CheckVal, counter) {
    var AllCheckFlg = CheckVal.checked;
    if (AllCheckFlg) {
        document.forms[0]["DIV1_REMARK2_" + counter].checked = true;
        document.forms[0]["DIV1_REMARK3_" + counter].checked = true;
        document.forms[0]["DIV1_REMARK4_" + counter].checked = true;
        document.forms[0]["DIV2_REMARK1_" + counter].checked = true;
        document.forms[0]["DIV2_REMARK2_" + counter].checked = true;
        document.forms[0]["DIV2_REMARK3_" + counter].checked = true;
        document.forms[0]["DIV2_REMARK4_" + counter].checked = true;
    } else {
        document.forms[0]["DIV1_REMARK2_" + counter].checked = false;
        document.forms[0]["DIV1_REMARK3_" + counter].checked = false;
        document.forms[0]["DIV1_REMARK4_" + counter].checked = false;
        document.forms[0]["DIV2_REMARK1_" + counter].checked = false;
        document.forms[0]["DIV2_REMARK2_" + counter].checked = false;
        document.forms[0]["DIV2_REMARK3_" + counter].checked = false;
        document.forms[0]["DIV2_REMARK4_" + counter].checked = false;
    }
}