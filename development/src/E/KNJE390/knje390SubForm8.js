// Add by PP for loading focus 2020-02-03 start
window.onload = function () {
    if (sessionStorage.getItem("KNJE390SubForm8_CurrentCursor915") != null) {
        // textbox 915 error
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm8_CurrentCursor915"))[0].focus();
        var value = document.getElementsByName(sessionStorage.getItem("KNJE390SubForm8_CurrentCursor915"))[0].value;
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm8_CurrentCursor915"))[0].value = "";
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm8_CurrentCursor915"))[0].value = value;
        sessionStorage.removeItem("KNJE390SubForm8_CurrentCursor915");
    } else {
        sessionStorage.removeItem("KNJE390SubForm8_CurrentCursor915");
        if (sessionStorage.getItem("KNJE390SubForm8_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE390SubForm8_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJE390SubForm8_CurrentCursor');
        } else {
            // start loading focus
            document.getElementById('screen_id').focus();
        }
    }
    setTimeout(function () {
            document.title = TITLE; 
    }, 100);
}

function current_cursor(para) {
    sessionStorage.setItem("KNJE390SubForm8_CurrentCursor", para);
}
// Add by PP loading focus 2020-02-20 end
function btn_submit(cmd) {
    // Add by PP for CurrentCursor 2020-02-03 start 
    if (sessionStorage.getItem("KNJE390SubForm8_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE390SubForm8_CurrentCursor")).blur();
    }
    // Add by PP for CurrentCursor 2020-02-20 end 

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    if (cmd == 'subform8_formatnew') {
        if (!confirm('{rval MSG108}')) {
            return false;
        }
    }
    if (cmd == 'subform8_updatemain') {
        //作成年月日チェック
        var getWiringDate = document.forms[0].WRITING_DATE.value;
        var getSDate = document.forms[0].SDATE.value;
        var getEDate = document.forms[0].EDATE.value;
        if (getWiringDate == "") {
            alert('{rval MSG301}' + '\n(作成年月日)');
            // Add by PP for date focus 2020-02-03 start
            document.getElementById(sessionStorage.getItem("KNJE390SubForm8_CurrentCursor")).focus();
            // Add by PP date focus 2020-02-20 end
           return true;
        }
        if (getSDate > getWiringDate) {
            alert('{rval MSG203}' + '\n作成年月日は年度内の日付を指定して下さい。');
            // Add by PP for date focus 2020-02-03 start
            document.getElementsByName('btn_calen')[0].focus();
            // Add by PP date focus 2020-02-20 end
           return true;
        }
        if (getEDate < getWiringDate) {
            alert('{rval MSG203}' + '\n作成年月日は年度内の日付を指定して下さい。');
            // Add by PP for date focus 2020-02-03 start
            document.getElementsByName('btn_calen')[0].focus();
            // Add by PP date focus 2020-02-20 end
           return true;
        }
        //新規作成時チェック
        var getNewFlg = document.forms[0].NEW_FLG.value;
        if (getNewFlg == "1") {
            if (!confirm('{rval MSG102}' + '\n同一の作成年月日のデータがある場合は上書き更新されます。')) {
                return false;
            }
        }
        //時間チェック
        var getShour    = document.forms[0].MEETING_SHOUR.value;
        var getSminutes = document.forms[0].MEETING_SMINUTES.value;
        var getEhour    = document.forms[0].MEETING_EHOUR.value;
        var getEminutes = document.forms[0].MEETING_EMINUTES.value;
        if ((getShour != "" && getSminutes == "") || (getShour == "" && getSminutes != "") ||
            (getEhour != "" && getEminutes == "") || (getEhour == "" && getEminutes != "")) {
           alert('{rval MSG203}' + '\n時間の指定が不正です。');
           return true;
        }
        if ((getShour > getEhour) && (getShour != "" && getEhour != "")) {
           alert('{rval MSG203}' + '\n時間の指定が不正です。');
           return true;
        } else if (getShour == getEhour) {
            if (getSminutes > getEminutes) {
               alert('{rval MSG203}' + '\n時間の指定が不正です。');
               return true;
            }
        }
    }
    if (cmd == 'subform8_clear'){
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
            nextURL = parent.left_frame.document.links[idx].href.replace("edit","subform8");    //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'subform8_updatemain';
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
