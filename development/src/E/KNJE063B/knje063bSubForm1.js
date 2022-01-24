/* Edit by HPA for PC-talker 読み start 2020/02/03 */
window.onload = function () {
  if (sessionStorage.getItem("KNJE063BForm2_CurrentCursor") == "btn_subform1") {
    document.getElementById('screen-id1').focus();
  } else {
    document.getElementById('screen-id1_2').focus();
  }
}
/* Edit by HPA for PC-talker 読み end 2020/02/20 */
//サブミット
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
