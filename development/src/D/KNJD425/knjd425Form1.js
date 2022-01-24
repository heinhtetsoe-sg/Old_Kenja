/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = function () {
  document.title += "の情報画面";
  if (sessionStorage.getItem("KNJD425Form1_CurrentCursor") != null) {
    document.getElementById(sessionStorage.getItem("KNJD425Form1_CurrentCursor")).focus();
    sessionStorage.clear();
  } else if (sessionStorage.getItem("link_click") == "right_screen") {
    document.getElementById("rightscreen").focus();
    sessionStorage.removeItem('link_click');
  } else {
    document.title = "右情報画面";
  }
}

function current_cursor(para) {
    sessionStorage.setItem("KNJD425Form1_CurrentCursor", para);
}

function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJD425Form1_CurrentCursor")).focus();
    sessionStorage.clear();
}
/* Add by HPA for current_cursor end 2020/02/20 */

var textRange;
function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } 

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chksetdate() {
    if (document.forms[0].USEKNJD425DISPUPDDATE.value == "1") {
        if (document.forms[0].UPDDATE.value == "" || (document.forms[0].UPDDATE.value == "9999/99/99" && document.forms[0].SELNEWDATE.value == "")) {
            alert('{rval MSG304}');
            return false;
        }
    }
    return true;
}
