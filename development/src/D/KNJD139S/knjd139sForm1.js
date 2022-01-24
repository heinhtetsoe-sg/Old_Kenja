function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
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

function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('リストから生徒を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    console.log(link);
    parent.location.href=link;
}

/************************************************* 貼付け関係 ***********************************************/            
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る

    var nameArray = new Array("REMARK01_01",
                              "REMARK01_02",
                              "REMARK01_03",
                              "REMARK02_02",
                              "REMARK02_01",
                              "REMARK02_03",
                              "REMARK02_04",
                              "ATTENDREC_REMARK",
                              "COMMUNICATION");

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
    //var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    var REMARK01_01_gyou = parseInt(document.forms[0].REMARK01_01_GYO.value);
    var REMARK01_01_moji = parseInt(document.forms[0].REMARK01_01_KETA.value);
    var REMARK01_02_gyou = parseInt(document.forms[0].REMARK01_02_GYO.value);
    var REMARK01_02_moji = parseInt(document.forms[0].REMARK01_02_KETA.value);
    var REMARK01_03_gyou = parseInt(document.forms[0].REMARK01_03_GYO.value);
    var REMARK01_03_moji = parseInt(document.forms[0].REMARK01_03_KETA.value);
    var REMARK02_02_gyou = parseInt(document.forms[0].REMARK02_02_GYO.value);
    var REMARK02_02_moji = parseInt(document.forms[0].REMARK02_02_KETA.value);
    var REMARK02_01_gyou = parseInt(document.forms[0].REMARK02_01_GYO.value);
    var REMARK02_01_moji = parseInt(document.forms[0].REMARK02_01_KETA.value);
    var REMARK02_03_gyou = parseInt(document.forms[0].REMARK02_03_GYO.value);
    var REMARK02_03_moji = parseInt(document.forms[0].REMARK02_03_KETA.value);
    var REMARK02_04_gyou = parseInt(document.forms[0].REMARK02_04_GYO.value);
    var REMARK02_04_moji = parseInt(document.forms[0].REMARK02_04_KETA.value);
    var ATTENDREC_REMARK_gyou = parseInt(document.forms[0].ATTENDREC_REMARK_GYO.value);
    var ATTENDREC_REMARK_moji = parseInt(document.forms[0].ATTENDREC_REMARK_KETA.value);
    var COMMUNICATION_gyou = parseInt(document.forms[0].COMMUNICATION_GYO.value);
    var COMMUNICATION_moji = parseInt(document.forms[0].COMMUNICATION_KETA.value);

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (objectNameArray[k] == 'REMARK01_01') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK01_01_moji * 2)) > REMARK01_01_gyou) {
                            alert(REMARK01_01_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK01_02') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK01_02_moji * 2)) > REMARK01_02_gyou) {
                            alert(REMARK01_02_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK01_03') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK01_03_moji * 2)) > REMARK01_03_gyou) {
                            alert(REMARK01_03_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK02_02') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK02_02_moji * 2)) > REMARK02_02_gyou) {
                            alert(REMARK02_02_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK02_01') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK02_01_moji * 2)) > REMARK02_01_gyou) {
                            alert(REMARK02_01_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK02_03') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK02_03_moji * 2)) > REMARK02_03_gyou) {
                            alert(REMARK02_03_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK02_04') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (REMARK02_04_moji * 2)) > REMARK02_04_gyou) {
                            alert(REMARK02_04_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'ATTENDREC_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (ATTENDREC_REMARK_moji * 2)) > ATTENDREC_REMARK_gyou) {
                            alert(ATTENDREC_REMARK_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'COMMUNICATION') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (COMMUNICATION_moji * 2)) > COMMUNICATION_gyou) {
                            alert(COMMUNICATION_gyou + '行までです');
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
