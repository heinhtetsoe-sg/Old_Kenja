/* Edit by HPA for current_cursor start 2020/01/20 */
setTimeout(function () {
  window.onload = new function () {
    if (sessionStorage.getItem("KNJD420BForm1_CurrentCursor") == "sortDes") { 
       document.getElementById("table").focus();
      setTimeout(function () {
        document.getElementById("sortAsc").focus();

        }, 3000);
      

    } else if (sessionStorage.getItem("KNJD420BForm1_CurrentCursor") == "sortAsc") {
      document.getElementById("table").focus();
      setTimeout(function () {
        document.getElementById("sortDes").focus();

        }, 3000);
     } else {
      if (sessionStorage.getItem("KNJD420BForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJD420BForm1_CurrentCursor")).focus();
      } else if (sessionStorage.getItem("link_click") == "right_screen") {
        document.getElementById("rightscreen").focus();
        sessionStorage.removeItem('link_click');
      } else {
                document.title = "右情報画面";
            }
      }
    }
}, 800);

function current_cursor(para) {
    sessionStorage.setItem("KNJD420BForm1_CurrentCursor", para);
}

function btn_submit(cmd) {
     /* Add by PP for CurrentCursor 2020-01-20 start */
    if (sessionStorage.getItem("KNJD420BForm1_CurrentCursor") != null) {
         document.title = "";
        document.getElementById(sessionStorage.getItem("KNJD420BForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-01-31 end */

    if (document.forms[0].SCHREGNO.value == ""){
      alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
      document.title = "";
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    } else if (cmd == 'allcopy') {
        if (document.forms[0].SEMESTERCOPY.value == "1") {
          if (!confirm('{rval MSG104}')) {
                return false;
            }
        }
    } else if (cmd == 'copy') {
      if (document.forms[0].SUBCLASSCOPY.value == "1") {
          if (!confirm('{rval MSG104}')) {
                return false;
            }
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
