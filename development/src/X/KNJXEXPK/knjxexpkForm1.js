function btn_submit(cmd) {
    document.forms[0].SCROLLLEFT.value = document.body.scrollLeft;
    document.forms[0].SCROLLTOP.value = document.body.scrollTop;
    if (cmd == "chg_grade"){
        parent.right_frame.location.href = document.forms[0].path.value+'&init=1';
        cmd = "list";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
//右フレームに検索画面
function showSearch(){
    parent.right_frame.location.replace('index.php?cmd=right');
}

//更新後次の生徒のリンクをクリックする
function updateNext(f, order, btn_update){

    f.document.forms[0]._ORDER.value = order;
    if (f.document.forms[0][btn_update]){
       f.document.forms[0][btn_update].click();
    }else{
       alert("関数updateNextの引数３の更新ボタンの名前が設定されていません");
       return true;
    }
    return false;
}
function nextLink(order){
    var NextNo;
    
    var row = parseInt(document.forms[0].ROW.value);
    var maxrow = parseInt(document.forms[0].MAXROW.value);
    var disp = document.forms[0].DISP.value;
    
    if (order == "pre" && row > 0){
        NextNo = row-1;
    }else if (order == "next" && row < maxrow-1){
        NextNo = row+1;
    }else{
        NextNo = row;
    }
    for (i = 0; i < document.links.length; i++){
        if (document.links[i].id == disp+"_"+NextNo) {
            document.links[i].click();
            break;
        }
    }
}
