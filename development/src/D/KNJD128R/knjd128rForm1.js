function btn_submit(cmd) {
    //取消
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

//事前チェック
function preCheck() {
    alert('{rval MSG305}\n欠課時数の出欠コードを設定してください。');
    closeWin();
}

//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

//得点チェック
function scoreCheck(obj) {
    if (obj.value != "*") {
        obj.value = toInteger(obj.value);
/***
        if (obj.value > eval(aPerfect[obj.id])) {
            alert('{rval MSG901}' + '\n満点：'+aPerfect[obj.id]+'以下で入力してください。');
            obj.focus();
            return;
        }
***/
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    var nameArray = new Array();
    var subclass = document.forms[0].SUBCLASS.value.split(",");

    //テキストボックスの名前の配列を作る
    for (sCnt = 0; sCnt < subclass.length; sCnt++) {
        nameArray.push('SCORE'+':'+subclass[sCnt]+'[]');
    }

    if (document.forms[0].objCntSub.value > 1) {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"hairetu",
                   "objectNameArray" :nameArray,
                   "hairetuCnt"      :cnt
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
    recordField = eval("document.forms[0][\"RECORD_FIELD[]\"][" + targetNumber + "]");
    var recField = recordField.value.split(',');

    var recFlg = false;
    for (var rCnt = 0; rCnt < recField.length; rCnt++) {
        if ('SCORE'+':'+recField[rCnt]+'[]' == targetObject.name) {
            recFlg = true;
        }
    }
    if (recFlg == true) {
        targetObject.value = val;
    }
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
