/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = function () {
  document.title = "";
  setTimeout(function () {
      if (sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortDesYear" || sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortDesName") {
        var sortId = sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortDesYear" ? "sortAscYear" : "sortAscName";
        var table = sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortDesYear" ? "table1" : "table2";
        document.getElementById(table).focus();
        setTimeout(function () {
          document.getElementById(sortId).focus();
          document.title = title;
        }, 3500);
      } else if (sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortAscYear" || sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortAscName") {
        var sortId = sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortAscYear" ? "sortDesYear" : "sortDesName";
        var table = sessionStorage.getItem("KNJE063BForm1_CurrentCursor") == "sortAscYear" ? "table1" : "table2";
        document.getElementById(table).focus();
        setTimeout(function () {
          document.getElementById(sortId).focus();
          document.title = title;
        }, 3500);
      } else {
        if (sessionStorage.getItem("KNJE063BForm1_CurrentCursor") != null) {
          document.title = "";
          document.getElementById(sessionStorage.getItem("KNJE063BForm1_CurrentCursor")).focus();
          sessionStorage.clear();
          setTimeout(function () {
          document.title = title;
          }, 1000);
        } else if (sessionStorage.getItem("link_click") == "right_screen") {
          document.getElementById("rightscreen").focus();
          sessionStorage.removeItem('link_click');
          document.title = title;
        } else {
          if (sessionStorage.getItem("KNJE063BForm2_CurrentCursor") == null) {
            document.title = "右結果画面";
          } else {
            document.title = title;
          }
      }
      }
    }, 800);
}

function current_cursor(para) {
  sessionStorage.setItem("KNJE063BForm1_CurrentCursor", para);
}
/* Add by HPA for current_cursor end 2020/02/20 */

function btn_submit(cmd) {
    /* Add by HPA for CurrentCursor blur 2020-02-03 start */
    if (sessionStorage.getItem("KNJE063BForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE063BForm1_CurrentCursor")).blur();
    }
    /* Add by HPA for CurrentCursor blur 2020-02-20 end */
  //削除
  if ((cmd == 'delete') && !confirm('{rval MSG103}')) {
    return true;
  } else if (cmd == 'delete') {
    //必須チェック
    if (document.forms[0].chkSCHREGNO.value == '') {
      alert('{rval MSG304}\n(左より生徒を選択してから行ってください)');
      /* Add by HPA for CurrentCursor blur 2020-02-03 start */
      document.getElementById(sessionStorage.getItem("KNJE063BForm1_CurrentCursor")).focus();
      /* Add by HPA for CurrentCursor blur 2020-02-20 end */
      return false;
    }
    //チェックボックス有無チェック
    for (var i = 0; i < document.forms[0].elements.length; i++) {
      if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked) {
        break;
      }
    }
    if (i == document.forms[0].elements.length) {
      alert("チェックボックスを選択してください");
      /* Add by HPA for CurrentCursor blur 2020-02-03 start */
      document.getElementById(sessionStorage.getItem("KNJE063BForm1_CurrentCursor")).focus();
      /* Add by HPA for CurrentCursor blur 2020-02-20 end */
      return true;
    }
  }

  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
}

//行選択
var selectedRow = 0;
function selectRow() {
  if (event.srcElement.parentElement.rowIndex == null) {
    return;
  }
  list.rows[selectedRow].bgColor = "white";
  selectedRow = event.srcElement.parentElement.rowIndex;
  list.rows[selectedRow].bgColor = "#ccffcc";

  var chk = document.forms[0]["CHECKED\[\]"];
  if (chk.length) {
  /* Add by HPA for CurrentCursor 2020-02-03 start */
    sessionStorage.clear();
    parent.bottom_frame.location.href = "knje063bindex.php?cmd=edit&CHECKED=" + chk[selectedRow].value;
    sessionStorage.setItem("rollclick", "rollclick_screen");
  } else if (chk) {
    sessionStorage.clear();
    parent.bottom_frame.location.href = "knje063bindex.php?cmd=edit&CHECKED=" + chk.value;
    sessionStorage.setItem("rollclick", "rollclick_screen");
    /* Add by HPA for CurrentCursor 2020-02-20 end */
  }
}

//チェックボタン（ALLチェック）
function check_all(obj) {
  for (var i = 0; i < document.forms[0].elements.length; i++) {
    if (document.forms[0].elements[i].name == "CHECKED[]" && !document.forms[0].elements[i].disabled) {
      document.forms[0].elements[i].checked = obj.checked;
    }
  }
}
