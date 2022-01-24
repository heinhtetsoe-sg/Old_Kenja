
/* Add by HPA for current_cursor and textarea_cursor start 2020/01/20 */
window.onload = function () {
  if (sessionStorage.getItem("KNJA128HForm1_CurrentCursor915") != null) {
    document.getElementsByName(sessionStorage.getItem("KNJA128HForm1_CurrentCursor915"))[0].focus();
    var a = document.getElementsByName(sessionStorage.getItem("KNJA128HForm1_CurrentCursor915"))[0].value;
    document.getElementsByName(sessionStorage.getItem("KNJA128HForm1_CurrentCursor915"))[0].value = "";
    document.getElementsByName(sessionStorage.getItem("KNJA128HForm1_CurrentCursor915"))[0].value = a;
    sessionStorage.removeItem("KNJA128HForm1_CurrentCursor915");
  } else {
    sessionStorage.removeItem("KNJA128HForm1_CurrentCursor915");
    if (sessionStorage.getItem("KNJA128HForm1_CurrentCursor") != null) {
        if (sessionStorage.getItem("KNJA128HForm1_CurrentCursor") == 'btn_up_pre' || sessionStorage.getItem("KNJA128HForm1_CurrentCursor") == 'btn_up_next') {
            document.getElementById("rightscreen").focus();
            setTimeout(function () {
              document.getElementById(sessionStorage.getItem("KNJA128HForm1_CurrentCursor")).focus();
              sessionStorage.clear();
            }, 4000);
            
        } else {
            document.title = "";
          document.getElementById(sessionStorage.getItem("KNJA128HForm1_CurrentCursor")).focus();
          sessionStorage.clear();
        }
       
    } else if (sessionStorage.getItem("link_click") == "right_screen") {
        document.getElementById("rightscreen").focus();
        sessionStorage.removeItem('link_click');
    }
  }  
}
function current_cursor(para){
  sessionStorage.setItem("KNJA128HForm1_CurrentCursor", para);
        if (sessionStorage.getItem("KNJA128HForm1_CurrentCursor")== 'btn_up_pre' || sessionStorage.getItem("KNJA128HForm1_CurrentCursor")== 'btn_up_next') {
            document.getElementById(sessionStorage.getItem("KNJA128HForm1_CurrentCursor")).blur();
        }
 }
function textbox_focus(name) {
  alert(name);
}
function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJA128HForm1_CurrentCursor")).focus();
}
/* Add by HPA for current_cursor and textarea_cursor end 2020/01/31 */
var textRange;
function btn_submit(cmd) {
  /* Add by HPA for CurrentCursor 2020-01-20 start */
    if (sessionStorage.getItem("KNJA128HForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJA128HForm1_CurrentCursor")).blur();
    }
    /* Add by HPA for CurrentCursor 2020-01-31 end */
    if (document.forms[0].SCHREGNO.value == "") {
      alert('{rval MSG304}');
      /* Add by HPA for CurrentCursor 2020-01-20 start */
      document.getElementById(sessionStorage.getItem("KNJA128HForm1_CurrentCursor")).focus();
      /* Add by HPA for CurrentCursor 2020-01-31 end */
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'subform4') {
        loadwindow('knja128hindex.php?cmd=subform4',0,document.documentElement.scrollTop || document.body.scrollTop,700,550);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    nextURL = "";
    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
          var search = parent.left_frame.document.links[i].search;
          //searchの中身を&で分割し配列にする。
          arr = search.split("&");

          //学籍番号が一致
          if (arr[1] == "SCHREGNO="+schregno) {
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length-1) {
                idx = 0;                                         //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i+1;                                       //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i-1;                                       //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href;//上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'update';
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();

    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == '0') {
                //クッキー削除
                deleteCookie("nextURL");
                document.location.replace(nextURL);
            alert('{rval MSG201}');
        } else if (cd == '1') {
                //クッキー削除
                deleteCookie("nextURL");
        }
    }
}

function newwin(SERVLET_URL, GRADE) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    var gradehrclass = GRADE.split('-');
    document.forms[0].GRADE_HR_CLASS.value = gradehrclass[0] + gradehrclass[1];
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
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
