function btn_submit(cmd) {
    if (cmd == 'educate1_delete' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

window.onload = function(e) {
    var keta = document.forms[0].useFinschoolcdFieldSize.value;
    if (keta == '12' && document.forms[0].P_J_SCHOOL_CD) {
        document.forms[0].P_J_SCHOOL_CD.maxlength = 12;
        document.forms[0].P_J_SCHOOL_CD.size = 12;
    }
};

