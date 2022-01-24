//サブミット
/* Edit by HPA for PC-talker 読み start 2020/01/20 */
window.onload = function () {
  if (sessionStorage.getItem("KNJA128HSubForm4_Currentcursor") != null) {
    document.title = "";
    document.getElementById(sessionStorage.getItem("KNJA128HSubForm4_Currentcursor")).focus();
  } else {
    document.getElementById("screen_id").focus();
  }
}
function current_cursor(para) {
  sessionStorage.setItem('KNJA128HSubForm4_Currentcursor',para);
}
/* Edit by HPA for PC-talker 読み end 2020/01/31 */
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
