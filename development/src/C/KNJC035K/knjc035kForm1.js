function btn_submit(cmd) {

    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    //更新
    if (cmd == 'update') {
        //データを格納
        document.forms[0].HIDDEN_SUBCLASSCD.value   = document.forms[0].SUBCLASSCD.value;
        document.forms[0].HIDDEN_CHAIRCD.value      = document.forms[0].CHAIRCD.value;
        document.forms[0].HIDDEN_MONTHCD.value      = document.forms[0].MONTHCD.value;
        document.forms[0].HIDDEN_LESSON_SET.value   = document.forms[0].LESSON_SET.value;

        //使用不可項目
        document.forms[0].SUBCLASSCD.disabled = true;
        document.forms[0].CHAIRCD.disabled = true;
        document.forms[0].MONTHCD.disabled = true;
        document.forms[0].LESSON_SET.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
        document.forms[0].btn_print.disabled = true;
        document.forms[0].btn_csv.disabled = true;
        document.forms[0].btn_reflect.disabled = true;
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

//授業時数反映処理
function reflect() {
    var lesson_set = "";

    //授業時数取得
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "LESSON_SET") {
            lesson_set = document.forms[0].elements[i].value;
        }
    }
    //授業時数セット
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

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }


    //テキストボックスの名前の配列を作る
    var delSick = 7;
    var delNotice = 7;
    var delNonotice = 7;
    var nameCnt = 0;
    var nameArray = new Array();
    if (document.forms[0].useVirus.value == "true" && document.forms[0].useKoudome.value == "true") {
        nameArray[nameCnt++] = "LESSON[]";
        nameArray[nameCnt++] = "OFFDAYS[]";
        nameArray[nameCnt++] = "ABROAD[]";
        nameArray[nameCnt++] = "ABSENT[]";
        nameArray[nameCnt++] = "SUSPEND[]";
        nameArray[nameCnt++] = "KOUDOME[]";
        nameArray[nameCnt++] = "VIRUS[]";
        nameArray[nameCnt++] = "MOURNING[]";
        if (document.forms[0].DISP_SICK.value == "4") {
            nameArray[nameCnt++] = "SICK[]";
        }
        if (document.forms[0].DISP_NOTICE.value == "5") {
            nameArray[nameCnt++] = "NOTICE[]";
        }
        if (document.forms[0].DISP_NONOTICE.value == "6") {
            nameArray[nameCnt++] = "NONOTICE[]";
        }
        nameArray[nameCnt++] = "NURSEOFF[]";
        nameArray[nameCnt++] = "LATE[]";
        nameArray[nameCnt++] = "EARLY[]";
    } else if (document.forms[0].useKoudome.value == "true") {
        nameArray[nameCnt++] = "LESSON[]";
        nameArray[nameCnt++] = "OFFDAYS[]";
        nameArray[nameCnt++] = "ABROAD[]";
        nameArray[nameCnt++] = "ABSENT[]";
        nameArray[nameCnt++] = "SUSPEND[]";
        nameArray[nameCnt++] = "KOUDOME[]";
        nameArray[nameCnt++] = "MOURNING[]";
        if (document.forms[0].DISP_SICK.value == "4") {
            nameArray[nameCnt++] = "SICK[]";
        }
        if (document.forms[0].DISP_NOTICE.value == "5") {
            nameArray[nameCnt++] = "NOTICE[]";
        }
        if (document.forms[0].DISP_NONOTICE.value == "6") {
            nameArray[nameCnt++] = "NONOTICE[]";
        }
        nameArray[nameCnt++] = "NURSEOFF[]";
        nameArray[nameCnt++] = "LATE[]";
        nameArray[nameCnt++] = "EARLY[]";
    } else if (document.forms[0].useVirus.value == "true") {
        nameArray[nameCnt++] = "LESSON[]";
        nameArray[nameCnt++] = "OFFDAYS[]";
        nameArray[nameCnt++] = "ABROAD[]";
        nameArray[nameCnt++] = "ABSENT[]";
        nameArray[nameCnt++] = "SUSPEND[]";
        nameArray[nameCnt++] = "VIRUS[]";
        nameArray[nameCnt++] = "MOURNING[]";
        if (document.forms[0].DISP_SICK.value == "4") {
            nameArray[nameCnt++] = "SICK[]";
        }
        if (document.forms[0].DISP_NOTICE.value == "5") {
            nameArray[nameCnt++] = "NOTICE[]";
        }
        if (document.forms[0].DISP_NONOTICE.value == "6") {
            nameArray[nameCnt++] = "NONOTICE[]";
        }
        nameArray[nameCnt++] = "NURSEOFF[]";
        nameArray[nameCnt++] = "LATE[]";
        nameArray[nameCnt++] = "EARLY[]";
    } else {
        nameArray[nameCnt++] = "LESSON[]";
        nameArray[nameCnt++] = "OFFDAYS[]";
        nameArray[nameCnt++] = "ABROAD[]";
        nameArray[nameCnt++] = "ABSENT[]";
        nameArray[nameCnt++] = "SUSPEND[]";
        nameArray[nameCnt++] = "MOURNING[]";
        if (document.forms[0].DISP_SICK.value == "4") {
            nameArray[nameCnt++] = "SICK[]";
        }
        if (document.forms[0].DISP_NOTICE.value == "5") {
            nameArray[nameCnt++] = "NOTICE[]";
        }
        if (document.forms[0].DISP_NONOTICE.value == "6") {
            nameArray[nameCnt++] = "NONOTICE[]";
        }
        nameArray[nameCnt++] = "NURSEOFF[]";
        nameArray[nameCnt++] = "LATE[]";
        nameArray[nameCnt++] = "EARLY[]";
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
