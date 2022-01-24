function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return true;
        } else {
            document.forms[0].EXAMHALLCD.value = null;
        }
    }
    var hasData;
    if (cmd != 'reset' || cmd != 'update' || cmd != 'replace') {
        for (i=0; i < document.forms[0].elements.length; i++) {
            el = document.forms[0].elements[i];
            if (el.name.match(/^CHECKED/)) {
                hasData = true;
                break;
            }
        }
        if (hasData && !confirm('{rval MSG108}')) {
            document.forms[0].APPLICANTDIV.value     = document.forms[0].HIDDEN_APPLICANTDIV.value;
            document.forms[0].TESTDIV.value          = document.forms[0].HIDDEN_TESTDIV.value;
            if (document.forms[0].HIDDEN_EXAMHALL_TYPE.value == document.forms[0].EXAMHALL_TYPE[0].value)  document.forms[0].EXAMHALL_TYPE[0].checked = true;
            if (document.forms[0].HIDDEN_EXAMHALL_TYPE.value == document.forms[0].EXAMHALL_TYPE[1].value)  document.forms[0].EXAMHALL_TYPE[1].checked = true;
            return true;
        }
    }

    //チェックボックスのチェック有無
    var counter = 0;
    if (cmd == 'replace') {
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.match(/^CHECKED/) && document.forms[0].elements[i].checked == true) {
                counter++;
            }
        }
    }

    //更新
    if (cmd == 'update' || cmd == 'replace') {
        //更新対象生徒チェック
        if (cmd == 'replace') {
            if (counter == 0) {
                alert('{rval MSG304}\n( 一括設定対象 )');
                return false;
            }
            if (document.forms[0].EXAMHALLCD.value == '') {
                alert('{rval MSG304}\n( 一括設定：会場 )');
                return false;
            }
        }

        //データを格納
        document.forms[0].HIDDEN_APPLICANTDIV.value     = document.forms[0].APPLICANTDIV.value;
        document.forms[0].HIDDEN_TESTDIV.value          = document.forms[0].TESTDIV.value;
        if (document.forms[0].EXAMHALL_TYPE[0].checked == true)  document.forms[0].HIDDEN_EXAMHALL_TYPE.value   = document.forms[0].EXAMHALL_TYPE[0].value;
        if (document.forms[0].EXAMHALL_TYPE[1].checked == true)  document.forms[0].HIDDEN_EXAMHALL_TYPE.value   = document.forms[0].EXAMHALL_TYPE[1].value;
        document.forms[0].HIDDEN_EXAMHALLCD.value       = document.forms[0].EXAMHALLCD.value;

        //使用不可項目
        document.forms[0].APPLICANTDIV.disabled     = true;
        document.forms[0].TESTDIV.disabled          = true;
        document.forms[0].EXAMHALL_TYPE[0].disabled = true;
        document.forms[0].EXAMHALL_TYPE[1].disabled = true;
        document.forms[0].EXAMHALLCD.disabled       = true;
        document.forms[0].btn_replace.disabled      = true;
        document.forms[0].btn_update.disabled       = true;
        document.forms[0].btn_reset.disabled        = true;
        document.forms[0].btn_end.disabled          = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var el = document.forms[0].elements[i];
        if (el.name.match(/^CHECKED/) && el.disabled == false) {
            el.checked = obj.checked;
        }
    }
}

//選択した会場をセットする
function setCheckedExamhall(obj) {
    var i, el;
    var examno;
    var examhall;
    for (i=0; i < document.forms[0].elements.length; i++) {
        el = document.forms[0].elements[i];
        if (el.name.match(/^CHECKED/) && el.disabled == false && el.checked) {
            examno = el.id.substring("CHECKED-".length);
            var examhall = document.getElementsByName("EXAMHALLCD-" + examno);
            console.log(examhall, "EXAMHALLCD-" + examno);
            if (examhall[0] && examhall[0].value != document.forms[0].EXAMHALLCD.value) {
                examhall[0].value = document.forms[0].EXAMHALLCD.value;
            }
        }
    }
}
//エンターキーをTabに変換
function keydownEnter (obj, div) {
    var targetObject;
    var targetObjectform;
    if (div == '1') {
        var fieldName = 'EXAMHALLGROUPCD-';
    } else {
        var fieldName = 'EXAMHALLGROUP_ORDER-';
    }

    if (window.event.keyCode == '13') {
        //移動可能なオブジェクト
        var textFieldArray = document.forms[0].HID_EXAMNO.value.split(",");

        for (var i = 0; i < textFieldArray.length; i++) {
            if (fieldName + textFieldArray[i] == obj.name) {
                //シフト＋Enter押下
                if (window.event.shiftKey) {
                    targetObjectform = fieldName + textFieldArray[(i - 1)];
                    if (div == '2' && !textFieldArray[(i - 1)]) {
                        targetObjectform = 'EXAMHALLGROUPCD-' + textFieldArray[textFieldArray.length - 1];
                    }
                } else {
                    targetObjectform = fieldName + textFieldArray[(i + 1)];
                    if (div == '1' && !textFieldArray[(i + 1)]) {
                        targetObjectform = 'EXAMHALLGROUP_ORDER-' + textFieldArray[0];
                    }
                }
                targetObject = document.forms[0][targetObjectform];
                if (targetObject) {
                    targetObject.focus();
                    targetObject.select();
                }
                return;
            }
        }
    }
    return;
}
/**********************/
/****** 貼り付け ******/
/**********************/
function showPaste(obj, cnt) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    var nameArray     = new Array("EXAMHALLGROUPCD", 
                                  "EXAMHALLGROUP_ORDER");

    var renArray = new Array();
    var renArray = document.forms[0].HID_EXAMNO.value.split(",");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt"      :cnt,
               "renbanArray"     : renArray
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
                if (clipTextArray[j][i] != undefined) {
                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])){
                        alert('{rval MSG907}');
                        return false;
                    }
                    //長さチェック
                    if (String(clipTextArray[j][i]).length > 3){
                        alert('{rval MSG915}');
                        return false;
                    }

                }
                i++;
            }
        }
    }
    return true;
}
