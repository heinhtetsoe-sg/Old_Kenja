function btn_submit(cmd) {
    if (cmd == "add") {
        var semester = parent.left_frame.document.forms[0].SEMESTER.value;
        var proficiencydiv = parent.left_frame.document.forms[0].PROFICIENCYDIV.value;
        var proficiencycd = parent.left_frame.document.forms[0].PROFICIENCYCD.value;
        var grade  = parent.left_frame.document.forms[0].GRADE.value;
        document.forms[0].SEMESTER.value  = semester;
        document.forms[0].PROFICIENCYDIV.value  = proficiencydiv;
        document.forms[0].PROFICIENCYCD.value  = proficiencycd;
        document.forms[0].GRADE.value   = grade;
    }
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

function OnAuthError()
{
   alert('{rval MSG300}');
   closeWin();
}

