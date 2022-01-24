function btn_submit(cmd)
{

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkVal(obj) {
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = "";
        obj.focus();
        return false;
    }
    if (String(obj.value).length > 0) {
        var seiSu = String(obj.value).split(".")[0];
        var syouSu = String(obj.value).split(".")[1];
        if (seiSu.length > 4 || syouSu.length > 1) {
            alert('整数は4桁、小数は第一位までです。');
            obj.value = "";
            obj.focus();
            return false;
        }
    }
}

//権限チェック
function OnPropertiesError()
{
    alert('この処理は、県下模試科目登録不要の時\n許可されていません。');
    closeWin();
}

/************************************************* 貼付け関係 ***********************************************/            
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array(1);
    nameArray[0] = "AVG";

    var renArray = new Array();
    var subclassArray = document.forms[0].subclassCd.value.split(":");
    var cnt = 0;
    for (var i = 0; i < subclassArray.length; i++) {
        renArray[i] = subclassArray[i];
        cnt++;
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt" :cnt,
               "renbanArray" : renArray
               });

    //これを実行しないと貼付けそのものが実行されてしまう
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

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
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
                    if (isNaN(String(clipTextArray[j][i]))) {
                        alert('{rval MSG907}');
                        return false;
                    }
                    var seiSu = String(clipTextArray[j][i]).split(".")[0];
                    var syouSu = String(clipTextArray[j][i]).split(".")[1];
                    if (seiSu.length > 4 || syouSu.length > 1) {
                        alert('整数は4桁、小数は第一位までです。');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}
