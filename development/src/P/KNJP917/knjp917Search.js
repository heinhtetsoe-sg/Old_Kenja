function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

var objLink;
var index = 0;
function Link(obj) {
    objLink = obj;
}
//右画面に生徒一覧情報を渡す
function sendPrintList(schregNoList) {
    var sortFlg = document.forms[0].HR_CLASS_HYOUJI_FLG.checked == true ? '1': '';

    parent.right_frame.location.href='knjp917index.php?cmd=sendList&sortFlg=' + sortFlg + '&sendList=' + schregNoList + '';
}

//更新後次の生徒のリンクをクリックする
function updateNext(f, order, btn_update){
    var nextFlg = false;

    if (!objLink){
        alert('{rval MSG304}');
        return true;
    }
    var a = objLink.pathname.split("/");
    switch(a[a.length-1]){
    case 'knja110index.php':
        index = 0;
        break;
    case 'knja110_2index.php':
        index = 1;
        break;
    case 'knja110_3index.php':
        index = 2;
        break;
    default:
        index = 0;
    }
    for (i = 0; i < document.links.length; i++){
        if (objLink.pathname != document.links[i].pathname){
            continue;
        }
        if (objLink == document.links[i]){
            if (order == 'pre'){
                break;
            }else if (order == 'next'){
                nextFlg = true;
                index = i;
                continue;
            }
        }
        index = i;
        if (nextFlg){
            break;
        }
    }
    f.document.forms[0]._ORDER.value = order;
    if (f.document.forms[0][btn_update]){
        f.document.forms[0][btn_update].click();
    } else {
        alert("関数updateNextの引数３の更新ボタンの名前が設定されていません");
        return true;
    }
    return false;
}
function nextLink(){
    document.links[index].click();
}
