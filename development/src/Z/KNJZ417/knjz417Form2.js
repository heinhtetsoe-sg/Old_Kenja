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
        if (document.forms[0].JOBTYPE_LCD.value == '' || document.forms[0].JOBTYPE_MCD.value == '') {
            alert('{rval MSG301}');
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

function check_cd(obj) {
    if (obj.name == 'JOBTYPE_LCD') {
        if (obj.value.match(/[^0-9a-zA-Z]/)) {
            alert('半角英数字を入力してください。');
            obj.value = '';
        }
    } else if (obj.name == 'JOBTYPE_MCD') {
        if (obj.value.match(/[\D]/)) {
            alert('半角数字を入力してください。');
            obj.value = '';
        }
    }
}

