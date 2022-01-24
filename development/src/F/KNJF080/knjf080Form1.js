function btn_submit(cmd) {
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
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
