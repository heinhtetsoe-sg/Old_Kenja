function btn_submit(cmd) {
/*    if (cmd == "chg_grade" || cmd == "chg_year"){
        if (cmd == "chg_year") document.forms[0].GRADE.value = "";
        parent.right_frame.location.href = document.forms[0].path.value+'&init=1';
        cmd = "list";
        document.forms[0].mode.value = "ungrd";
    }*/
    
    if(cmd == "search"){
        if(document.forms[0].GAKUSEKINO.value == ''
           && document.forms[0].GRADE.value == ''
           && document.forms[0].COURSE.value == ''
           && document.forms[0].NAME.value == ''
           && document.forms[0].NAME_KANA.value == '')
           {
            
            alert('検索項目を入力してください。');
            return false;
           }
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
function search(f,mode){
    for (var i = 0; i < f.elements.length; i++){
        switch(f.elements[i].name){
        case 'GRADE2':
        case 'GRD_YEAR':
        case 'HR_CLASS':
        case 'COURSECODE':
        case 'NAME':
        case 'NAME_SHOW':
        case 'NAME_KANA':
        case 'NAME_ENG':
        case 'SEX':
        case 'DATE':
            document.forms[0][f.elements[i].name].value = f.elements[i].value;
        }
    }
    document.forms[0].cmd.value = 'search';
    document.forms[0].mode.value = mode;
    document.forms[0].submit();
}
function check_all(obj){
    var flg = obj.checked;

    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type=='checkbox' && e.name != "chk_all"){
            e.checked = flg;
        }
    }
}
//右フレームに検索画面
function showSearch(mode){
    parent.right_frame.location.href='index.php?cmd=right&mode='+mode;
}

var objLink;
var objSch;
var index = 0;
function Link(obj){
    objLink = obj;
}

function Link2(obj, schregNo){
    if (document.forms[0].changeFlg.value == "1") {
        if (!confirm('{rval MSG107}')){
            parent.left_frame.location.href = "javascript:void(0);";
        } else {
            document.forms[0].changeFlg.value = "";
            parent.right_frame.location.href = document.forms[0]["linkVal" + schregNo].value;
            objLink = document.forms[0]["linkVal" + schregNo].value;
            objSch = schregNo;
        }
    } else {
        parent.right_frame.location.href = document.forms[0]["linkVal" + schregNo].value;
        objLink = document.forms[0]["linkVal" + schregNo].value;
        objSch = schregNo;
    }
}

var nextHref;
//更新後次の生徒のリンクをクリックする
function updateNext(f, order, btn_update){
    if (document.forms[0].PROGRAMID.value == "KNJA120A") {
        updateNext2(f, order, btn_update);
    } else {
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
    }
    return false;
}

//更新後次の生徒のリンクをクリックする
function updateNext2(f, order, btn_update){
    var nextFlg = false;
    document.forms[0].setOrder.value = order;

    if (objLink.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    index = 0;
    for (i = 0; i < document.links.length; i++){
        if (document.forms[0]["linkCnt" + objSch].value != i){
            continue;
        }
        nextFlg = true;
        index = i;
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

//次の生徒のリンクをクリックする
function nextStudentOnly(order){
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
                nextLink();
                break;
            }else if (order == 'next'){
                nextFlg = true;
                index = i;
                continue;
            }
        }
        index = i;
        if (nextFlg){
            nextLink();
            break;
        }
    }
    return false;
}
