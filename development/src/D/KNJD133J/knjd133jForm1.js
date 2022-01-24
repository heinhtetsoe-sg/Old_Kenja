function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            //更新中の画面ロック
            updateFrameLock()
        }
    } else if (cmd == 'teikei' || cmd == 'teikei2' || cmd == 'teikei3') {
        chr = document.forms[0].CHAIRCD.value;
        if (document.forms[0].KNJD133J_semesCombo) {
            sendSemester = document.forms[0].SEMESTER.value;
        } else {
            sendSemester = "";
        }

        loadwindow('knjd133jindex.php?cmd='+cmd+'&CHR='+chr+'&SEMESTER='+sendSemester, event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 450);
        return true;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//画面切り替え
function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var y=0;
    var nameArray = [];
    if (document.forms[0].useRemark1.value == '1') {
        nameArray[y++] = "REMARK1";
    } else {
        nameArray[y++] = "TOTALSTUDYACT";
        if (document.forms[0].useTotalstudyTime_J.value == '1') {
            nameArray[y++] = "TOTALSTUDYTIME";
        }
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
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
                    if (document.forms[0].useRemark1.value == '1') {
                        if (objectNameArray[k] == 'REMARK1') {
                            moji = document.forms[0].REMARK1_moji.value;
                            gyou = document.forms[0].REMARK1_gyou.value;
                            if (validate_row_cnt(String(clipTextArray[j][i]), (moji * 2)) > gyou) {
                                label = document.forms[0].REMARK1_label.value;
                                alert(label+'は'+gyou+'行までです');
                                return false;
                            }
                        }
                    } else {
                        if (objectNameArray[k] == 'TOTALSTUDYACT') {
                            moji = document.forms[0].TOTALSTUDYACT_moji.value;
                            gyou = document.forms[0].TOTALSTUDYACT_gyou.value;
                            if (validate_row_cnt(String(clipTextArray[j][i]), (moji * 2)) > gyou) {
                                label = document.forms[0].TOTALSTUDYACT_label.value;
                                alert(label+'は'+gyou+'行までです');
                                return false;
                            }
                        }
                        if (document.forms[0].useTotalstudyTime_J.value == '1') {
                            if (objectNameArray[k] == 'TOTALSTUDYTIME') {
                                moji = document.forms[0].TOTALSTUDYTIME_moji.value;
                                gyou = document.forms[0].TOTALSTUDYTIME_gyou.value;
                                if (validate_row_cnt(String(clipTextArray[j][i]), (moji * 2)) > gyou) {
                                    alert('評価は'+gyou+'行までです');
                                    return false;
                                }
                            }
                        }
                    }
                }
                i++;
            }
        }
    }
    return true;
}
