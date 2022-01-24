function btn_submit(cmd) {
    if (cmd == 'reset'){
        if(!confirm('{rval MSG106}')){
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

//一人の生徒が同じ科目を複数選択しないようにチェック
function check_subclasscd(obj) {
    target_obj_array = obj.name.split('_');
    target_obj_subclasscd = target_obj_array[3]

    var list_table  = document.getElementById('list_table'); //対象のテーブル
    var rowIndex_no = obj.parentNode.parentNode.rowIndex;    //何行目なのか

    for (var i = 0; i < list_table.rows[rowIndex_no].cells.length; i++) {
        target_check = list_table.rows[rowIndex_no].cells[i].firstChild;
        if (target_check.name.match(target_obj_subclasscd)) { //同じ科目コードなら
            if (target_check.name == obj.name) { //クリックしたオブジェクト自身の時はスルー
                continue;
            }
            if (target_check.checked == true && obj.checked == true) {
                alert('その科目は既に選ばれています。');
                obj.checked = false;
                return;
            }
        }
    }
}
