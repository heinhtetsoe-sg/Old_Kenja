function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
        closeWin();
    }

    //読込
    if (cmd == 'read') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }

    //実行
    if (cmd == "exec") {
        if (document.forms[0].OUTPUT[1].checked) {
            cmd = "csvInput";
        } else if (document.forms[0].OUTPUT[0].checked || document.forms[0].OUTPUT[2].checked) {
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

//得点チェック
function checkScore(obj) {
    //満点チェック
    obj.value = toInteger(obj.value);
    var perfect = document.forms[0].PERFECT.value;
    if (obj.value > eval(perfect)) {
        alert('{rval MSG901}' + '\n満点：' + perfect + '以下で入力してください。');
        obj.focus();
        return;
    }
}

function changeFlg(obj) {
    document.forms[0].CHANGE_FLG.value = '1';

    document.forms[0].HID_EXAM_TYPE.value = document.forms[0].EXAM_TYPE.options[document.forms[0].EXAM_TYPE.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].HID_EXAMHALLCD.value = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;

    document.forms[0].EXAM_TYPE.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;
    document.forms[0].EXAMHALLCD.disabled = true;
}

//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SCORE");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, targetNumber) {
    var orgValue = targetObject.value;

    //テキストボックスがdisable以外の場合
    if (!targetObject.disabled) {
        targetObject.value = val;
    }

    //すでにある値とクリップボードの値が違う場合
    if (targetObject.value != orgValue) {
        changeFlg(targetObject);
    }

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
                    if (objectNameArray[k].match(/SCORE/)) {
                        if (str.match(/[^0-9]/) || str.length > 3) {
                            alert('{rval MSG901}'+'\n3桁の数値を入力して下さい。');
                            return false;
                        }

                        //満点チェック
                        var perfect = document.forms[0].PERFECT.value;
                        if (str > eval(perfect)) {
                            alert('{rval MSG901}' + '\n満点：' + perfect + '以下で入力してください。');
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
function keyChangeEntToTab(obj) {
    //対象項目
    var setField = document.forms[0].setField.value.split(",");
    //対象生徒
    var examnoArray = document.forms[0].HID_EXAMNO.value.split(",");
    var lastExamno = examnoArray.slice(-1)[0];
    var lastField = "";

    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    //移動可能なオブジェクト
    var targetFieldArray = new Array();
    y = 0;
    for (var e = 0; e < examnoArray.length; e++) {
        for (var i = 0; i < setField.length; i++) {
            targetFieldArray[y++] = setField[i]+'-'+examnoArray[e];
            if (examnoArray[e] == lastExamno) {
                lastField = setField[i]+'-'+examnoArray[e];
            }
        }
    }

    for (var i = 0; i < targetFieldArray.length; i++) {
        if (targetFieldArray[i] == obj.name) {
            if (targetFieldArray[i] == lastField) {
                targetObject = eval("document.forms[0][\"" + targetFieldArray[0] + "\"]");
                targetObject.focus();
            } else {
                targetObject = eval("document.forms[0][\"" + targetFieldArray[(i + 1)] + "\"]");
                targetObject.focus();
            }
            return;
        }
    }
}
