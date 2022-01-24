/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = new function () {
  document.title = "";
  setTimeout(function () {
    if (sessionStorage.getItem("KNJE063BForm2_CurrentCursor915") != null) {
      document.getElementById(sessionStorage.getItem("KNJE063BForm2_CurrentCursor915")).focus();
      var value = document.getElementsByName(sessionStorage.getItem("KNJE063BForm2_CurrentCursor915"))[0].value;
      document.getElementsByName(sessionStorage.getItem("KNJE063BForm2_CurrentCursor915"))[0].value = "";
      document.getElementsByName(sessionStorage.getItem("KNJE063BForm2_CurrentCursor915"))[0].value = value;
      sessionStorage.removeItem("KNJE063BForm2_CurrentCursor915");
      setTimeout(function () {
          document.title = title;
          }, 1000);
    } else {
      sessionStorage.removeItem("KNJE063BForm2_CurrentCursor915");
      if (sessionStorage.getItem("KNJE063BForm2_CurrentCursor") != null) {
        document.title = "";
        if (document.getElementById(sessionStorage.getItem("KNJE063BForm2_CurrentCursor")).disabled) {
          parent.top_frame.document.getElementById('rightscreen').focus();
        } else {
          document.getElementById(sessionStorage.getItem("KNJE063BForm2_CurrentCursor")).focus();
          setTimeout(function () {
            document.title = title;
          }, 1000);
        }
      } else if (sessionStorage.getItem("rollclick") == "rollclick_screen") {
        document.getElementById('rollclick_screen').focus();
        sessionStorage.clear();
        document.title = title;
      }else {
        document.title = title;
      }
    }
  }, 800);
}

function current_cursor(para) {
  sessionStorage.setItem("KNJE063BForm2_CurrentCursor", para);
}
function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJE063BForm2_CurrentCursor")).focus();
}
/* Add by HPA for current_cursor end 2020/02/20 */

function btn_submit(cmd) {
/* Add by HPA for current_cursor start 2020/02/03 */
  if (sessionStorage.getItem("KNJE063BForm2_CurrentCursor") != null) {
    document.getElementById(sessionStorage.getItem("KNJE063BForm2_CurrentCursor")).blur();
    document.title = "";
    sessionStorage.removeItem("KNJE063BForm1_CurrentCursor");
  }
  /* Add by HPA for current_cursor end 2020/02/20 */
    //必須チェック
    if (cmd == 'update' || cmd == 'delete2' || cmd == 'subform1' || cmd == 'subform1_2') {
        if (document.forms[0].chkSCHREGNO.value == '') {
            alert('{rval MSG304}\n(左より生徒を選択してから行ってください)');
            return false;
        }
        if (document.forms[0].YEAR.value == '') {
            alert('{rval MSG301}\n　　　　　　　　( 年度 )');
            return false;
        }
        if (cmd == 'subform1_2') {
            if (document.forms[0].SUBCLASSCD_SEQ001.value == '') {
                alert('{rval MSG301}\n　　　　　　( 科目コード )');
                return false;
            }
        } else {
            if (document.forms[0].SUBCLASSCD.value == '') {
                alert('{rval MSG301}\n　　　　　　( 科目コード )');
                return false;
            }
        }
    }

    //削除
    if (cmd == 'delete2' && !confirm('{rval MSG103}')) {
        return true;
    }

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //指導計画参照
    if (cmd == 'subform1' || cmd == 'subform1_2') {
        param = document.forms[0].YEAR.value+','+document.forms[0].SUBCLASSCD.value;
        if (cmd == 'subform1') {
            param = document.forms[0].YEAR.value+','+document.forms[0].SUBCLASSCD.value;
        } else {
            param = document.forms[0].YEAR.value+','+document.forms[0].SUBCLASSCD_SEQ001.value;
        }
        loadwindow('knje063bindex.php?cmd=subform1&TO_DATA='+param,0,document.documentElement.scrollTop || document.body.scrollTop, 400, 420);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
/* Add by HPA for current_cursor end 2020/02/20 */
//年度追加
function add() {
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].YEAR.length;
    var w = document.forms[0].year_add.value;

    if (w == "") {
      alert('{rval MSG901}\n数字を入力してください。');
      /* Add by HPA for current_cursor start 2020/02/03 */
      document.getElementById('year_add').focus();
      /* Add by HPA for current_cursor end 2020/02/20 */
        return false;
    }

    for (var i = 0; i < v; i++) {
        if (w == document.forms[0].YEAR.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].YEAR.options[v] = new Option();
    document.forms[0].YEAR.options[v].value = w;
    document.forms[0].YEAR.options[v].text = w;

    for (var i = 0; i < document.forms[0].YEAR.length; i++) {
        temp1[i] = document.forms[0].YEAR.options[i].value;
        tempa[i] = document.forms[0].YEAR.options[i].text;
    }

    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    //generating new options
    ClearList(document.forms[0].YEAR,document.forms[0].YEAR);
    if (temp1.length > 0) {
        for (var i = 0; i < temp1.length; i++) {
            document.forms[0].YEAR.options[i] = new Option();
            document.forms[0].YEAR.options[i].value = temp1[i];
            document.forms[0].YEAR.options[i].text =  tempa[i];
            if (w == temp1[i]) {
                document.forms[0].YEAR.options[i].selected=true;
            }
        }
    }
    btn_submit('add_year');
}

function subclasscdSeq001Changed() {
    if (document.forms[0].YEAR.value == '' || document.forms[0].SUBCLASSCD_SEQ001.value == '') {
        document.forms[0].btn_subform1_2.disabled = true;
    } else {
        document.forms[0].btn_subform1_2.disabled = false;
    }
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].chkSCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }

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
