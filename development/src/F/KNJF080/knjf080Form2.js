function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function check(obj) {
    if (getByte(obj.value) > 40) {
        alert('全角２０、半角４０文字以内で入力してください。');
        obj.focus();
    }
}
