function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
//更新後次の生徒のリンクをクリックする
function updateNext(f, order){
    var nextFlg = false;

    if (!top.main_frame.left_frame.objLink){
        alert('{rval MSG304}');
        return true;
    }
    var a = top.main_frame.left_frame.objLink.pathname.split("/");
    switch(a[a.length-1]){
    case 'knjh160index.php':
        index = 0;
        break;
    case 'knjh160_2index.php':
        index = 1;
        break;
    case 'knjh160_3index.php':
        index = 2;
        break;
    default:
        index = 0;
    }
    for (i = 0; i < top.main_frame.left_frame.document.links.length; i++){
        if (top.main_frame.left_frame.objLink.pathname != top.main_frame.left_frame.document.links[i].pathname){
            continue;
        }
        if (top.main_frame.left_frame.objLink == top.main_frame.left_frame.document.links[i]){
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
    top.main_frame.left_frame.document.links[index].click();
    return false;
}
