function btn_submit(cmd) {
    //変更確認
    if (cmd == 'change_course' && 0 < document.forms[0].objCntSub.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_COURSE" + document.forms[0].SELECT_COURSE.value].value;
            document.forms[0].COURSE_MAJOR[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'change_radio' && 0 < document.forms[0].objCntSub.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_SELECT_CLASS_TYPE" + document.forms[0].SELECT_SELECT_CLASS_TYPE.value].value;
            document.forms[0].SELECT_CLASS_TYPE[cnt].checked = true;
            return false;
        }
    }
    if (document.forms[0].useSpecial_Support_Hrclass.value == '1') {
        if (cmd == 'change_class' && document.forms[0].SELECT_CLASS_TYPE[1].checked == true && 0 < document.forms[0].objCntSub.value) {
            if (!confirm('{rval MSG108}')) {
                var cnt = document.forms[0]["LIST_GROUP_HR_CLASS" + document.forms[0].SELECT_GROUP_HR_CLASS.value].value;
                document.forms[0].GROUP_HR_CLASS[cnt].selected = true;
                return false;
            }
        }
        if (cmd == 'change_class' && document.forms[0].SELECT_CLASS_TYPE[0].checked == true && 0 < document.forms[0].objCntSub.value) {
            if (!confirm('{rval MSG108}')) {
                var cnt = document.forms[0]["LIST_HR_CLASS" + document.forms[0].SELECT_HR_CLASS.value].value;
                document.forms[0].HR_CLASS[cnt].selected = true;
                return false;
            }
        }
    } else {
        if (cmd == 'change_class' && 0 < document.forms[0].objCntSub.value) {
            if (!confirm('{rval MSG108}')) {
                var cnt = document.forms[0]["LIST_HR_CLASS" + document.forms[0].SELECT_HR_CLASS.value].value;
                document.forms[0].HR_CLASS[cnt].selected = true;
                return false;
            }
        }
    }
    if (cmd == 'change' && 0 < document.forms[0].objCntSub.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_SCHREGNO" + document.forms[0].SELECT_SCHREGNO.value].value;
            document.forms[0].SCHREGNO[cnt].selected = true;
            return false;
        }
    }

    //更新
    if (cmd == 'update') {
        var i = document.forms[0].SCHREGNO.selectedIndex;
        if (document.forms[0].SCHREGNO.options[i].value == '') {
            alert('{rval MSG304}');
            return false;
        }

        //データを格納
        if (document.forms[0].use_school_detail_gcm_dat.value == "1") document.forms[0].HIDDEN_COURSE_MAJOR.value = document.forms[0].COURSE_MAJOR.value;
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1") {
            if (document.forms[0].SELECT_CLASS_TYPE[0].checked == true) document.forms[0].HIDDEN_SELECT_CLASS_TYPE.value    = document.forms[0].SELECT_CLASS_TYPE[0].value;
            if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) document.forms[0].HIDDEN_SELECT_CLASS_TYPE.value    = document.forms[0].SELECT_CLASS_TYPE[1].value;
            if (document.forms[0].SELECT_CLASS_TYPE[0].checked == true) document.forms[0].HIDDEN_HR_CLASS.value         = document.forms[0].HR_CLASS.value;
            if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) document.forms[0].HIDDEN_GROUP_HR_CLASS.value   = document.forms[0].GROUP_HR_CLASS.value;
        } else {
            document.forms[0].HIDDEN_HR_CLASS.value = document.forms[0].HR_CLASS.value;
        }
        document.forms[0].HIDDEN_SCHREGNO.value = document.forms[0].SCHREGNO.value;
        if (document.forms[0].MOVE_ENTER[0].checked == true) document.forms[0].HIDDEN_MOVE_ENTER.value  = document.forms[0].MOVE_ENTER[0].value;
        if (document.forms[0].MOVE_ENTER[1].checked == true) document.forms[0].HIDDEN_MOVE_ENTER.value  = document.forms[0].MOVE_ENTER[1].value;

        //使用不可項目
        if (document.forms[0].use_school_detail_gcm_dat.value == "1") document.forms[0].COURSE_MAJOR.disabled = true;
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1") {
            document.forms[0].SELECT_CLASS_TYPE[0].disabled = true;
            document.forms[0].SELECT_CLASS_TYPE[1].disabled = true;
            if (document.forms[0].SELECT_CLASS_TYPE[0].checked == true) document.forms[0].HR_CLASS.disabled = true;
            if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) document.forms[0].GROUP_HR_CLASS.disabled = true;
        } else {
            document.forms[0].HR_CLASS.disabled = true;
        }
        document.forms[0].SCHREGNO.disabled = true;
        document.forms[0].MOVE_ENTER[0].disabled = true;
        document.forms[0].MOVE_ENTER[1].disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

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
    var lineCnt = document.forms[0][textFieldArray[0]].length;
    //移動可能な行
    var useLine_array = document.forms[0].useLine.value.split(",");
    //移動可能な最初の行
    var firstLine = useLine_array[0];
    //移動可能な最終行
    var lastLine = useLine_array[useLine_array.length - 1];
    //最初の行か判定
    var isFirstMonth = cnt == firstLine ? true : false;
    //最終行か判定
    var isLastMonth = cnt == lastLine ? true : false;
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
                if (isFirstMonth) {
                    obj.focus();
                    return;
                }
                targetObject = eval("document.forms[0][\"" + textFieldArray[i] + "\"][" + (cnt - 1) + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 39 || moveEnt == 13) {
                if (isLastItem && isLastMonth) {
                    obj.focus();
                    return;
                }
                if (isLastItem) {
                    for (var l = 0; l < useLine_array.length; l++) {
                        if (cnt < useLine_array[l]) {
                            targetObject = eval("document.forms[0][\"" + textFieldArray[0] + "\"][" + useLine_array[l] + "]");
                            targetObject.focus();
                            return;
                        }
                    }
                }
                targetObject = eval("document.forms[0][\"" + textFieldArray[(i + 1)] + "\"][" + cnt + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 40) {
                if (isLastItem && isLastMonth) {
                    obj.focus();
                    return;
                }
                if (isLastMonth) {
                    targetObject = eval("document.forms[0][\"" + textFieldArray[i + 1] + "\"][" + firstLine + "]");
                    targetObject.focus();
                    return;
                }
                for (var l = 0; l < useLine_array.length; l++) {
                    if (cnt < useLine_array[l]) {
                        targetObject = eval("document.forms[0][\"" + textFieldArray[i] + "\"][" + useLine_array[l] + "]");
                        targetObject.focus();
                        return;
                    }
                }
            }
        }
    }
}

//事前チェック
function preCheck() {
    alert('{rval MSG305}\n（出欠管理者コントロール）');
    closeWin();
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array();
    var fields = document.forms[0].copyField.value.split(":");
    for (fCnt = 0; fCnt < fields.length; fCnt++) {
        nameArray.push(fields[fCnt]);
    }

    if (document.forms[0].objCntSub.value > 1) {
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
                        //数値チェック（備考を除く）
                        if (isNaN(clipTextArray[gyouCnt][retuCnt]) && objectNameArray[k] != "REMARK[]"){
                            alert('{rval MSG907}');
                            return false;
                        }

                        //数値チェック
                        err = false;
                        if (objectNameArray[k] == "DETAIL_101[]") {
                            chkNumData = clipTextArray[gyouCnt][retuCnt].toString();
                            if (clipTextArray[gyouCnt][retuCnt] > 999.9) {  //範囲
                                err = true;
                            } else if (chkNumData.length > 5) {             //桁数
                                err = true;
                            }
                        } else if (objectNameArray[k] != "REMARK[]") {
                            if (clipTextArray[gyouCnt][retuCnt] > 999) {    //範囲
                                err = true;
                            }
                        }
                        //エラーを返す
                        if (err) {
                            alert('{rval MSG916}');
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
