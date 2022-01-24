function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    if (cmd == 'update') {
        for(i = 0; i < document.forms[0].counter.value; i++) {
            var schregno = document.getElementById('SCHREGNO_' + i).innerText;
            if(document.forms[0]["ATTEND_REMARK-" + i].value.length > document.forms[0].moji.value * 3) {
                alert('出欠の備考は' + document.forms[0].moji.value + '文字までです。\n学籍番号:' + schregno);
                return false;
            }
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("ATTEND_REMARK");

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
                    //if (String(clipTextArray[j][i]).length > 30) {
                    //   alert('全角30文字までです');
                    //   return false;
                    //}
                }
                i++;
            }
        }
    }
    return true;
}

/***********************************/
/* フォームの中身のチェック  */
/* (入力が変化していたら色を付ける) */
/***********************************/
function changeColorIfInputChange(defaultInput, name) {
    var target = document.forms[0][name];
    var input = target.value;
    if (defaultInput !== input) {
        target.style.background='#ccffcc';
    } else {
        target.style.background='#ffffff';
    }
}
