function btn_submit(cmd)
{
    if (cmd == 'main') {
        if (document.forms[0].changeVal !== undefined && document.forms[0].changeVal.value == '1') {
            alert('保存されていないデータは破棄されます。');
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return;
        }
    }
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }

    if (cmd == 'update' && document.forms[0].AUTHORITY.value < document.forms[0].DEF_UPDATE_RESTRICT.value) {
        alert('{rval MSG300}');
        document.forms[0].cmd.value = 'reset';
        document.forms[0].submit();
        return false;
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

function checkVal(obj, perfect, cnt) {
    if (isNaN(obj.value) && "*" != obj.value) {
        alert('{rval MSG907}\n又は、「*」を入力して下さい。');
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
//    targetObject = eval("document.forms[0][\"" + obj.name.replace('[]', '2[]') + "\"][" + cnt + "]");
    document.getElementById(obj.name.replace('[]', '') + cnt).innerHTML = obj.value;
}

function data_clear(clearName) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var obj_updElement = document.forms[0].elements[i];
        re = new RegExp("^" + clearName);
        if (obj_updElement.name.match(re) && obj_updElement.type == "text") {
            obj_updElement.value = "";
            obj_updElement.style.background='#ccffcc';
            document.forms[0].changeVal.value = '1';
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
    if (targetObject.value != val) {
        targetObject.style.background = '#ccffcc';
        document.forms[0].changeVal.value = '1';
    }
    if(String(val) != 0) {
        targetObject.value = String(val).replace(/^0+/,"");
    } else {
        targetObject.value = String(val).replace(/^0+/,0);
    }
    document.getElementById(targetObject.name.replace('[]', '') + objCnt).innerHTML = targetObject.value;
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

function csvOutputOnly(){
    document.forms[0].csv[0].disabled = true;
    document.forms[0].csv[1].checked = true;
    document.forms[0].userfile.disabled = true;
    document.forms[0].btn_refer.disabled = true;
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj, cnt) {
//    //移動可能なオブジェクト
//    var textFieldArray = setTextField.split(",");
//    //行数
//    var lineCnt = document.forms[0].COUNTER.value;
//    //1行目の生徒
//    var isFirstStudent = cnt == 0;
//    //最終行の生徒
//    var isLastStudent = cnt == lineCnt - 1;
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }
    //var moveEnt = document.forms[0].MOVE_ENTER[0].checked ? 40 : 39;
    //var moveEnt = 40;
    var targetObject = document.getElementById("scoreText" + (cnt + 1));
    if (targetObject) {
        targetObject.focus();
    }
}
