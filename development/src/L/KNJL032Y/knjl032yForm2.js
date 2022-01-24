function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//ボタンを押し下げ不可にする
function btn_disabled() {
    document.forms[0].btn_update.disabled = true;
}
