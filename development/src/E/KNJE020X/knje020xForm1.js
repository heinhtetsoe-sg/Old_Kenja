function btn_submit(cmd){
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update'){
            updateFrameLocks();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showPaste(obj, setName) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array(setName);

    var renArray = new Array();
    var renArray = document.forms[0].schregNos.value.split(",");
    var cnt = 0;
    for (var i = 0; i < renArray.length; i++) {
        cnt++;
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt"      :cnt,
               "renbanArray"     :renArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

//貼付けの際何かするわけではないのでそのまま貼付けをするだけ(共通関数から呼ばれる)
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
    return true;
}

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
function checkClip(clipTextArray, harituke_jouhou) {
    return true;
}
