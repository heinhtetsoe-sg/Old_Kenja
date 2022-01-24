function btn_submit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}


function closing_window(flg){
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(HRクラス作成、課程マスタ、学科マスタ、出身学校マスタ、名称マスタのいずれか)');
    }
    closeWin();
    return true;
}
