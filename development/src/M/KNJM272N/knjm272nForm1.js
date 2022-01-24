function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == 'add') {
        if (document.forms[0].STAFF.value == '') {
            alert('添削者を指定して下さい。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function checkDate(obj) {
    if (obj.value == '') {
        return true;
    }
    var dateArray = obj.value.split('/');
    var m = dateArray[0];
    var d = dateArray[1];
    var y = parseInt(m) < 4 ? document.forms[0].nextYear.value : document.forms[0].thisYear.value;

    const date = new Date(y, m - 1, d);
    if (y !== String(date.getFullYear()) ||
        ('0' + m).slice(-2) !== ('0' + (date.getMonth() + 1)).slice(-2) ||
        ('0' + d).slice(-2) !== ('0' + date.getDate()).slice(-2)
    ) {
        alert('入力した月日は不正です。(' + obj.value + ')');
        obj.value = '';
        obj.focus();
        return false;
    } else {
        obj.value = ('0' + (date.getMonth() + 1)).slice(-2) + '/' + ('0' + date.getDate()).slice(-2);
        return true;
    }
}
