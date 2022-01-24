function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'subform5'){      //検定選択
        var sizeW = 670;
        if (document.forms[0].useQualifiedMst.value == "1") {
            sizeW = 800;
        }
        loadwindow('knjd131tindex.php?cmd=subform5',0,0,sizeW,550);
        return true;
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

//総学講座名取込処理
function inputChairName() {
    var chairname = document.forms[0].chairname.value;
    document.forms[0].REMARK1.value += chairname;
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

    var nameArray = new Array("DETAIL_01_01_REMARK1",
                              "DETAIL_01_02_REMARK1",
                              "DETAIL_02_01_REMARK1",
                              "DETAIL_03_01_REMARK1",
                              "DETAIL_04_01_REMARK1",
                              "ATTENDREC_REMARK");

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

    var DETAIL_01_01_REMARK1_gyou = parseInt(document.forms[0].DETAIL_01_01_REMARK1_gyou.value);
    var DETAIL_01_01_REMARK1_moji = parseInt(document.forms[0].DETAIL_01_01_REMARK1_moji.value);
    var DETAIL_01_02_REMARK1_gyou = parseInt(document.forms[0].DETAIL_01_02_REMARK1_gyou.value);
    var DETAIL_01_02_REMARK1_moji = parseInt(document.forms[0].DETAIL_01_02_REMARK1_moji.value);
    var DETAIL_02_01_REMARK1_gyou = parseInt(document.forms[0].DETAIL_02_01_REMARK1_gyou.value);
    var DETAIL_02_01_REMARK1_moji = parseInt(document.forms[0].DETAIL_02_01_REMARK1_moji.value);
    var DETAIL_03_01_REMARK1_gyou = parseInt(document.forms[0].DETAIL_03_01_REMARK1_gyou.value);
    var DETAIL_03_01_REMARK1_moji = parseInt(document.forms[0].DETAIL_03_01_REMARK1_moji.value);
    var DETAIL_04_01_REMARK1_gyou = parseInt(document.forms[0].DETAIL_04_01_REMARK1_gyou.value);
    var DETAIL_04_01_REMARK1_moji = parseInt(document.forms[0].DETAIL_04_01_REMARK1_moji.value);
    var attendrec_remark_gyou = parseInt(document.forms[0].attendrec_remark_gyou.value);
    var attendrec_remark_moji = parseInt(document.forms[0].attendrec_remark_moji.value);

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (objectNameArray[k] == 'DETAIL_01_01_REMARK1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (DETAIL_01_01_REMARK1_moji * 2)) > DETAIL_01_01_REMARK1_gyou) {
                            alert(DETAIL_01_01_REMARK1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'DETAIL_01_02_REMARK1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (DETAIL_01_02_REMARK1_moji * 2)) > DETAIL_01_02_REMARK1_gyou) {
                            alert(DETAIL_01_02_REMARK1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'DETAIL_02_01_REMARK1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (DETAIL_02_01_REMARK1_moji * 2)) > DETAIL_02_01_REMARK1_gyou) {
                            alert(DETAIL_02_01_REMARK1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'DETAIL_03_01_REMARK1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (DETAIL_03_01_REMARK1_moji * 2)) > DETAIL_03_01_REMARK1_gyou) {
                            alert(DETAIL_03_01_REMARK1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'DETAIL_04_01_REMARK1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (DETAIL_04_01_REMARK1_moji * 2)) > DETAIL_04_01_REMARK1_gyou) {
                            alert(DETAIL_04_01_REMARK1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'ATTENDREC_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (attendrec_remark_moji * 2)) > attendrec_remark_gyou) {
                            alert(attendrec_remark_gyou + '行までです');
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
