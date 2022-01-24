function btn_submit(cmd)
{

    if (cmd == 'reset'){
        if(document.forms[0].MONTH.value=="" || document.forms[0].HR_CLASS.value=="" ){
            confirm('{rval MSG304}');
            return;
        }else{
           if(!confirm('{rval MSG106}')){
            return;
           }
        }
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
    if (document.forms[0].HR_CLASS.value == '') {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].MONTH.value == '') {
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

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array();
    var arCnt = 0;
    nameArray[arCnt++] = "LESSON[]";
    nameArray[arCnt++] = "OFFDAYS[]";
    nameArray[arCnt++] = "ABROAD[]";
    nameArray[arCnt++] = "ABSENT[]";
    nameArray[arCnt++] = "SUSPEND[]";

    if (document.forms[0].SET_SUSPEND_FIELD.value) {
        var setFieldArray = document.forms[0].SET_SUSPEND_FIELD.value.split(",");
        for (var fieldCnt = 0; fieldCnt < setFieldArray.length; fieldCnt++) {
            nameArray[arCnt++] = setFieldArray[fieldCnt];
        }
    }

    nameArray[arCnt++] = "MOURNING[]";

    var setFieldArray = document.forms[0].SET_FIELD.value.split(",");
    for (var fieldCnt = 0; fieldCnt < setFieldArray.length; fieldCnt++) {
        nameArray[arCnt++] = setFieldArray[fieldCnt];
    }
    nameArray[arCnt++] = "LATE[]";
    nameArray[arCnt++] = "EARLY[]";
    var setFieldArray = document.forms[0].SET_DETAIL_FIELD.value.split(",");
    for (var fieldCnt = 0; fieldCnt < setFieldArray.length; fieldCnt++) {
        nameArray[arCnt++] = setFieldArray[fieldCnt];
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

