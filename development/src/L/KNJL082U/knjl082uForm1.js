function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (!confirm('{rval MSG108}')) {
            return false;
        }
        closeWin();
    }

    if (cmd == 'update') {
        var dataCnt = 0;
        var examnos = document.forms[0].HID_EXAMNO.value.split(",");
        for (cnt = 0; cnt < examnos.length; cnt++) {
            var targetId  = examnos[cnt];
            var targetObj = document.getElementById(targetId);
            if (targetObj.value !== '') {
                dataCnt += 1;
            }
        }
        document.forms[0].AUTO_NO_FLG.value = '';
        if (dataCnt == 0) {
            //全て未入力 ===> 自動割振りする
            document.forms[0].AUTO_NO_FLG.value = '1';
        } else if (dataCnt == examnos.length) {
            //全て入力済 ===> 手入力した値で登録する
        } else {
            //一部未入力 ===> エラー
            alert('\n未入力データが存在します。\nデータを入力して下さい。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//学籍番号チェック
function checkNo(obj) {
    if (obj.value != "") {
        //数値チェック
        obj.value = toInteger(obj.value);
        //桁数チェック
        if (String(obj.value).length < 8) {
            alert('{rval MSG901}' + '\n桁数が不足しています。8桁入力して下さい。');
            obj.focus();
            return;
        }
        //重複チェック
        var examnos = document.forms[0].HID_EXAMNO.value.split(",");
        for (cnt = 0; cnt < examnos.length; cnt++) {
            var targetId  = examnos[cnt];
            var targetObj = document.getElementById(targetId);
            if (obj.id !== targetId && obj.value == targetObj.value) {
                alert('{rval MSG901}' + '\nデータが重複しています。');
                obj.focus();
                return;
            }
        }
    }
}

function Setflg(obj) {
    change_flg = true;

    document.getElementById('ROWID' + obj.id).style.background = "yellow";
    obj.style.background = "yellow";
}

var clipValLength;
//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SCHREGNO[]");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });

    //以下、insertTsvで取得するclipValLengthが前提なので注意。
    var targetName   = obj.name.split("-")[0];
    var targetNumber = parseInt(obj.name.split("-")[1]);
    var setNumber = 0;
    for (var objCnt = 0; objCnt < clipValLength; objCnt++) {
        setNumber = targetNumber + objCnt;
        //貼り付けた値を元に、非活性設定の処理を実施
        checkNo(document.forms[0][targetName+"-" + setNumber]);
    }

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
    
    clipValLength = clipTextArray.length;
    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
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
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }
    var moveEnt = 40;
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
