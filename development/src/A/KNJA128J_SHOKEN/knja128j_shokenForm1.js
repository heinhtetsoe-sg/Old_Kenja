
/* Add by HPA for PC-talker current_cursor start 2020/01/20 */
window.onload = function () {
  if (sessionStorage.getItem("KNJA128J_shokenForm1_CurrentCursor") != null) {
    document.getElementById(sessionStorage.getItem("KNJA128J_shokenForm1_CurrentCursor")).focus();
  } else {
    document.getElementById('screen_id').focus();
  }
}
function current_cursor(para){
  sessionStorage.setItem("KNJA128J_shokenForm1_CurrentCursor", para);
 }
/* Add by HPA for PC-talker current_cursor end 2020/01/31 */
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
