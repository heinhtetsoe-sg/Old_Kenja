function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function search(f){
    for (var i = 0; i < f.elements.length; i++){
        switch(f.elements[i].name){
        case 'GRD_YEAR':
        case 'GRADE_HR_CLASS':
        case 'COURSECODE':
        case 'S_SCHREGNO':
        case 'NAME':
        case 'NAME_SHOW':
        case 'NAME_KANA':
        case 'SEX':
            document.forms[0][f.elements[i].name].value = f.elements[i].value;
        }
    }
    document.forms[0].cmd.value = 'search';
    document.forms[0].submit();
}

//チェック
function closing_window(flg) {
    //権限チェック
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    //事前チェック
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(HRクラス作成、課程マスタ、学科マスタ、出身学校マスタ、名称マスタのいずれか)');
    }
    closeWin();
    return true;
}

//右フレームに検索画面
function showSearch(){
    parent.right_frame.location.href='knji130index.php?cmd=right';
}
