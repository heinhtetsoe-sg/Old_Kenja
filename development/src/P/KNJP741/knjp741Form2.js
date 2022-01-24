function btn_submit(cmd) {
    if (cmd == 'insert' || cmd == 'update' || cmd == 'delete') {
        if (document.forms[0].checkSchreg.value == '') {
            alert('{rval MSG304}' + '\n(左より生徒を選択してから行ってください)');
            return false;
        }
        if (cmd == 'update' || cmd == 'delete') {
            if (document.forms[0].REPAY_SLIP_NO.value == '') {
                alert('{rval MSG304}' + '\n(上段のリストより選択してから行ってください)');
                return false;
            }
        }
        if (cmd == 'insert' || cmd == 'update') {
            if (document.forms[0].REPAY_DATE.value == '' || document.forms[0].REPAY_MONEY.value == '') {
                alert('{rval MSG301}');
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
