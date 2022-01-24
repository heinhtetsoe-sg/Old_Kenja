function btn_submit(cmd) {
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
window.onkeydown = function keydown(e) {
    if (e.keyCode === 13) {
        var nameArray = document.activeElement.name.split("-");
        var name = nameArray[0] + "-" + (parseInt(nameArray[1]) + 1);
        if (document.forms[0].elements[name]) {
            document.forms[0].elements[name].focus();
        }
    }
};
/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm("内容を貼付けますか？")) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("IQ");

    insertTsv({
        clickedObj: obj,
        harituke_type: "renban",
        objectNameArray: nameArray,
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
    targetObject.focus();
}

/***********************************/
/* クリップボードの中身のチェック  */
/* (だめなデータならばfalseを返す) */
/* (共通関数から呼ばれる)          */
/***********************************/
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var i;
    var targetName = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    for (j = 0; j < clipTextArray.length; j++) {
        //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) {
            //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) {
                //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    //スペース削除
                    var str_num = new String(clipTextArray[j][i]);
                    clipTextArray[j][i] = str_num.replace(/ |　/g, "");

                    clipTextArray[j][i] = clipTextArray[j][i].substr(0, 3);

                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])) {
                        alert("{rval MSG907}");
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}
