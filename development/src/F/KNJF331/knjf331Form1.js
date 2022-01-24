function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == 'houkoku' || cmd == 'update') {
        if (document.forms[0].SCHOOLCD.value == "") {
            alert('教育委員会統計用学校番号が、未登録です。');
            return false;
        }
    }
    if (cmd == 'houkoku') {
        if (document.forms[0].FIXED_DATA.value == "") {
            alert('{rval MSG304}'+'(確定データ)');
            return false;
        }
        if (document.forms[0].DOC_NUMBER.value == "") {
            alert('{rval MSG304}'+'(文書番号)');
            return false;
        }
        if (document.forms[0].EXECUTE_DATE.value == "") {
            alert('{rval MSG304}'+'(作成日)');
            return false;
        }
        if (!confirm('{rval MSG108}')) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function closing_window(cd){
    if(cd == 'Rec'){
        alert('{rval MSG305}'+'\n'+'（ 学籍学習記録データ ）');
    }else if(cd == 'Sch'){
        alert('{rval MSG305}'+'\n'+'（ 学籍在籍データ ）');
    }else{
        alert('{rval MSG305}'+'\n'+'（ 評定マスタ ）');
    }
    closeWin();
    return true;
}

function sumNum(obj){
    obj.value=toInteger(obj.value);
    if(obj.value == ''){
        obj.value = 0;
    }
    for(i = 0; i < document.forms[0][obj.name].length; i++){
        if (obj == document.forms[0][obj.name][i]){
            var row = i;
            break;
        }
    }
    var objA = document.forms[0]["A_MEMBER[]"];
    var objB = document.forms[0]["B_MEMBER[]"];
    var objC = document.forms[0]["C_MEMBER[]"];
    var objD = document.forms[0]["D_MEMBER[]"];
    var objE = document.forms[0]["E_MEMBER[]"];

    var a = parseInt(objA[row].value,10);
    var b = parseInt(objB[row].value,10);
    var c = parseInt(objC[row].value,10);
    var d = parseInt(objD[row].value,10);
    var e = parseInt(objE[row].value,10);

    outputLAYER(row, a + b + c + d + e);
}

function getZero(obj){
    obj.value=toInteger(obj.value);
    if(obj.value == ''){
        obj.value = 0;
    }
}

function showConfirm()
{
}

//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

function fixed(REQUESTROOT){

    load  = "loadwindow('"+ REQUESTROOT +"/F/KNJF331/knjf331index.php?cmd=fixedLoad";
    load += "',400,250,450,250)";

    eval(load);
}
