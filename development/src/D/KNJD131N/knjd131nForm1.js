function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'reset') { //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function reloadIframe(url){
    document.getElementById("cframe").src=url
}

/************************************************* 貼付け関係 ***********************************************/            
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }
    
    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SEM1_01_REMARK",
                              "SEM1_02_REMARK",
                              "SEM2_01_REMARK",
                              "SEM2_02_REMARK",
                              "SEM3_02_REMARK");

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
    
    var SemesterName1 = document.forms[0].SemesterName1.value;
    var SemesterName2 = document.forms[0].SemesterName2.value;
    var SemesterName3 = document.forms[0].SemesterName3.value;

    var sem1_01_remark_gyou = parseInt(document.forms[0].sem1_01_remark_gyou.value);
    var sem1_01_remark_moji = parseInt(document.forms[0].sem1_01_remark_moji.value);
    var sem1_02_remark_gyou = parseInt(document.forms[0].sem1_02_remark_gyou.value);
    var sem1_02_remark_moji = parseInt(document.forms[0].sem1_02_remark_moji.value);
    var sem2_01_remark_gyou = parseInt(document.forms[0].sem2_01_remark_gyou.value);
    var sem2_01_remark_moji = parseInt(document.forms[0].sem2_01_remark_moji.value);
    var sem2_02_remark_gyou = parseInt(document.forms[0].sem2_02_remark_gyou.value);
    var sem2_02_remark_moji = parseInt(document.forms[0].sem2_02_remark_moji.value);
    var sem3_02_remark_gyou = parseInt(document.forms[0].sem3_02_remark_gyou.value);
    var sem3_02_remark_moji = parseInt(document.forms[0].sem3_02_remark_moji.value);

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (objectNameArray[k] == 'SEM1_01_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (sem1_01_remark_moji * 2)) > sem1_01_remark_gyou) {
                            alert(SemesterName1 + '中間考査の特記事項は' + sem1_01_remark_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'SEM1_02_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (sem1_02_remark_moji * 2)) > sem1_02_remark_gyou) {
                            alert(SemesterName1 + '期末考査の特記事項は' + sem1_02_remark_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'SEM2_01_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (sem2_01_remark_moji * 2)) > sem2_01_remark_gyou) {
                            alert(SemesterName2 + '中間考査の特記事項は' + sem2_01_remark_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'SEM2_02_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (sem2_02_remark_moji * 2)) > sem2_02_remark_gyou) {
                            alert(SemesterName2 + '期末考査の特記事項は' + sem2_02_remark_gyou + '行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'SEM3_02_REMARK') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (sem3_02_remark_moji * 2)) > sem3_02_remark_gyou) {
                            alert(SemesterName3 + '期末考査の特記事項は' + sem3_02_remark_gyou + '行までです');
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
