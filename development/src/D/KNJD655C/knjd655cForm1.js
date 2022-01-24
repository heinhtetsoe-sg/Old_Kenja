function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    if (cmd == 'update') {
        check_flag = false;
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/^CHECK\-/)) {
                if (document.forms[0][i].checked) {
                    check_flag = true;
                }
            }
        }
        //どれにもチェックば入ってなかったら
        if (!check_flag) {
            alert('{rval MSG304}');
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all() {
    for (var i = 0; i < document.forms[0].length; i++) {
        if (document.forms[0][i].name.match(/^CHECK/)) {
            document.forms[0][i].checked = document.forms[0].CHECK_ALL.checked;
        }
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("REMARK");

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
    var moji = document.forms[0].moji.value; //入力可能な文字数を取得
    

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (String(clipTextArray[j][i]).length > moji) {
                        alert('全角' + moji + '文字までです');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}

