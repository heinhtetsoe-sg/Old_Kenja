/* Add by HPA for current_cursor start 2020/02/03 */
setTimeout(function () {
  window.onload = new function () {
    document.title = title;
    if (sessionStorage.getItem("KNJD425_6Form1_CurrentCursor") == "sortDes") { 
       document.getElementById("table").focus();
      setTimeout(function () {
        document.getElementById("sortAsc").focus();
        }, 3000);

    } else if (sessionStorage.getItem("KNJD425_6Form1_CurrentCursor") == "sortAsc") {
      document.getElementById("table").focus();
      setTimeout(function () {
        document.getElementById("sortDes").focus();
        }, 3000);
     } else {
      if (sessionStorage.getItem("KNJD425_6Form1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJD425_6Form1_CurrentCursor")).focus();
        setTimeout(function () {
          document.title = title;
          }, 1000);

      } else {
                document.getElementById('rightscreen').focus();
            }
      }
    }
}, 800);

document.addEventListener('focusin', function () {
  if (document.activeElement.value) {
    var value = document.activeElement.value;
    document.activeElement.value = "";
    document.activeElement.value = value;
  }
}, true);

function current_cursor(para) {
    sessionStorage.setItem("KNJD425_6Form1_CurrentCursor", para);
}
/* Add by HPA for current_cursor end 2020/02/20 */

function btn_submit(cmd) {
/* Add by HPA for current_cursor start 2020/02/03 */
  if (sessionStorage.getItem("KNJD425_6Form1_CurrentCursor") != null) {
    document.title = "";
        document.getElementById(sessionStorage.getItem("KNJD425_6Form1_CurrentCursor")).blur();
      }
/* Add by HPA for current_cursor end 2020/02/20 */
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    var i;
    var radio2;
    //必須チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    radio2 = parent.left_frame.document.getElementById("HUKUSIKI_RADIO2");
    if (radio2 && radio2.checked) {
        document.forms[0].SELECT_GHR.value = "1";
    } else {
        document.forms[0].SELECT_GHR.value = "";
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
  document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
