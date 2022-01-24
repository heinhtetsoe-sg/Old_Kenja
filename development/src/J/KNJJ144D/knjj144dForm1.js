function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkTime(time, obj) {
    if (obj.value >= time) {
        alert('{rval MSG915}');
        obj.value = "";
    }
}