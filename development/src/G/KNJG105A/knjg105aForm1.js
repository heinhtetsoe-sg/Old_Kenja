function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    if (cmd == 'update') {
        var checkFlg = false;
        var checkSch = "";
        var sep = "";

        for (var i=0; i < document.forms[0].elements.length; i++) {
            re = new RegExp("CHECKED");
            if (document.forms[0].elements[i].name.match(re)) {
                if (document.forms[0].elements[i].checked == true) {
                    checkSch += sep + document.forms[0].elements[i].value;
                    sep = ",";
                    checkFlg = true;
                }
            }
        }
        document.forms[0].checkSch.value = checkSch;

        if (checkFlg == false) {
            alert("チェックボックスを選択してください");
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function toInteger1(obj) {
    obj.value = toInteger(obj.value);

    //サブミット
    document.forms[0].cmd.value = 'main';
    document.forms[0].submit();
    return false;
}
function goEnter(obj){
    if (window.event.keyCode==13) {
        obj.blur();
        return false;
    }
}
//全チェック操作
function check_all() {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        re = new RegExp("CHECKED");
        if (document.forms[0].elements[i].name.match(re)) {
            document.forms[0].elements[i].checked = document.forms[0]['CHECKALL'].checked;
            if (document.forms[0].elements[i].checked == true) {
                chgColor(document.forms[0].elements[i].value, "#ccffcc");
            }else{
                chgColor(document.forms[0].elements[i].value, "#ffffff");
            }
        }
    }
}
function checkSelf(schNo) {
    var idname = "CHECKED" + schNo;
    document.getElementById(idname).checked = true;
    chgColor(schNo, "#ccffcc");
}
function chkClick(obj) {
    if (obj.checked == true) {
        chgColor(obj.value, "#ccffcc");
    }else{
        chgColor(obj.value, "#ffffff");
    }
}
function chgColor(schNo, rgb) {
    document.getElementById(schNo).style.background = rgb;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("REMARK");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });
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
    var i;
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (String(clipTextArray[j][i]).length > 30) {
                        alert('全角30文字までです');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}

