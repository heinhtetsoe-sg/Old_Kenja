function btn_submit(cmd) {
/***
    if (cmd == "update") {
        re = new RegExp("^UPDATE_CHK_" );
        var updDataUmu = false;
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.match(re) && document.forms[0].elements[i].checked) {
                updDataUmu = true;
                break;
            }
        }
        if (!updDataUmu) {
            alert('更新対象データが選択されていません。');
            return false;
        }
    }
***/
    //サブミット中、更新ボタン使用不可
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    re = new RegExp("^UPDATE_CHK_" );
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(re) && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}
/**************************************************** 貼付け関係 **********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("PASSNO"
                              );

    var renArray = new Array();
    var setKeyArray = document.forms[0].hiddenSetKey.value.split(",");
    for (var i = 0; i < setKeyArray.length; i++) {
        renArray[i] = setKeyArray[i];
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt"      :cnt,
               "renbanArray"     : renArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}
//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
    return true;
}
//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
function checkClip(clipTextArray, harituke_jouhou) {
    return true;
}
