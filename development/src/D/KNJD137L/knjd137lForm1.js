function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
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
    var nameArray = new Array("RECORD_VAL02",
                              "TOTALSTUDYTIME",
                              "ATTENDREC_REMARK",
                              "REMARK3",
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
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    var record_val02_gyou       = parseInt(document.forms[0].record_val02_gyou.value);
    var record_val02_moji       = parseInt(document.forms[0].record_val02_moji.value);
    var totalstudytime_gyou     = parseInt(document.forms[0].totalstudytime_gyou.value);
    var totalstudytime_moji     = parseInt(document.forms[0].totalstudytime_moji.value);
    var attendrec_remark_gyou   = parseInt(document.forms[0].attendrec_remark_gyou.value);
    var attendrec_remark_moji   = parseInt(document.forms[0].attendrec_remark_moji.value);
    var remark3_gyou            = parseInt(document.forms[0].remark3_gyou.value);
    var remark3_moji            = parseInt(document.forms[0].remark3_moji.value);
    var communication_gyou      = parseInt(document.forms[0].communication_gyou.value);
    var communication_moji      = parseInt(document.forms[0].communication_moji.value);

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {

                if (clipTextArray[j][i] != undefined) {

                    if (objectNameArray[k] == 'RECORD_VAL02') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (record_val02_moji * 2)) > record_val02_gyou) {
                            alert(record_val02_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'TOTALSTUDYTIME') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (totalstudytime_moji * 2)) > totalstudytime_gyou) {
                            alert(totalstudytime_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'ATTENDREC_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (attendrec_remark_moji * 2)) > attendrec_remark_gyou) {
                            alert(attendrec_remark_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK3') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (remark3_moji * 2)) > remark3_gyou) {
                            alert(remark3_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'COMMUNICATION') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (communication_moji * 2)) > communication_gyou) {
                            alert(communication_gyou + '行までです');
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
