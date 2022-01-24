function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkDate(obj) {
    if (obj.value.length > 0) {
        if (obj.value == 0 || obj.value > 31) {
            alert('引き落とし日付の指定が間違っています。(1～31まで)');
            obj.focus();
        } else {
            defVal = toInteger(obj.value);
            if (defVal != obj.value) {
                obj.value = defVal;
                obj.focus();
            }
        }
    }
}

function checkedMethod(obj) {
    for (var monthVal = 4; monthVal <= 15; monthVal++) {
        setMonth = monthVal > 12 ? monthVal - 12 : monthVal;
        document.forms[0]["COLLECT_MONTH_" + setMonth].value = obj.value;
    }
}
