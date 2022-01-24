/* Edit by HPA for PC-talker 読み start 2020/01/20 */
window.onload = function () {
  document.getElementById('screen_id').focus();
}
/* Edit by HPA for PC-talker 読み end 2020/01/31 */
//サブミット
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
