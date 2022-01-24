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
        if (document.forms[0].TESTDIV.disabled) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'next' || cmd == 'back') {
        //必須チェック
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（志望区分）');
            return true;
        }
        if (document.forms[0].EVALTYPE.value == ""){
            alert('{rval MSG304}'+ '\n（評価項目）');
            return true;
        }
    }

    //コンボ変更
    if (cmd == 'read') {
        document.forms[0].S_FINDNO.value = '';
        document.forms[0].E_FINDNO.value = '';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//得点チェック
function CheckScore(obj) {
    if (obj.value != "*") {
        obj.value = toInteger(obj.value);
        if (obj.value > eval(aPerfect[obj.id])) {
            alert('{rval MSG901}' + '\n満点：'+aPerfect[obj.id]+'以下で入力してください。');
            obj.focus();
            obj.style.background="yellow";
            return;
        }
    }
//    if (obj.style.background == "yellow") {
//        obj.style.background="white";
//    }
}

//非活性制御
function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_EVALTYPE.value  = document.forms[0].EVALTYPE.options[document.forms[0].EVALTYPE.selectedIndex].value;
    document.forms[0].HID_S_FINDNO.value      = document.forms[0].S_FINDNO.value;
    document.forms[0].HID_E_FINDNO.value      = document.forms[0].E_FINDNO.value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].EVALTYPE.disabled         = true;
    document.forms[0].S_FINDNO.disabled       = true;
    document.forms[0].E_FINDNO.disabled       = true;

//    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
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
    if (targetObject.readOnly == false) {
        if (targetObject.value != val) {
            targetObject.style.background = 'yellow';
        }
        targetObject.value = val;
    }
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
