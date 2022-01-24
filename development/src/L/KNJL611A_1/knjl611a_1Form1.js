function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function dataCheck(obj, checkVal, namecd) {
    if (obj.value == '') {
        return true;
    }
    if (obj.name == 'TOTAL5') {
        if (0 <= obj.value && obj.value <= 25) {
            return true;
        } else {
            alert('入力値が不正です。入力可能な値【0～25】');
            obj.value = '';
            obj.focus();
            return false;
        }
    }
    if (checkVal == '') {
        alert('名称マスタ【' + namecd + '】が未設定です。');
    }
    var checkArray = checkVal.split(",");
    if (checkArray.indexOf(obj.value) < 0) {
        alert('入力値が不正です。入力可能な値【' + checkVal +'】');
        obj.value = '';
        obj.focus();
    }
}
//戻る
function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
