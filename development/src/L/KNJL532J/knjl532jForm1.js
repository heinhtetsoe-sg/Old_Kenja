function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //次へ/前へ
    if (cmd == 'back'|| cmd == 'next') {
        //前回検索値が入力されている状態、かつ検索結果の値の変更ができない状態(入試制度等、いずれかが変更不可)であれば、確認メッセージを出す。
        if (document.forms[0].HID_S_RECEPTNO.value != "" && document.forms[0].TESTDIV.disabled) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ( (document.forms[0].TESTDIV.disabled) || (document.forms[0].EXAMTYPE.disabled) ) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'read') {
        //必須チェック
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試種別）');
            return true;
        }
        if (document.forms[0].EXAMTYPE.value == ""){
            alert('{rval MSG304}'+ '\n（入試方式）');
            return true;
        }
    }

    //CSV出力
    if (cmd == "csvOutput" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //CSV取込
    if (cmd == "csvInput") {
        //必須チェック
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試種別）');
            return true;
        }
        if (document.forms[0].EXAMTYPE.value == ""){
            alert('{rval MSG304}'+ '\n（入試方式）');
            return true;
        }

        document.forms[0].btn_input.disabled  = true;
        document.forms[0].btn_output.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled  = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

//得点チェック
function CheckScore(obj) {
    if (obj.value != "*") {
        obj.value = toInteger(obj.value);
        if (obj.value > 999) {
            alert('{rval MSG901}' + '\n3桁以下で入力してください。');
            obj.focus();
            return;
        }
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value   = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_EXAMTYPE.value  = document.forms[0].EXAMTYPE.options[document.forms[0].EXAMTYPE.selectedIndex].value;

    document.forms[0].TESTDIV.disabled    = true;
    document.forms[0].EXAMTYPE.disabled   = true;

    document.getElementById(obj.id).style.background="yellow";
    obj.style.background="yellow";
}

//受験番号変更したら更新ボタンをグレーにする
function btn_disabled() {
    document.forms[0].btn_update.disabled = true;
}

//貼り付け機能
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("ORDERNO[]");

    if (document.forms[0].all_count.value > 1) {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"hairetu",
                   "objectNameArray" :nameArray,
                   "hairetuCnt"      :cnt
                   });
    } else {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"kotei",
                   "objectNameArray" :nameArray
                   });
    }
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, objCnt) {
    if (targetObject.value != val) {
        document.getElementById(targetObject.id).style.background = 'yellow';
        targetObject.style.background = 'yellow';
    }
    targetObject.value = val;
}

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
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

                    if (document.forms[0].all_count.value > 1) {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"][" + objCnt + "]");
                    } else {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"]");
                    }
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        if (clipTextArray[gyouCnt][retuCnt] == "*") {
                        } else if (isNaN(clipTextArray[gyouCnt][retuCnt])) {
                            alert('{rval MSG907}');
                            return false;
                        } else if (clipTextArray[gyouCnt][retuCnt] > 999) {
                            alert('{rval MSG901}' + '\n3桁以下で入力してください。');
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
