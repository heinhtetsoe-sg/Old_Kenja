/* Edit by HPA for PC-talker current_cursor  start 2020/01/20 */
window.onload = new function () {
  if (sessionStorage.getItem("KNJA128JForm2_CurrentCursor") != null) {
    document.title = "";
    setTimeout(function () {
      document.getElementById(sessionStorage.getItem("KNJA128JForm2_CurrentCursor")).focus();
      sessionStorage.removeItem("KNJA128JForm2_CurrentCursor");
    }, 500);
  } else {
    setTimeout(function () {
        document.getElementById('screen_id').focus();
    }, 500);
  }
}
function current_cursor(para) {
  sessionStorage.setItem("KNJA128JForm2_CurrentCursor", para);
}

function current_cursor_focus() {
  document.getElementById(sessionStorage.getItem("KNJA128JForm2_CurrentCursor")).focus();
}
/* Edit by HPA for PC-talker current_cursor end 2020/01/31 */
//サブミット
function btn_submit(cmd) {
  document.getElementById(sessionStorage.getItem("KNJA128JForm2_CurrentCursor")).blur();
  if (cmd == 'clear2') {
    if (!confirm('{rval MSG106}')) {
      return false;
    }
  }
  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
}

//Submitしない
function btn_keypress() {
  if (event.keyCode == 13) {
    event.keyCode = 0;
    window.returnValue = false;
  }
}
