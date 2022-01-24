function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//文字数チェック
function toStringCheck(checkString, len) {
    var newString = "";
    var count = 0;
    if (checkString != "" && checkString.length <= len) {
        return checkString;
    }

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if (newString.length < len) {
            newString += ch;
        }
    }
    return ShowDialog(newString, checkString, len + "文字以下");
}

//値チェック
function toIntegerCheck(checkString, digits, objId) {
    var newString = "";
    if (checkString != "" && checkString.length > digits) {
        return ShowDialog(newString, checkString, "数字" + digits + "桁");
    }
    return toInteger(checkString, objId);
}

//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("KYOKA_01",
        "KYOKA_02",
        "KYOKA_03",
        "KYOKA_04",
        "KYOKA_05",
        "KYOKA_06",
        "KYOKA_07",
        "KYOKA_08",
        "KYOKA_09",
        "KODO",
        "KESSEKI",
        "REMARK");

    insertTsv({
        "clickedObj": obj,
        "harituke_type": "renban",
        "objectNameArray": nameArray
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
    var targetName = harituke_jouhou.clickedObj.name.split("-")[0];
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
                    if (objectNameArray[k].match(/KYOKA/)) {
                        if (str.length > 1 && str.match(/[^0-9]+/)) {
                            alert('{rval MSG901}' + '\n1桁の数値を入力して下さい。');
                            return false;
                        }
                    }
                    if (objectNameArray[k].match(/KODO/) || objectNameArray[k].match(/KESSEKI/)) {
                        if (str.length > 3 && str.match(/[^0-9]+/)) {
                            alert('{rval MSG901}' + '\n3桁の数値を入力して下さい。');
                            return false;
                        }
                    }
                    if (objectNameArray[k].match(/REMARK/)) {
                        var len = jstrlen(str);
                        if (str.length > 15) {
                            alert('{rval MSG914}' + '\n備考は15文字までです。');
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

//バイト数取得
function jstrlen(str) {
    len = 0;
    str = escape(str);
    for (i = 0; i < str.length; i++, len++) {
        if (str.charAt(i) == "%") {
            if (str.charAt(++i) == "u") {
                i += 3;
                len += 2;
            }
            i++;
        }
    }
    return len;
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
