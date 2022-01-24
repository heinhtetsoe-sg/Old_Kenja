function btn_submit(cmd)
{
    if (cmd == 'update') {
        var i = document.forms[0].SCHREGNO.selectedIndex;
        if (document.forms[0].SCHREGNO.options[i].value == '') {
            alert('{rval MSG304}');
            return false;
        }

        //データを格納
        document.forms[0].HIDDEN_SUBCLASSCD.value     = document.forms[0].SUBCLASSCD.value;
        document.forms[0].HIDDEN_CHAIRCD.value     = document.forms[0].CHAIRCD.value;
        document.forms[0].HIDDEN_SCHREGNO.value     = document.forms[0].SCHREGNO.value;

        //使用不可項目
        document.forms[0].SUBCLASSCD.disabled = true;
        document.forms[0].CHAIRCD.disabled = true;
        document.forms[0].SCHREGNO.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("LESSON[]");
    if (document.forms[0].unUseOffdays.value != "true") {
        nameArray.push("OFFDAYS[]");
    }
    if (document.forms[0].unUseAbroad.value != "true") {
        nameArray.push("ABROAD[]");
    }
    if (document.forms[0].unUseAbsent.value != "true") {
        nameArray.push("ABSENT[]");
    }
    nameArray.push("SUSPEND[]");
    if (document.forms[0].useKoudome.value == "true") {
        nameArray.push("KOUDOME[]");
    }
    if (document.forms[0].useVirus.value == "true") {
        nameArray.push("VIRUS[]");
    }
    nameArray.push("MOURNING[]");
    if (document.forms[0].SICK_FLG !== undefined) {
        nameArray.push("SICK[]");
    }
    if (document.forms[0].NOTICE_FLG !== undefined) {
        nameArray.push("NOTICE[]");
    }
    if (document.forms[0].NONOTICE_FLG !== undefined) {
        nameArray.push("NONOTICE[]");
    }
    nameArray.push("LATE[]");
    nameArray.push("EARLY[]");

    if (document.forms[0].objCntSub.value > 1) {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"hairetu",
                   "objectNameArray" :nameArray,
                   "hairetuCnt" :cnt
                   });
    } else {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"kotei",
                   "objectNameArray" :nameArray
                   });
    }

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
    var retuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var objCnt = harituke_jouhou.hairetuCnt;

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == harituke_jouhou.clickedObj.name) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    if (document.forms[0].objCntSub.value > 1) {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"][" + objCnt + "]");
                    } else {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"]");
                    }
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        if (isNaN(clipTextArray[gyouCnt][retuCnt])){
                            alert('{rval MSG907}');
                            return false;
                        }
                    }
                }
                retuCnt++;
            }
        }
        objCnt++;
    }
    return true;
}
