function btn_submit(cmd) {
    if (cmd == 'change_group' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_GROUP_HR_CLASS" + document.forms[0].SELECT_GROUP_HR_CLASS.value].value;
            document.forms[0].GROUP_HR_CLASS[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'change_hrclass' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_HR_CLASS" + document.forms[0].SELECT_HR_CLASS.value].value;
            document.forms[0].HR_CLASS[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'change_month' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_MONTH" + document.forms[0].SELECT_MONTH.value].value;
            document.forms[0].MONTH[cnt].selected = true;
            return false;
        }
    }
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return;
        }
    }

    //更新
    if (cmd == 'update') {
        if (document.forms[0].useSpecial_Support_Hrclass.value == 1) {
            if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) {
                if (document.forms[0].GROUP_HR_CLASS.value == '') {
                    alert('{rval MSG916}\n　　　( 学級 )');
                    return;
                }
            } else {
                if (document.forms[0].HR_CLASS.value == '') {
                    alert('{rval MSG916}\n　　　( 学級 )');
                    return;
                }
            }
        } else {
            if (document.forms[0].HR_CLASS.value == '') {
                alert('{rval MSG916}\n　　　( 学級 )');
                return;
            }
        }
        if (document.forms[0].MONTH.value == '') {
            alert('{rval MSG916}\n　　　( 対象月 )');
            return;
        }

        //データを格納
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1") {
            if (document.forms[0].SELECT_CLASS_TYPE[0].checked == true) document.forms[0].HIDDEN_SELECT_CLASS_TYPE.value    = document.forms[0].SELECT_CLASS_TYPE[0].value;
            if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) document.forms[0].HIDDEN_SELECT_CLASS_TYPE.value    = document.forms[0].SELECT_CLASS_TYPE[1].value;
            if (document.forms[0].SELECT_CLASS_TYPE[0].checked == true) document.forms[0].HIDDEN_HR_CLASS.value         = document.forms[0].HR_CLASS.value;
            if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) document.forms[0].HIDDEN_GROUP_HR_CLASS.value   = document.forms[0].GROUP_HR_CLASS.value;
        } else {
            document.forms[0].HIDDEN_HR_CLASS.value         = document.forms[0].HR_CLASS.value;
        }
        document.forms[0].HIDDEN_MONTH.value            = document.forms[0].MONTH.value;
        document.forms[0].HIDDEN_LESSON_SET.value       = document.forms[0].LESSON_SET.value;
        if (document.forms[0].MOVE_ENTER[0].checked == true) document.forms[0].HIDDEN_MOVE_ENTER.value  = document.forms[0].MOVE_ENTER[0].value;
        if (document.forms[0].MOVE_ENTER[1].checked == true) document.forms[0].HIDDEN_MOVE_ENTER.value  = document.forms[0].MOVE_ENTER[1].value;

        //使用不可項目
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1") {
            document.forms[0].SELECT_CLASS_TYPE[0].disabled = true;
            document.forms[0].SELECT_CLASS_TYPE[1].disabled = true;
            if (document.forms[0].SELECT_CLASS_TYPE[0].checked == true) document.forms[0].HR_CLASS.disabled = true;
            if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) document.forms[0].GROUP_HR_CLASS.disabled = true;
        } else {
            document.forms[0].HR_CLASS.disabled = true;
        }
        document.forms[0].MONTH.disabled = true;
        document.forms[0].LESSON_SET.disabled = true;
        document.forms[0].MOVE_ENTER[0].disabled = true;
        document.forms[0].MOVE_ENTER[1].disabled = true;
        document.forms[0].btn_reflect.disabled = true;
        document.forms[0].btn_lesson_clear.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
        document.forms[0].btn_print.disabled = true;
        document.forms[0].btn_csv.disabled = true;
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

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].useSpecial_Support_Hrclass.value == 1) {
        if (document.forms[0].SELECT_CLASS_TYPE[1].checked == true) {
            if (document.forms[0].GROUP_HR_CLASS.value == '') {
                alert('{rval MSG916}\n　　　( 学級 )');
                return false;
            }
        } else {
            if (document.forms[0].HR_CLASS.value == '') {
                alert('{rval MSG916}\n　　　( 学級 )');
                return false;
            }
        }
    } else {
        if (document.forms[0].HR_CLASS.value == '') {
            alert('{rval MSG916}\n　　　( 学級 )');
            return false;
        }
    }
    if (document.forms[0].MONTH.value == '') {
        alert('{rval MSG916}\n　　　( 対象月 )');
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

//数字チェック
function NumCheck(num) {
    num = toFloat(num);

    //文字チェック
    var n = num.split(".").length - 1;
    if (n > 1) {
        alert('{rval MSG907}\n入力された文字列は削除されます。');
        num = '';
    }
    //範囲チェック
    if (num > 999.9) {
        alert('{rval MSG916}\n( 0 ～ 999.9 )');
        num = '';
    }
    return num;
}

//授業日数反映処理
function reflect() {
    var lesson_set = "";

    //授業日数取得
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "LESSON_SET") {
            lesson_set = document.forms[0].elements[i].value;
        }
    }
    //授業日数セット
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/LESSON/) && document.forms[0].elements[i].name != "LESSON_SET") {
            //0かnullのときセットする
            if (document.forms[0].elements[i].value > 0) {
            } else {
                document.forms[0].elements[i].value = lesson_set;
                document.forms[0].elements[i].style.backgroundColor = "#ccffcc";
            }
        }
    }
}

//授業日数クリア処理
function lesson_clear() {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/LESSON/) && document.forms[0].elements[i].name != "LESSON_SET") {
            document.forms[0].elements[i].value = "";
            document.forms[0].elements[i].style.backgroundColor = "#ccffcc";
        }
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("LESSON[]");

    var nameArray = new Array("LESSON[]");
    var fields = document.forms[0].copyField.value.split(":");

    for (fCnt = 0; fCnt < fields.length; fCnt++) {
        nameArray.push(fields[fCnt]);
    }
    nameArray.push("REMARK[]");

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
function execCopy(targetObject, val, objCnt) {
    targetObject.value = val;
};


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

