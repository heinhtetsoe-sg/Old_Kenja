function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'reload'){ //成績参照
        reloadIframe("knje011oindex.php?cmd=reload");
        for (var i = 0; i < document.forms[0]["CHECK\[\]"].length; i++){
            document.forms[0]["CHECK\[\]"][i].checked = false;
        }
        return true;
    }else if (cmd == 'form2_first'){ //特別活動の記録～
        if (document.forms[0].useSyojikou3.value == "1") {
            loadwindow('knje011oindex.php?cmd=form2_first',0,0,730,520);
        } else {
            loadwindow('knje011oindex.php?cmd=form2_first',0,0,670,440);
        }
        return true;
    }else if (cmd == 'form3_first'){ //成績参照
        loadwindow('knje011oindex.php?cmd=form3_first',0,0,600,540);
        return true;
    }else if (cmd == 'form4_first'){ //指導要録参照
        loadwindow('knje011oindex.php?cmd=form4_first',0,0,710,290);
        return true;
    }else if (cmd == 'reset'){ //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function CheckHealth(obj)
{
    var el = obj.value;
    if (obj.checked == true) {
        var val = "異常なし";
        if (el == "TR_REMARK") val = "特記事項なし";
        document.forms[0][el].value = val;
        document.forms[0][el].onfocus = new Function("this.blur()");
    } else {
        document.forms[0][el].value = document.forms[0][el].defaultValue;
        document.forms[0][el].onfocus = new Function("");
    }

}
function reloadIframe(url){
    document.getElementById("cframe").src=url
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order){
   if (document.forms[0].SCHREGNO.value == ""){
       alert('{rval MSG304}');
       return true;
   }
    nextURL = "";
    for (var i = 0; i < parent.left_frame.document.links.length; i++){
          var search = parent.left_frame.document.links[i].search;
          //searchの中身を&で分割し配列にする。
          arr = search.split("&");

          //学籍番号が一致
          if (arr[1] == "SCHREGNO="+schregno){
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length-1){
                idx = 0;                                         //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            }else if (order == 0){
                idx = i+1;                                       //更新後次の生徒へ
            }else if (order == 1 && i == 0){
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            }else if (order == 1){
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

function NextStudent(cd){
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL){
        if(cd == '0'){
                //クッキー削除
                deleteCookie("nextURL");

                document.location.replace(nextURL);
            alert('{rval MSG201}');
        }else if(cd == '1'){
                //クッキー削除
                deleteCookie("nextURL");

        }
    }
}
