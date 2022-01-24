function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'form2'){
        loadwindow('knjd138jindex.php?cmd=form2',0,0,600,500);
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
    var nameArray = new Array("TOTALSTUDYTIME",
                              "REMARK2",
                              "REMARK1",
                              "REMARK01",
                              "REMARK02",
                              "REMARK03",
                              "REMARK04",
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
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    var totalstudytime_gyou = parseInt(document.forms[0].totalstudytime_gyou.value);
    var totalstudytime_moji = parseInt(document.forms[0].totalstudytime_moji.value);
    var remark1_gyou = parseInt(document.forms[0].remark1_gyou.value);
    var remark1_moji = parseInt(document.forms[0].remark1_moji.value);
    var remark2_gyou = parseInt(document.forms[0].remark2_gyou.value);
    var remark2_moji = parseInt(document.forms[0].remark2_moji.value);
    var attemdrec_remark_gyou = parseInt(document.forms[0].attemdrec_remark_gyou.value);
    var attemdrec_remark_moji = parseInt(document.forms[0].attemdrec_remark_moji.value);
    var communication_gyou = parseInt(document.forms[0].communication_gyou.value);
    var communication_moji = parseInt(document.forms[0].communication_moji.value);

    var remark01_gyou = parseInt(document.forms[0].remark01_gyou.value);
    var remark01_moji = parseInt(document.forms[0].remark01_moji.value);
    var remark02_gyou = parseInt(document.forms[0].remark02_gyou.value);
    var remark02_moji = parseInt(document.forms[0].remark02_moji.value);
    var remark03_gyou = parseInt(document.forms[0].remark03_gyou.value);
    var remark03_moji = parseInt(document.forms[0].remark03_moji.value);
    var remark04_gyou = parseInt(document.forms[0].remark04_gyou.value);
    var remark04_moji = parseInt(document.forms[0].remark04_moji.value);

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }

            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {

                    if (objectNameArray[k] == 'TOTALSTUDYTIME') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (totalstudytime_moji * 2)) > totalstudytime_gyou) {
                            alert(totalstudytime_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK2') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (remark2_moji * 2)) > remark2_gyou) {
                            alert(remark2_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK1') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (remark1_moji * 2)) > remark1_gyou) {
                            alert(remark1_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK01') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (remark01_moji * 2)) > remark01_gyou) {
                            alert(remark01_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK02') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (remark02_moji * 2)) > remark02_gyou) {
                            alert(remark02_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK03') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (remark03_moji * 2)) > remark03_gyou) {
                            alert(remark03_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'REMARK04') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (remark04_moji * 2)) > remark04_gyou) {
                            alert(remark04_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'ATTENDREC_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (attemdrec_remark_moji * 2)) > attemdrec_remark_gyou) {
                            alert(attemdrec_remark_gyou + '行までです');
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
