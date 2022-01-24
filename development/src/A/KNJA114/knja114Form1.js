function btn_submit(cmd) {
    if (cmd == 'list') {
        parent.right_frame.location.href='knja114index.php?cmd=edit&chFlg=1&LEFT_GRADE_HR_CLASS=' + document.forms[0].LEFT_GRADE_HR_CLASS.value;
    }

    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
function goEnter(obj){
    if (window.event.keyCode==13) {
        obj.blur();
        return false;
    }
}
