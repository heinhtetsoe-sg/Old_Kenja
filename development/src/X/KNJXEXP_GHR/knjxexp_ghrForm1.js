/* Add by Kaung for CurrentCursor 2019-01-03 start */
window.onload = function(){
    if(sessionStorage.getItem("KNJXEXP_GHR_CurrentCursor") != null){
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJXEXP_GHR_CurrentCursor")).focus();
        sessionStorage.clear();
    }
    setTimeout(function () {
        document.title = TITLE;
    }, 100);
}
function current_cursor(para){
    sessionStorage.setItem("KNJXEXP_GHR_CurrentCursor",para);
}

function current_cursor_focus(para) {
    document.getElementById(para).focus();
    sessionStorage.clear();
}


/* Add by Kaung for CurrentCursor 2019-01-31 end */

function btn_submit(cmd) {
    /* Add by Kaung for CurrentCursor 2019-01-03 start */
    document.title = "";
    if(sessionStorage.getItem("KNJXEXP_GHR_CurrentCursor") != null){
        document.getElementById(sessionStorage.getItem("KNJXEXP_GHR_CurrentCursor")).blur();
    }
    /* Add by Kaung for CurrentCursor 2019-01-31 end */
    if (cmd == "chg_grade" || cmd == "chg_year" || cmd == "chg_hukusiki_radio" || cmd == "chg_ghr_cd" || cmd == "chg_fi_grade_hr_class"){
        if (cmd == "chg_year") document.forms[0].GRADE.value = "";
        parent.right_frame.location.href = document.forms[0].path.value+'&init=1';
        if (cmd != "chg_hukusiki_radio") {
            cmd = "list";
        }
        document.forms[0].mode.value = "ungrd";
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
function search(f, mode) {
    
    sessionStorage.setItem("KNJXEXP_GHR_CurrentCursor", 'for_search');
    
    for (var i = 0; i < f.elements.length; i++){
        switch(f.elements[i].name){
        case 'GRADE2':
        case 'GRD_YEAR':
        case 'HR_CLASS':
        case 'COURSECODE':
        case 'SRCH_SCHREGNO':
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
var index = 0;
var session_value = '';
function Link(obj) {
/* Add by Kaung for PC-Talker 2019-01-20 start */
    if (session_value != '') {
        if (sessionStorage.key(0) != null) {
          sessionStorage.setItem(sessionStorage.key(0), session_value);
          session_value = '';
        } else {
          sessionStorage.clear();
          sessionStorage.setItem("link_click", "right_screen");
        }
    } else {
        sessionStorage.clear();
        sessionStorage.setItem("link_click", "right_screen");
    }
    /* Add by Kaung for PC-Talker 2019-01-31 end */
    objLink = obj;
}

var nextHref;
//更新後次の生徒のリンクをクリックする
function updateNext(f, order, btn_update){
    var nextFlg = false;
    /* Add by Kaung for PC-Talker 2019-01-20 start */
    session_value = sessionStorage.getItem(sessionStorage.key(0));
    /* Add by Kaung for PC-Talker 2019-01-31 end */
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
