/* Add by PP for CurrentCursor 2020-01-20 start */
window.onload = function () {
    if (sessionStorage.getItem("KNJC035fForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJC035fForm1_CurrentCursor")).focus();
    }
    document.title = TITLE;
};

function current_cursor(para) {
    sessionStorage.setItem("KNJC035fForm1_CurrentCursor", para);
}

//定型文セット
function tmpSet(obj, id) {
    //数値チェック
    var value = obj.value;
    obj.value = toInteger(obj.value);
    if (obj.value != value) {
        document.getElementById(id).focus();
    }
}
/* Add by PP for CurrentCursor 2020-01-31 end */
function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2019-01-20 start */
    if (sessionStorage.getItem("KNJC035fForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJC035fForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2019-01-31 end */

    if (cmd == "course" && 0 < document.forms[0].COUNTER.value) {
        if (!confirm("{rval MSG108}")) {
            var cnt = document.forms[0]["LIST_COURSE_MAJOR" + document.forms[0].SELECT_COURSE_MAJOR.value].value;
            document.forms[0].COURSE_MAJOR[cnt].selected = true;
            return false;
        }
    }
    if (cmd == "subclasscd" && 0 < document.forms[0].COUNTER.value) {
        if (!confirm("{rval MSG108}")) {
            var cnt = document.forms[0]["LIST_SUBCLASSCD" + document.forms[0].SELECT_SUBCLASSCD.value].value;
            document.forms[0].SUBCLASSCD[cnt].selected = true;
            return false;
        }
    }
    if (cmd == "chaircd" && 0 < document.forms[0].COUNTER.value) {
        if (!confirm("{rval MSG108}")) {
            var cnt = document.forms[0]["LIST_CHAIRCD" + document.forms[0].SELECT_CHAIRCD.value].value;
            document.forms[0].CHAIRCD[cnt].selected = true;
            return false;
        }
    }
    if (cmd == "change" && 0 < document.forms[0].COUNTER.value) {
        if (!confirm("{rval MSG108}")) {
            var cnt = document.forms[0]["LIST_MONTH" + document.forms[0].SELECT_MONTH.value].value;
            document.forms[0].MONTHCD[cnt].selected = true;
            return false;
        }
    }
    //取消
    if (cmd == "reset" && !confirm("{rval MSG106}")) {
        return;
    }

    if (cmd == "update") {
        //データを格納
        if (document.forms[0].use_prg_schoolkind.value == "1") document.forms[0].HIDDEN_SCHOOL_KIND.value = document.forms[0].SCHOOL_KIND.value;
        if (document.forms[0].use_school_detail_gcm_dat.value == "1") document.forms[0].HIDDEN_COURSE_MAJOR.value = document.forms[0].COURSE_MAJOR.value;
        document.forms[0].HIDDEN_SUBCLASSCD.value = document.forms[0].SUBCLASSCD.value;
        document.forms[0].HIDDEN_CHAIRCD.value = document.forms[0].CHAIRCD.value;
        document.forms[0].HIDDEN_MONTHCD.value = document.forms[0].MONTHCD.value;
        document.forms[0].HIDDEN_LESSON_SET.value = document.forms[0].LESSON_SET.value;
        if (document.forms[0].MOVE_ENTER[0].checked == true) document.forms[0].HIDDEN_MOVE_ENTER.value = document.forms[0].MOVE_ENTER[0].value;
        if (document.forms[0].MOVE_ENTER[1].checked == true) document.forms[0].HIDDEN_MOVE_ENTER.value = document.forms[0].MOVE_ENTER[1].value;
        if (document.forms[0].EXECUTED.checked == true) document.forms[0].HIDDEN_EXECUTED.value = document.forms[0].EXECUTED.value;

        //使用不可項目
        if (document.forms[0].use_prg_schoolkind.value == "1") document.forms[0].SCHOOL_KIND.disabled = true;
        if (document.forms[0].use_school_detail_gcm_dat.value == "1") document.forms[0].COURSE_MAJOR.disabled = true;
        document.forms[0].SUBCLASSCD.disabled = true;
        document.forms[0].CHAIRCD.disabled = true;
        document.forms[0].MONTHCD.disabled = true;
        document.forms[0].LESSON_SET.disabled = true;
        document.forms[0].MOVE_ENTER[0].disabled = true;
        document.forms[0].MOVE_ENTER[1].disabled = true;
        document.forms[0].EXECUTED.disabled = true;
        document.forms[0].btn_reflect.disabled = true;
        if (document.forms[0].USE_LESSON.value == true) document.forms[0].btn_lesson_clear.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
        if (!document.forms[0].getPrgId.value) document.forms[0].btn_csv.disabled = true;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == "update") {
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
    //移動可能な生徒
    var useLine_array = document.forms[0].useLine.value.split(",");
    //移動可能な最初の行
    var firstLine = useLine_array[0];
    //移動可能な最終行
    var lastLine = useLine_array[useLine_array.length - 1];
    //最初の行の生徒か判定
    var isFirstStudent = cnt == firstLine ? true : false;
    //最終行の生徒か判定
    var isLastStudent = cnt == lastLine ? true : false;
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
                    targetObject = eval('document.forms[0]["' + textFieldArray[textFieldArray.length - 1] + '"][' + (cnt - 1) + "]");
                    targetObject.focus();
                    return;
                }
                targetObject = eval('document.forms[0]["' + textFieldArray[i - 1] + '"][' + cnt + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 38) {
                if (isFirstStudent) {
                    obj.focus();
                    return;
                }
                targetObject = eval('document.forms[0]["' + textFieldArray[i] + '"][' + (cnt - 1) + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 39 || moveEnt == 13) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastItem) {
                    for (var l = 0; l < useLine_array.length; l++) {
                        if (cnt < useLine_array[l]) {
                            targetObject = eval('document.forms[0]["' + textFieldArray[0] + '"][' + useLine_array[l] + "]");
                            targetObject.focus();
                            return;
                        }
                    }
                }
                targetObject = eval('document.forms[0]["' + textFieldArray[i + 1] + '"][' + cnt + "]");
                targetObject.focus();
                return;
            }
            if (moveEnt == 40) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastStudent) {
                    targetObject = eval('document.forms[0]["' + textFieldArray[i + 1] + '"][' + firstLine + "]");
                    targetObject.focus();
                    return;
                }
                for (var l = 0; l < useLine_array.length; l++) {
                    if (cnt < useLine_array[l]) {
                        targetObject = eval('document.forms[0]["' + textFieldArray[i] + '"][' + useLine_array[l] + "]");
                        targetObject.focus();
                        return;
                    }
                }
            }
        }
    }
}

