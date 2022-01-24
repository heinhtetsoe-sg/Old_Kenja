function btn_submit(cmd) {
    var schregno = document.forms[0].SCHREGNO;
    var seq = '';
    if (cmd == 'update') {
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/CHECK/)) {
                if (document.forms[0][i].checked) {
                    schregno.value += seq + document.forms[0][i].value;
                    seq = ',';
                }
            }
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm('{rval MSG106}'))
        return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function chenge_color(obj) {
    if (obj.checked) {
        obj.parentNode.parentNode.style.backgroundColor = '#ccffcc';
    } else {
        obj.parentNode.parentNode.style.backgroundColor = '#ffffff';
    }
}
