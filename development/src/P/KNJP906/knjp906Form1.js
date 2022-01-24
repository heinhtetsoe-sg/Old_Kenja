function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    if (cmd == 'update') {
       if (document.forms[0]["CHANGE_VAL_FLG"].value == '1') {
            if (!confirm('給付金額を変更した生徒の伝票を削除します。よろしいでしょうか？')) {
                return;
            }
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkChgValue(schregno) {
    var currentVal = document.forms[0]["BENEFIT_MONEY-" + schregno].value;
    var preVal     = document.forms[0]["PRE_MONEY-" + schregno].value;

    if (currentVal != preVal) {
        document.forms[0]["CHANGE_VAL_FLG"].value = '1';
    }
}
/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    var val = document.forms[0].TEXT_FIELD_NAME.value.split(',');
    var nameArray = new Array();
    nameArray[0] = document.forms[0].TEXT_FIELD_NAME.value;

    var renArray = new Array();
    var schregNoArray = document.forms[0].SCHREGNOS.value.split(",");
    for (var i = 0; i < schregNoArray.length; i++) {
        renArray[i] = schregNoArray[i];
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt"      :cnt,
               "renbanArray"     : renArray
               });

    return false;
}

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
}

/***********************************/
/* クリップボードの中身のチェック  */
/* (だめなデータならばfalseを返す) */
/* (共通関数から呼ばれる)          */
/***********************************/
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var i;
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (isNaN(clipTextArray[j][i])) {
                        alert('数値以外のデータが含まれいてます。【' + clipTextArray[j][i] + '】');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}

