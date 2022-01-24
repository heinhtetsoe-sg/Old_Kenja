function btn_submit(cmd)
{
    if (cmd == 'reset'){
        if(document.forms[0].MONTH.value == "" || document.forms[0].HR_CLASS.value == "" ){
            confirm('{rval MSG304}');
            return;
        }else{
           if(!confirm('{rval MSG106}')){
            return;
           }
        }
    }

    if (cmd == 'update') {
        //データを格納
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1" || document.forms[0].useFi_Hrclass.value == "1") {
            if (document.forms[0].HR_CLASS_TYPE[0].checked == true) document.forms[0].HIDDEN_HR_CLASS_TYPE.value    = document.forms[0].HR_CLASS_TYPE[0].value;
            if (document.forms[0].HR_CLASS_TYPE[1].checked == true) document.forms[0].HIDDEN_HR_CLASS_TYPE.value    = document.forms[0].HR_CLASS_TYPE[1].value;
        }
        document.forms[0].HIDDEN_HR_CLASS.value = document.forms[0].HR_CLASS.value;
        document.forms[0].HIDDEN_MONTH.value    = document.forms[0].MONTH.value;

        //使用不可項目
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1" || document.forms[0].useFi_Hrclass.value == "1") {
            document.forms[0].HR_CLASS_TYPE[0].disabled = true;
            document.forms[0].HR_CLASS_TYPE[1].disabled = true;
        }
        document.forms[0].HR_CLASS.disabled = true;
        document.forms[0].MONTH.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;

        //リンクを使用不可
        var elem = document.getElementsByTagName("a");
        for(var i = 0; i < elem.length; ++i){
            elem[i].onclick = "return false;";
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

//子画面へ
function openSubWindow(URL) {
    if (document.forms[0].HR_CLASS.value == '') {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].MONTH.value == '') {
        alert('{rval MSG916}');
        return false;
    }

    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
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

    var setFieldArray = document.forms[0].SET_FIELD.value.split(",");
    for (var fieldCnt = 0; fieldCnt < setFieldArray.length; fieldCnt++) {
        nameArray[arCnt++] = setFieldArray[fieldCnt];
    }

    var setC002Array = document.forms[0].SET_FIELD_C002.value.split(",");
    for (var fieldCnt = 0; fieldCnt < setC002Array.length; fieldCnt++) {
        nameArray[arCnt++] = setC002Array[fieldCnt];
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

                        //数値チェック
                        err = false;
                        if (objectNameArray[k] == "DETAIL_101[]") {
                            chkNumData = clipTextArray[gyouCnt][retuCnt].toString();
                            if (clipTextArray[gyouCnt][retuCnt] > 999.9) {  //範囲
                                err = true;
                            } else if (chkNumData.length > 5) {             //桁数
                                err = true;
                            }
                        } else {
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
