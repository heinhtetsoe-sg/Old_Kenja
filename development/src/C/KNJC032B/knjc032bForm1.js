function btn_submit(cmd)
{
    if (cmd == 'update') {
    
        var i = document.forms[0].SCHREGNO.selectedIndex;
        if (document.forms[0].SCHREGNO.options[i].value == '') {
            alert('{rval MSG304}');
            return false;
        }
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}



function show(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("LESSON[]",
                              "OFFDAYS[]",
                              "ABROAD[]",
                              "ABSENT[]",
                              "SUSPEND[]",
                              "VIRUS[]",
                              "MOURNING[]",
                              "SICK[]",
                              "NOTICE[]",
                              "NONOTICE[]",
                              "LATEDETAIL[]",
                              "KEKKA_JISU[]",
                              "KEKKA[]");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt" :cnt
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

                    targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"][" + objCnt + "]");
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
