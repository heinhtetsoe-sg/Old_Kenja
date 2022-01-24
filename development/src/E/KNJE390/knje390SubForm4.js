// Add by PP for loading focus 2020-02-03 start
window.addEventListener("load", function (event) {
    if (sessionStorage.getItem("KNJE390SubForm4_CurrentCursor915") != null) {
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor915"))[0].focus();
        var value = document.getElementsByName(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor915"))[0].value;
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor915"))[0].value = "";
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor915"))[0].value = value;
        sessionStorage.removeItem("KNJE390SubForm4_CurrentCursor915");
    } else {
        sessionStorage.removeItem("KNJE390SubForm4_CurrentCursor915");
        if (sessionStorage.getItem("KNJE390SubForm4_CurrentCursor") != null) {
            document.title = "";
            setTimeout(function () {
            document.getElementById(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJE390SubForm4_CurrentCursor');
            }, 100);
        } else {
            // start loading focus
            document.getElementById('screen_id').focus();
        }
    }
    setTimeout(function () {
            document.title = TITLE; 
    }, 100);
 },false);

function current_cursor(para) {
    sessionStorage.setItem("KNJE390SubForm4_CurrentCursor", para);
}
function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor")).focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubForm4_CurrentCursor'); 
}

 // choice cursor
function current_cursor_choice() {
    document.getElementById('CHALLENGED_NAMES').focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubForm4_CurrentCursor'); 
}

// Add by PP loading focus 2020-02-20 end
function btn_submit(cmd) {
// Add by PP for CurrentCursor 2020-02-03 start 
    if (sessionStorage.getItem("KNJE390SubForm4_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor")).blur();
    }
// Add by PP for CurrentCursor 2020-02-20 end
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    if (cmd == 'subform4_updatemain') {
        //作成年月日チェック
        var getWiringDate = document.forms[0].WRITING_DATE.value;
        if (getWiringDate == "") {
            alert('{rval MSG301}' + '\n(作成年月日)');
            // Add by PP for CurrentCursor 2020-02-03 start 
            document.getElementById(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJE390SubForm4_CurrentCursor');
            // Add by PP for CurrentCursor 2020-02-20 end 
           return true;
        }
    }
    if (cmd == 'subform4_rireki'){
        if (document.forms[0].BACKUP_DATE.value == "") {
            alert('日付を指定して下さい。');
            // Add by PP for CurrentCursor 2020-02-03 start 
            document.getElementById(sessionStorage.getItem("KNJE390SubForm4_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJE390SubForm4_CurrentCursor');
            // Add by PP for CurrentCursor 2020-02-20 end 
           return true;
        }
        if (!confirm('参照しているD 移行支援計画の履歴を取りますか？'+'\n※指定日付で履歴を取ります。')) {
            return false;
        }
    }

    if (cmd == 'subform4_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
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
            }else if (order == 0) {
                idx = i+1;                                       //更新後次の生徒へ
            }else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            }else if (order == 1) {
                idx = i-1;                                       //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href.replace("edit","subform4");    //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'subform4_updatemain';
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {

    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if(cd == '0') {
                //クッキー削除
                deleteCookie("nextURL");
                document.location.replace(nextURL);
            alert('{rval MSG201}');
        }else if(cd == '1') {
                //クッキー削除
                deleteCookie("nextURL");

        }
    }
}
