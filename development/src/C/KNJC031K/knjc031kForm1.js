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

    if (cmd == 'update') {
        //データを格納
        document.forms[0].HIDDEN_HR_CLASS.value     = document.forms[0].HR_CLASS.value;
        document.forms[0].HIDDEN_MONTH.value        = document.forms[0].MONTH.value;
        if (document.forms[0].EXECUTED.checked == true) document.forms[0].HIDDEN_EXECUTED.value = document.forms[0].EXECUTED.value;

        //使用不可項目
        document.forms[0].HR_CLASS.disabled = true;
        document.forms[0].MONTH.disabled = true;
        document.forms[0].LESSON_SET.disabled = true;
        document.forms[0].EXECUTED.disabled = true;
        document.forms[0].btn_reflect.disabled = true;
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

//チェックボックスのラベル表示（出欠済・未）
function checkExecutedLabel(obj, id) {
    var zumi = document.getElementById(id);

    if (obj.checked) {
        zumi.innerHTML = '<font color="white">' + '出欠済' + '</font>';
    } else {
        zumi.innerHTML = '<font color="#ff0099">' + '出欠未' + '</font>';
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("LESSON[]");
    if (document.forms[0].unUseOffdays.value != "true") {
        nameArray.push("OFFDAYS[]");
    }
    if (document.forms[0].unUseAbroad.value != "true") {
        nameArray.push("ABROAD[]");
    }
    if (document.forms[0].unUseAbsent.value != "true") {
        nameArray.push("ABSENT[]");
    }
    nameArray.push("SUSPEND[]");
    if (document.forms[0].useKoudome.value == "true") {
        nameArray.push("KOUDOME[]");
    }
    if (document.forms[0].useVirus.value == "true") {
        nameArray.push("VIRUS[]");
    }
    nameArray.push("MOURNING[]");
    if (document.forms[0].SICK_FLG !== undefined) {
        nameArray.push("SICK[]");
    }
    if (document.forms[0].NOTICE_FLG !== undefined) {
        nameArray.push("NOTICE[]");
    }
    if (document.forms[0].NONOTICE_FLG !== undefined) {
        nameArray.push("NONOTICE[]");
    }
    nameArray.push("LATE[]");
    nameArray.push("EARLY[]");
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
                    }
                }
                retuCnt++;
            }
        }
        objCnt++;
    }
    return true;
}

