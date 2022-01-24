function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == '') {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    } else if (cmd == 'delete') {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == 'CHECKED[]' && document.forms[0].elements[i].checked) {
                break;
            }
        }
        if (i == document.forms[0].elements.length) {
            alert('チェックボックスを選択してください');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == 'CHECKED[]') {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
