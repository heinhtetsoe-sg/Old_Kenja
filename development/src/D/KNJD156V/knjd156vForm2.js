function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
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

function OnAuthError() {
   alert('{rval MSG300}');
   closeWin();
}

window.onload = function() {
    document.forms[0].SEMESTER.value = parent.left_frame.document.forms[0].SEMESTER.value;
    document.forms[0].GRADE.value    = parent.left_frame.document.forms[0].GRADE.value;
}

