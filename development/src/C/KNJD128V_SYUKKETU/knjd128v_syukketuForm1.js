function btn_submit(cmd) {

    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab2(obj, setTextField, cnt) {
    //移動可能なオブジェクト
    var textFieldArray = setTextField.split(",");
    //行数
    var lineCnt = document.forms[0]["LESSON[]"].length;
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
    var moveEnt = document.forms[0].MOVE_ENTER[0].checked ? 40 : 39;
    for (var i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] == obj.name) {
            var isFirstItem = i == 0 ? true : false;
            var isLastItem = i == textFieldArray.length - 1 ? true : false;
            if (moveEnt == 37) {
                if (isFirstItem && isFirstStudent) {
                    obj.focus();
                    return;
                }
                if (isFirstItem) {
                    targetObject = eval("document.forms[0][\"" + textFieldArray[(textFieldArray.length - 1)] + "\"][" + (cnt - 1) + "]");
                    targetObject.focus();
                    return;
                }
                targetObject = eval("document.forms[0][\"" + textFieldArray[(i - 1)] + "\"][" + cnt + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 38) {
                if (isFirstStudent) {
                    obj.focus();
                    return;
                }
                targetObject = eval("document.forms[0][\"" + textFieldArray[i] + "\"][" + (cnt - 1) + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 39 || moveEnt == 13) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastItem) {
                    targetObject = eval("document.forms[0][\"" + textFieldArray[0] + "\"][" + (cnt + 1) + "]");
                    targetObject.focus();
                    return;
                }
                targetObject = eval("document.forms[0][\"" + textFieldArray[(i + 1)] + "\"][" + cnt + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 40) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastStudent) {
                    targetObject = eval("document.forms[0][\"" + textFieldArray[i + 1] + "\"][0]");
                    targetObject.focus();
                    return;
                }
                targetObject = eval("document.forms[0][\"" + textFieldArray[i] + "\"][" + (cnt + 1) + "]");
                targetObject.focus();
                return;
            }
        }
    }
}

function newwin(SERVLET_URL) {
    if (document.forms[0].SUBCLASSCD.value == '') {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].CHAIRCD.value == '') {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].MONTHCD.value == '') {
        alert('{rval MSG916}');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function closeFunc() {
    if (!confirm('{rval MSG108}')){
        return;
    }
    top.opener.document.forms[0].btnAttend.disabled = true;
    top.opener.document.forms[0].btn_update.disabled = true;
    top.opener.document.forms[0].submit();
    top.window.close();
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    var nameArray = new Array("LESSON[]");
    var fields = document.forms[0].copyField.value.split(":");

    for (fCnt = 0; fCnt < fields.length; fCnt++) {
        nameArray.push(fields[fCnt]);
    }

    if (document.forms[0].objCntSub.value > 1) {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"hairetu",
                   "objectNameArray" :nameArray,
                   "hairetuCnt" :cnt
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

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
}

/***********************************/
/* クリップボードの中身のチェック  */
/* (だめなデータならばfalseを返す) */
/* (共通関数から呼ばれる)          */
/***********************************/
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

                    if (document.forms[0].objCntSub.value > 1) {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"][" + objCnt + "]");
                    } else {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"]");
                    }
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        if (isNaN(clipTextArray[gyouCnt][retuCnt])){
                            alert('{rval MSG907}');
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
