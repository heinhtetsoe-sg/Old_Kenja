function btn_submit(cmd) {


    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    if (cmd == 'modify'){ //出欠の記録修正
        var url = "../../X/KNJXATTEND/index.php?SCHREGNO="+ document.forms[0].SCHREGNO.value;
        url += "&mode="+ document.forms[0].mode.value ;
        url += "&GRD_YEAR="+ document.forms[0].GRD_YEAR.value ;
        url += "&PROGRAMID="+ document.forms[0].PROGRAMID.value ;

        loadwindow(url ,0,0,470,480);
        return true;
    }else if (cmd == 'subform1'){ //成績参照
        loadwindow('knje020oindex.php?cmd=subform1',0,0,700,550);
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

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');  //この処理は許可されていません。
    closeWin();
}

//チェックボックス
function CheckHealthRemark(){
    if (document.forms[0].CHECK.checked == true) {
        document.forms[0].jobhunt_healthremark.value ="異常なし";
        document.forms[0].jobhunt_healthremark.disabled =true;
    }else{
        document.forms[0].jobhunt_healthremark.disabled = false;
    }
}

function wopen2(URL,winName,x,y,w,h)
{
    var newWin;
    var para =""
             +" left="        +x
             +",screenX="     +x
             +",top="         +y
             +",screenY="     +y
             +",toolbar="     +0
             +",location="    +0
             +",directories=" +0
             +",status="      +0
             +",menubar="     +0
             +",scrollbars="  +1//------------------------後でゼロに修正
             +",resizable="   +1//-------------------後でゼロに修正
             +",innerWidth="  +w
             +",innerHeight=" +h
             +",width="       +w
             +",height="      +h

    if(sbwin_closed(newWin)){
        newWin = window.open(URL,winName,para);
    }else{
        newWin.location.href=URL
    }
    newWin.focus()
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
