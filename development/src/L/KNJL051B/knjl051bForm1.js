function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'read' || cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].S_EXAMNO.value == '' || eval(document.forms[0].S_EXAMNO.value) == 0) {
            alert('{rval MSG901}' + '\n 座席番号には 1 以上を入力してください');
            return false;
        }
    }    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function toInterViewInteger(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= "1" && ch <= "5") {
            newString += ch;
        }
    }
    return ShowDialog(newString,checkString,"数字の1～5");
}

//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("A_CHECK",
                              "B_CHECK",
                              "C_CHECK");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
    return true;
}

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
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
                if (clipTextArray[j][i] != undefined && clipTextArray[j][i] != '') {
                    var str = clipTextArray[j][i];
                    if (objectNameArray[k].match(/_CHECK/)) {
                        if (str != "1" && str != "2" && str != "3" && str != "4" && str != "5") {
                            alert('{rval MSG901}'+'「1～5」を入力して下さい。');
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
