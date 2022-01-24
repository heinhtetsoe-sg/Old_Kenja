function btn_submit(cmd) {
    if (cmd == "search") {
        var flg = false;
        for (var i=0;i<document.forms[0].elements.length;i++){
            var e = document.forms[0].elements[i];
            if ((e.type == 'text' || e.type == 'select-one' || e.type == 'select') && e.value != ''){
                flg = true;
                break;
            }
            if (e.type == 'checkbox' && e.checked && e.name == 'GRD_CHECK'){
                flg = true;
                break;
            }
        }
        if (!flg) {
            alert('{rval MSG301}' + '最低一項目を指定してください。');
            return true;
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

var objLink;
var index = 0;
function Link(obj) {
    objLink = obj;
}

function Link2(requestRoot) {
    //PROGRAMIDを半角に KNJW005 → knjw005
    progIndex = document.forms[0].PROGRAMID.value.toLowerCase();
    var linkdata = requestRoot + document.forms[0].right_path.value + '?cmd=search';

    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        //入力・選択された項目をパラメータにセット
        if ((e.type == 'text' || e.type == 'select-one' || (e.type == 'radio' && e.checked)) || (e.type == 'checkbox' && e.checked) && e.value != ''){
            var setVal = (e.name == 'NAME_KANA' || e.name == 'NAME') ? encodeURI(e.value) : e.value;
            linkdata += '&LEFT_' + e.name + '=' + setVal;
        }
    }
    linkdata += '&dumy=0,';
    linkdata += ' target=right_frame';

    parent.right_frame.location.href = linkdata;
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
    }else{
        alert("関数updateNextの引数３の更新ボタンの名前が設定されていません");
        return true;
    }
    return false;
}
function nextLink(){
    document.links[index].click();
}
