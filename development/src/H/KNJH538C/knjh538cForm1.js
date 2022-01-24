function btn_submit(cmd)
{

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJH";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function checkVal(obj, perfect) {
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = "";
        obj.focus();
        return false;
    }
    if (document.forms[0].useProficiencyPerfect.value) {
        if (obj.value > perfect) {
            alert('満点は' + perfect + '点です。');
            obj.value = "";
            obj.focus();
            return false;
        }
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SCORE[]");

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
    if(String(val) != 0) {
        targetObject.value = String(val).replace(/^0*/,"");
    } else {
        targetObject.value = String(val).replace(/^0*/,0);
    }
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
                        if (isNaN(clipTextArray[gyouCnt][retuCnt]) && "*" != clipTextArray[gyouCnt][retuCnt]){
                            alert('{rval MSG907}\n又は、「*」を入力して下さい。');
                            targetObject.value = "";
                            targetObject.focus();
                            return false;
                        }
                        if (document.forms[0].useProficiencyPerfect.value) {
                            var perfectArray = targetObject.className.split('_');
                            if (parseInt(clipTextArray[gyouCnt][retuCnt], 10) > parseInt(perfectArray[1], 10)) {
                                //満点は○点です。 + テキストの親(TD)の親(TR)の氏名欄(cells[1])データ
                                alert('満点は' + perfectArray[1] + '点です。\n' + targetObject.parentNode.parentNode.cells[1].innerHTML);
                                targetObject.value = "";
                                targetObject.focus();
                                return false;
                            }
                        }
                        if (clipTextArray[gyouCnt][retuCnt].length > 3){
                            alert('{rval MSG901}\n3桁までです');
                            targetObject.value = "";
                            targetObject.focus();
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

