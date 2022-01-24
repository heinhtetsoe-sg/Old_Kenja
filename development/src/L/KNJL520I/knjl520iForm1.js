function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    //実行
    if (cmd == "exec") {
        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "csvInput";
        } else {
            cmd = "csvOutput";
        }
    }

    //CSV取込
    if (cmd == "csvInput") {
        if (!confirm('処理を開始します。よろしいでしょうか？')) {
            return false;
        }

        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//値チェック
function toIntegerCheck(checkString, digits, objId) {
    var newString = "";
    if (checkString != "" && checkString.length > digits) {
        return ShowDialog(newString,checkString,"数字"+digits+"桁");
    }
    return toInteger(checkString, objId);
}

//計算
function Keisan(rowCount, grade)
{
    var setSum = 0;
    var setAvg = '0.0';
    var kyouka_count = document.forms[0].kyouka_count.value;

    for (var i = 1; i <= kyouka_count; i++) {
        var tmpObj = document.getElementById("KYOKA" + grade + "_0" + i + "-" + rowCount);
        if (tmpObj.value != '') {
            setSum = parseInt(setSum, 10) + parseInt(tmpObj.value, 10);
        }
    }

    if (setSum != 0) {
        setAvg = setSum / kyouka_count;
        setAvg = Math.round(setAvg * 10) / 10;
        setAvg = setAvg.toFixed(1);
    }

    document.getElementById("TOTAL_ALL" + grade + "-" + rowCount).innerHTML = setSum;
    document.forms[0]["HID_TOTAL_ALL" + grade + "-" + rowCount].value = setSum;
    document.getElementById("AVERAGE_ALL" + grade + "-" + rowCount).innerHTML = setAvg;
    document.forms[0]["HID_AVERAGE_ALL" + grade + "-" + rowCount].value = setAvg;

    return;
}

//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = document.forms[0].TEXT_NAME.value.split(",");

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
                    var str = new String(clipTextArray[j][i]);
                    if (objectNameArray[k].match(/KYOKA/)) {
                        if (str.length > 1 || str.match(/[^0-9]/)) {
                            alert('{rval MSG901}'+'\n1桁の数値を入力して下さい。');
                            return false;
                        }
                    }
                    if (objectNameArray[k].match(/KESSEKI/)) {
                        if (str.length > 3 || str.match(/[^0-9]/)) {
                            alert('{rval MSG901}'+'\n3桁の数値を入力して下さい。');
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

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab2(obj, setTextField, cnt) {
    //移動可能なオブジェクト
    var textFieldArray = setTextField.split(",");
    //行数
    var lineCnt = document.forms[0].COUNT.value;
    //1行目の生徒
    var isFirstStudent = cnt == 0 ? true : false;
    //最終行の生徒
    var isLastStudent = cnt == lineCnt - 1 ? true : false;
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    var moveEnt = e.keyCode;
    // if (e.keyCode != 13 || e.keyCode != 9) {
    if (e.keyCode != 13) {
        return;
    }
    // var moveEnt = 40;
    for (var i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] + cnt == obj.name) {
            var isFirstItem = i == 0 ? true : false;
            var isLastItem = i == textFieldArray.length - 1 ? true : false;
            if (moveEnt == 37) {
                if (isFirstItem && isFirstStudent) {
                    obj.focus();
                    return;
                }
                if (isFirstItem) {
                    targetname = textFieldArray[(textFieldArray.length - 1)] + (cnt - 1);
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[(i - 1)] + cnt;
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 38) {
                if (isFirstStudent) {
                    obj.focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt - 1);
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 39 || moveEnt == 13) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastItem) {
                    targetname = textFieldArray[0] + (cnt + 1);
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[(i + 1)] + cnt;
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 40) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastStudent) {
                    targetname = textFieldArray[(i + 1)] + 0;
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt + 1);
                document.forms[0].elements[targetname].focus();
                return;
            }
        }
    }
}
