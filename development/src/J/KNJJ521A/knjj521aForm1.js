function btn_submit(cmd) {
    if (cmd == 'houkoku') {
        if (document.forms[0].DOC_NUMBER.value == '') {
            alert('{rval MSG304}' + '(文書番号)');
            return false;
        }
        if (document.forms[0].EXECUTE_DATE.value == '') {
            alert('{rval MSG304}' + '(作成日)');
            return false;
        }
        if (!confirm('{rval MSG108}')) return false;
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        if (document.forms[0].SOKUTEI_DATE.value == '') {
            alert('{rval MSG304}\n( 測定日付 )');
            return true;
        }
    }

    //CSV
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }

        if (document.forms[0].OUTPUT[1].checked) {
            cmd = 'uploadCsv';
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = 'downloadCsv';
        } else if (document.forms[0].OUTPUT[0].checked) {
            cmd = 'downloadError';
        } else {
            alert('ラジオボタンを選択してください。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function background_color(obj) {
    obj.style.background = '#ffffff';
}

function scrollRC() {
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

//入力チェック
function calc(obj) {
    var str = obj.value;
    var nam = obj.name;

    //空欄
    if (str == '') {
        return;
    }

    //英小文字から大文字へ自動変換
    if (str.match(/a|b|c|d|e/)) {
        obj.value = str.toUpperCase();
        str = str.toUpperCase();
    }

    //総合判定
    if (nam.match(/VALUE./)) {
        if (!str.match(/A|B|C|D|E/)) {
            alert('{rval MSG901}' + '\n「A,B,C,D,E」のいずれかを入力して下さい。');
            obj.focus();
            return;
        }

        //種目
    } else {
        obj.value = toFloat(obj.value);
        if (parseFloat(obj.value) >= 1000) {
            alert('{rval MSG915}' + '\n【0～999.9】までを入力して下さい。');
            obj.focus();
            return;
        }
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array('INQUIRY1', 'INQUIRY2', 'INQUIRY3', 'INQUIRY4', 'INQUIRY5', 'INQUIRY6', 'RECORD1', 'RECORD2', 'RECORD3', 'RECORD4', 'RECORD5', 'RECORD6', 'RECORD7', 'RECORD8', 'RECORD9', 'TOTAL', 'VALUE', 'HEIGHT', 'WEIGHT', 'SITHEIGHT');

    insertTsv({ clickedObj: obj, harituke_type: 'renban', objectNameArray: nameArray });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    if (targetObject.value != val) {
        targetObject.style.background = '#ccffcc';
    }
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
    var targetName = harituke_jouhou.clickedObj.name.split('-')[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split('-')[1];
    var objectNameArray = harituke_jouhou.objectNameArray;
    var electdiv = document.forms[0].ELECTDIV.value;

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
                if (clipTextArray[j][i] != undefined && clipTextArray[j][i] != '') {
                    var str = new String(clipTextArray[j][i]);
                    //総合判定
                    if (objectNameArray[k].match(/VALUE/)) {
                        //英小文字から大文字へ自動変換
                        if (str.match(/a|b|c|d|e/)) {
                            clipTextArray[j][i] = str.toUpperCase();
                            str = str.toUpperCase();
                        }
                        if (!str.match(/A|B|C|D|E/)) {
                            alert('{rval MSG901}' + '\n「A,B,C,D,E」のいずれかを入力して下さい。\n（総合判定）');
                            return false;
                        }
                        //種目
                    } else {
                        if (!toFloat(str)) {
                            return false;
                        }
                        if (parseFloat(str) >= 1000) {
                            alert('{rval MSG915}' + '\n【0～999.9】までを入力して下さい。\n（種目）');
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

function changeRadio(obj) {
    var type_file;
    if (obj.value == '1') {
        //1は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById('type_file'); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}
