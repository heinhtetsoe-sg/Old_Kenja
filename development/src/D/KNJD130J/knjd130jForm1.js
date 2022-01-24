function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        //更新中の画面ロック(全フレーム)
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//テキスト内でEnterを押してもsubmitされないようにする
//Submitしない
function btn_keypress(){
    if (event.keyCode == 13){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}

/************************************************* 貼付け関係 ***********************************************/            
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る

    var nameArray = new Array("REMARK1_1",
                              "REMARK1_2",
                              "REMARK1_3",
                              "REMARK2",
                              "REMARK3_1",
                              "REMARK3_2",
                              "REMARK3_3",
                              "REMARK4"
                              );

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"kotei",
               "objectNameArray" :nameArray
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

    var REMARK1_1_gyou = parseInt(document.forms[0].REMARK1_1_GYO.value);
    var REMARK1_1_moji = parseInt(document.forms[0].REMARK1_1_KETA.value);
    var REMARK1_2_gyou = parseInt(document.forms[0].REMARK1_2_GYO.value);
    var REMARK1_2_moji = parseInt(document.forms[0].REMARK1_2_KETA.value);
    var REMARK1_3_gyou = parseInt(document.forms[0].REMARK1_3_GYO.value);
    var REMARK1_3_moji = parseInt(document.forms[0].REMARK1_3_KETA.value);
    var REMARK2_gyou = parseInt(document.forms[0].REMARK2_GYO.value);
    var REMARK2_moji = parseInt(document.forms[0].REMARK2_KETA.value);
    var REMARK3_1_gyou = parseInt(document.forms[0].REMARK3_1_GYO.value);
    var REMARK3_1_moji = parseInt(document.forms[0].REMARK3_1_KETA.value);
    var REMARK3_2_gyou = parseInt(document.forms[0].REMARK3_2_GYO.value);
    var REMARK3_2_moji = parseInt(document.forms[0].REMARK3_2_KETA.value);
    var REMARK3_3_gyou = parseInt(document.forms[0].REMARK3_3_GYO.value);
    var REMARK3_3_moji = parseInt(document.forms[0].REMARK3_3_KETA.value);
    var REMARK4_gyou = parseInt(document.forms[0].REMARK4_GYO.value);
    var REMARK4_moji = parseInt(document.forms[0].REMARK4_KETA.value);

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (objectNameArray[k] == 'REMARK1_1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK1_1_moji * 2)) > REMARK1_1_gyou) {
                            alert(REMARK1_1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK1_2') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK1_2_moji * 2)) > REMARK1_2_gyou) {
                            alert(REMARK1_2_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK1_3') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK1_3_moji * 2)) > REMARK1_3_gyou) {
                            alert(REMARK1_3_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK2') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK2_moji * 2)) > REMARK2_gyou) {
                            alert(REMARK2_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK3_1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK3_1_moji * 2)) > REMARK3_1_gyou) {
                            alert(REMARK3_1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK3_2') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK3_2_moji * 2)) > REMARK3_2_gyou) {
                            alert(REMARK3_2_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK3_3') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK3_3_moji * 2)) > REMARK3_3_gyou) {
                            alert(REMARK3_3_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK4') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK4_moji * 2)) > REMARK4_gyou) {
                            alert(REMARK4_gyou + '行までです');
                            return false;
                        }
                    }
                }
                i++;
            }
        }
    }
    return true;
}
