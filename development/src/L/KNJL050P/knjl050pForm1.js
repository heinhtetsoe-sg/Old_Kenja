function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled) || (document.forms[0].S_RECEPTNO.disabled) || (document.forms[0].E_RECEPTNO.disabled)) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'read') {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert('{rval MSG304}'+ '\n（入試制度）');
            return true;
        }
        if (document.forms[0].TESTDIV.value == "") {
            alert('{rval MSG304}'+ '\n（入試区分）');
            return true;
        }
        if (document.forms[0].TESTSUBCLASSCD.value == "") {
            alert('{rval MSG304}'+ '\n（受験科目）');
            return true;
        }
        if (document.forms[0].S_RECEPTNO.value == "") {
            alert('{rval MSG304}'+ '\n（開始受験番号）');
            return true;
        }
        if (document.forms[0].E_RECEPTNO.value == "") {
            alert('{rval MSG304}'+ '\n（終了受験番号）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

//エンターキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECEPTNO.value.split(',');
        var index = setArr.indexOf(obj.id);
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
        }
        var targetId = setArr[index];
        document.getElementById(targetId).focus();
        return false;
    }
}

//得点チェック
function CheckScore(obj) {
    if (obj.value != "*") {
        obj.value = toInteger(obj.value);
        if (obj.value > eval(aPerfect[obj.id])) {
            alert('{rval MSG901}' + '\n満点：'+aPerfect[obj.id]+'以下で入力してください。');
            obj.focus();
            return;
        }
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].HID_S_RECEPTNO.value      = document.forms[0].S_RECEPTNO.value;
    document.forms[0].HID_E_RECEPTNO.value      = document.forms[0].E_RECEPTNO.value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].TESTSUBCLASSCD.disabled = true;
    document.forms[0].S_RECEPTNO.disabled       = true;
    document.forms[0].E_RECEPTNO.disabled       = true;
    document.forms[0].btn_read.disabled         = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
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
    var nameArray = new Array("SCORE[]");

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
    if (targetObject.readOnly) { //欠席者は表示する。但し、入力不可とする。
        return true;
    }
    if (targetObject.value != val) {
        document.getElementById('ROWID' + targetObject.id).style.background = 'yellow';
        targetObject.style.background = 'yellow';
    }
    targetObject.value = val;
//    return true;
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
                        } else if (clipTextArray[gyouCnt][retuCnt] > eval(aPerfect[targetObject.id])) {
                            alert('{rval MSG901}' + '\n満点：'+aPerfect[targetObject.id]+'以下で入力してください。');
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
