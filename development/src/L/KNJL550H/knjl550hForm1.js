String.prototype.bytes = function () {
  return(encodeURIComponent(this).replace(/%../g,"x").length);
}

function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update") {
        if (document.forms[0].HID_EXAMNO.value.length == 0) {
            return false;
        }
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].APPLICANTDIV.disabled) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'next' || cmd == 'back') {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（学校種別）');
            return true;
        }
        if (document.forms[0].DISTINCT_ID.value == ""){
            alert('{rval MSG304}'+ '\n（入試判別）');
            return true;
        }
    }

    //コンボ変更
    if (cmd == 'read') {
        document.forms[0].S_EXAMNO.value = '';
        document.forms[0].E_EXAMNO.value = '';
    }

    //CSV出力
    if (cmd == "csvOutput" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //CSV取込
    if (cmd == "csvInput") {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled  = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

// 値を変えたら、キーを変更不可にする。
function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_YEAR.value            = document.forms[0].YEAR.options[document.forms[0].YEAR.selectedIndex].value;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_DISTINCT_ID.value     = document.forms[0].DISTINCT_ID.options[document.forms[0].DISTINCT_ID.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value  = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].HID_S_EXAMNO.value        = document.forms[0].S_EXAMNO.value;
    document.forms[0].HID_E_EXAMNO.value        = document.forms[0].E_EXAMNO.value;

    obj.style.background = "yellow";

    document.forms[0].YEAR.disabled             = true;
    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].DISTINCT_ID.disabled      = true;
    document.forms[0].TESTSUBCLASSCD.disabled   = true;
    document.forms[0].S_EXAMNO.disabled         = true;
    document.forms[0].E_EXAMNO.disabled         = true;
    document.forms[0].btn_read.disabled         = true;
    document.forms[0].btn_back.disabled         = true;
    document.forms[0].btn_next.disabled         = true;
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_EXAMNO.value.split(',');
        var index = setArr.indexOf(obj.id);
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
            var targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (setArr.length - 1); i++) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}
/**********************/
/****** 貼り付け ******/
/**********************/
function showPaste(obj, cnt) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    var nameArray = new Array('SCORE');

    var renArray = new Array();
    var examNoArray = document.forms[0].RECEPTNO_REN.value.split(",");
    for (var i = 0; i < examNoArray.length; i++) {
        renArray[i] = examNoArray[i];
    }

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
