//サブミット
/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = new function () {
  setTimeout(function () {
    document.title = title;
      if (sessionStorage.getItem("KNJD425_4Form1_CurrentCursor915") != null) {
        document.getElementById(sessionStorage.getItem("KNJD425_4Form1_CurrentCursor915")).focus();
        var value = document.getElementsByName(sessionStorage.getItem("KNJD425_4Form1_CurrentCursor915"))[0].value;
        document.getElementsByName(sessionStorage.getItem("KNJD425_4Form1_CurrentCursor915"))[0].value = "";
        document.getElementsByName(sessionStorage.getItem("KNJD425_4Form1_CurrentCursor915"))[0].value = value;
        sessionStorage.removeItem("KNJD425_4Form1_CurrentCursor915");
      } else {
        sessionStorage.removeItem("KNJD425_4Form1_CurrentCursor915");
        if (sessionStorage.getItem("KNJD425_4Form1_CurrentCursor") != null) {
          document.getElementById(sessionStorage.getItem("KNJD425_4Form1_CurrentCursor")).focus();
          sessionStorage.removeItem("KNJD425_4Form1_CurrentCursor");
        } else {
          document.getElementById('rightscreen').focus();
        }
      }
    }, 800);
}

document.addEventListener('focusin', function () {
  if (document.activeElement.value) {
    var value = document.activeElement.value;
    document.activeElement.value = "";
    document.activeElement.value = value;
  }
}, true);

function current_cursor(para) {
    sessionStorage.setItem("KNJD425_4Form1_CurrentCursor", para);
}

/* Add by HPA for current_cursor end 2020/02/20 */
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