//チェックボックスのラベル表示（出欠済・未）
function checkExecutedLabel(obj, id) {
    var zumi = document.getElementById(id);

    if (obj.checked) {
        zumi.innerHTML = '<font color="white">' + "出欠済" + "</font>";
    } else {
        zumi.innerHTML = '<font color="#ff0099">' + "出欠未" + "</font>";
    }
}

function closeFunc() {
    if (!confirm("{rval MSG108}")) {
        return;
    }
    /* Add by Kaung for PC-Talker 2020-02-10 start */
    document.getElementsByName("btn_end")[0].blur();
    document.title = "";
    /* Add by Kaung for PC-Talker 2020-02-20 end */
    top.opener.document.forms[0].btnAttend.disabled = true;
    top.opener.document.forms[0].btn_update.disabled = true;
    top.opener.document.forms[0].submit();
    top.window.close();
}

//授業時数反映処理
function reflect() {
    var lesson_set = "";

    //授業時数取得
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "LESSON_SET") {
            lesson_set = document.forms[0].elements[i].value;
        }
    }
    //授業時数セット
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/LESSON/) && document.forms[0].elements[i].name != "LESSON_SET") {
            //0かnullのときセットする
            if (document.forms[0].elements[i].value > 0) {
            } else if (document.forms[0].elements[i].disabled == false) {
                document.forms[0].elements[i].value = lesson_set;
                document.forms[0].elements[i].style.backgroundColor = "#ccffcc";
            }
        }
    }
}

//授業時数クリア処理
function lesson_clear() {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/LESSON/) && document.forms[0].elements[i].name != "LESSON_SET") {
            if (document.forms[0].elements[i].disabled == false) {
                document.forms[0].elements[i].value = "";
                document.forms[0].elements[i].style.backgroundColor = "#ccffcc";
            }
        }
    }
}

//事前チェック
function preCheck() {
    alert("{rval MSG305}\n（出欠管理者コントロール）");
    closeWin();
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm("内容を貼付けますか？")) {
        return false;
    }

    var nameArray = new Array();
    var fields = document.forms[0].copyField.value.split(":");

    for (fCnt = 0; fCnt < fields.length; fCnt++) {
        nameArray.push(fields[fCnt]);
    }

    if (document.forms[0].objCntSub.value > 1) {
        insertTsv({ clickedObj: obj, harituke_type: "hairetu", objectNameArray: nameArray, hairetuCnt: cnt });
    } else {
        insertTsv({ clickedObj: obj, harituke_type: "kotei", objectNameArray: nameArray });
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

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) {
        //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) {
            //テキストボックス名でまわす
            if (objectNameArray[k] == harituke_jouhou.clickedObj.name) {
                //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) {
                    //対象となるデータがあれば

                    if (document.forms[0].objCntSub.value > 1) {
                        targetObject = eval('document.forms[0]["' + objectNameArray[k] + '"][' + objCnt + "]");
                    } else {
                        targetObject = eval('document.forms[0]["' + objectNameArray[k] + '"]');
                    }
                    if (targetObject) {
                        //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        if (isNaN(clipTextArray[gyouCnt][retuCnt])) {
                            alert("{rval MSG907}");
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
