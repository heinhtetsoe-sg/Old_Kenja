function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'form2'){
        loadwindow('knja125pindex.php?cmd=form2',0,0,600,650);
        return true;
    } else if (cmd == 'subform1'){
        if(document.forms[0].useKnja125pBehaviorSemesMst.value == 2) {
            loadwindow('knja125pindex.php?cmd=subform1',0,0,540,700);
        } else {
            if(document.forms[0].SEMES_CNT.value == 3) {
                loadwindow('knja125pindex.php?cmd=subform1',0,200,750,400);
            } else {
                loadwindow('knja125pindex.php?cmd=subform1',0,250,750,320);
            }
        }
        return true;
    } else if (cmd == 'subform2'){
        loadwindow('knja125pindex.php?cmd=subform2',0,200,750,280);
        return true;
    } else if (cmd == 'teikei') {
        loadwindow('knja125pindex.php?cmd=teikei', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 450);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
            } else if (order == 0){
                idx = i+1;                                       //更新後次の生徒へ
            } else if (order == 1 && i == 0){
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1){
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
        } else if(cd == '1'){
                //クッキー削除
                deleteCookie("nextURL");

        }
    }
}

function dataSelectCheck() {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return false;
    }
    return true;
}