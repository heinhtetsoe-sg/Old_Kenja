function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    if (cmd == 'delete' && !confirm('{rval MSG103}'+'\n\n注意：この生徒の関連データも全て削除されます！')){
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

function Page_jumper(link)
{
    if (document.forms[0].UPDATED1.value == "") {
        alert('一括更新をするにはリストから生徒を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}


function Name_Clip(name_text){
    var str = name_text.value;
    var Cliping_str;

    Cliping_str = str.slice(0,10);

    if(document.forms[0].NAME_SHOW.value == '') document.forms[0].NAME_SHOW.value = Cliping_str;

    return true;
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

function setWareki(obj, ymd)
{
    var d = ymd;
    var tmp = d.split('/');
    var ret = Calc_Wareki(tmp[0],tmp[1],tmp[2]);


}