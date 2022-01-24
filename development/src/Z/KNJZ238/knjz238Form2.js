function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].SPECIAL_GROUP_CD.value == '' || document.forms[0].SUBCLASSCD.value == '') {
            alert('{rval MSG301}');
            return false;
        }
        if (document.forms[0].SPECIAL_GROUP_CD.value == '999') {
            alert('特活グループコード999は、使用できません。');
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
