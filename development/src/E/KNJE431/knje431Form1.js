function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == 'houkoku') {
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
    
    //CSV
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }

        if (document.forms[0].OUTPUT[1].checked) {
            cmd = 'uploadCsv';
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = 'downloadCsv';
        } else if (document.forms[0].OUTPUT[0].checked) {
            cmd = 'downloadError';
        } else {
            alert('ラジオボタンを選択してください。');
            return false;
        }
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

function changeRadio(obj) {
    var type_file;
    if (obj.value == '1') { //1は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById('type_file'); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}
