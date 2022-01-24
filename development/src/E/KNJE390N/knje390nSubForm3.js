function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    if (cmd == 'subform3_updatemain') {
        //作成年月日チェック
        var getWiringDate = document.forms[0].WRITING_DATE.value;
        var getSDate = document.forms[0].SDATE.value;
        var getEDate = document.forms[0].EDATE.value;
        if (getWiringDate == "") {
           alert('{rval MSG301}' + '\n(作成年月日)');
           return true;
        }
        if (getSDate > getWiringDate) {
           alert('{rval MSG203}' + '\n作成年月日は年度内の日付を指定して下さい。');
           return true;
        }
        if (getEDate < getWiringDate) {
           alert('{rval MSG203}' + '\n作成年月日は年度内の日付を指定して下さい。');
           return true;
        }
    }
    if (cmd == 'subform3_rireki'){
        var getBackupDate = document.forms[0].BACKUP_DATE.value;
        var getSDate = document.forms[0].SDATE.value;
        var getEDate = document.forms[0].EDATE.value;
        if (getBackupDate == "") {
           alert('日付を指定して下さい。');
           return true;
        }
        if (getSDate > getBackupDate) {
           alert('{rval MSG203}' + '\n指定日付は年度内の日付を指定して下さい。');
           return true;
        }
        if (getEDate < getBackupDate) {
           alert('{rval MSG203}' + '\n指定日付は年度内の日付を指定して下さい。');
           return true;
        }
        if (!confirm('参照しているC 支援内容・計画の履歴を取りますか？'+'\n※指定日付で履歴を取ります。')) {
            return false;
        }
    }

    if (cmd == 'subform3_clear'){
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
            nextURL = parent.left_frame.document.links[idx].href.replace("edit","subform3");    //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'subform3_updatemain';
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
