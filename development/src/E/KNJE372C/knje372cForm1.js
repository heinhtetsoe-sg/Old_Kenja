function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//入力用
function checkVal(obj) {

    //ブランクはチェックしない
    if (obj.value == "") return true;

    //推薦枠マスタに登録されてい学科番号をチェック
    var validateValues = document.forms[0].validateValues.value.split(",");
    if (validateValues.indexOf(obj.value) === -1) {
        alert('{rval MSG901}'+'推薦枠の学科番号を入力して下さい。\n入力された文字列は削除されます。');
        obj.value = "";
        return false;
    }
}

//貼り付け値用
function checkVal2(value) {
    //ブランクはチェックしない
    if (value == "") return true;

    //推薦枠マスタに登録されてい学科番号をチェック
    var validateValues = document.forms[0].validateValues.value.split(",");
    if (validateValues.indexOf(value) === -1) {
        alert('{rval MSG901}'+'推薦枠の学科番号を入力して下さい。');
        return false;
    }

    return true;
}

function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
/*************************************************貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('内容を貼り付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("HOPEORDER1",
                              "HOPEORDER2",
                              "HOPEORDER3",
                              "HOPEORDER4",
                              "HOPEORDER5",
                              "HOPEORDER6",
                              "HOPEORDER7",
                              "HOPEORDER8",
                              "HOPEORDER9",
                              "HOPEORDER10",
                              "HOPEORDER11",
                              "HOPEORDER12",
                              "HOPEORDER13",
                              "HOPEORDER14",
                              "HOPEORDER15",
                              "HOPEORDER16",
                              "HOPEORDER17",
                              "HOPEORDER18",
                              "HOPEORDER19",
                              "HOPEORDER20",
                              "HOPEORDER21",
                              "HOPEORDER22",
                              "HOPEORDER23",
                              "HOPEORDER24",
                              "HOPEORDER25",
                              "HOPEORDER26",
                              "HOPEORDER27",
                              "HOPEORDER28",
                              "HOPEORDER29",
                              "HOPEORDER30",
                              "HOPEORDER31",
                              "HOPEORDER32",
                              "HOPEORDER33",
                              "HOPEORDER34",
                              "HOPEORDER35",
                              "HOPEORDER36",
                              "HOPEORDER37",
                              "HOPEORDER38",
                              "HOPEORDER39",
                              "HOPEORDER40",
                              "HOPEORDER41",
                              "HOPEORDER42");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });

    //これを実行しないと貼付けそのものが実行されてしまう
    if (event.preventDefault) {
        event.preventDefault();
    }
    return false;
}


function insertTsv(harituke_jouhou) {
    if (typeof checkClip != "function") {
        alert("関数checkClipが定義されていません。");
        return false;
    }
    if (typeof execCopy != "function") {
        alert("関数execCopyが定義されていません。");
        return false;
    }
    if (window.clipboardData && window.clipboardData.getData) { // IE
        var clipText = window.clipboardData.getData('Text');
    } else if (window.event.clipboardData && window.event.clipboardData.getData) {   //non-IE
        var clipText = window.event.clipboardData.getData('text/plain');
    }

    var clipTextArray = tsv_to_array(clipText);

    // まずはクリップボードの中身のチェック
    // falseが帰ってきたら正しくないデータが混ざっている
    if (!checkClip(clipTextArray, harituke_jouhou)) {
        return false;
    }

    if (harituke_jouhou.harituke_type == "hairetu") {
        paste_hairetu(clipTextArray, harituke_jouhou);
    } else if(harituke_jouhou.harituke_type == "renban_hairetu") {
        paste_renban_hairetu(clipTextArray, harituke_jouhou);
    } else {
        paste_other(clipTextArray, harituke_jouhou);
    }
    if (typeof afterPasteClip == 'function') {
        afterPasteClip(harituke_jouhou);
    }
}

function paste_other(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var targetName;
    var targetNumber;
    if (harituke_jouhou.harituke_type == "renban") {
        targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
        targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    } else if (harituke_jouhou.harituke_type == "kotei") {
        targetName   = harituke_jouhou.clickedObj.name;
    }

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    if (harituke_jouhou.harituke_type == "renban") {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "-"  + targetNumber + "\"]");
                    } else if (harituke_jouhou.harituke_type == "kotei") {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"]");
                    }
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        execCopy(targetObject, clipTextArray[gyouCnt][retuCnt], targetNumber);
                    }
                }
                retuCnt++;
            }
        }
        if (harituke_jouhou.harituke_type == "kotei") {
            break;
        }
        targetNumber++;
    }
}


/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    if (targetObject.value != val) {
        targetObject.style.background = '#ccffcc';
    }
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
    var validateValues = document.forms[0].validateValues.value.split(",");

    for (j = 0; j < clipTextArray.length; j++) {
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) {
            if (objectNameArray[k] == targetName) {
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined && clipTextArray[j][i] != '') {
                    var inputValue = String(clipTextArray[j][i][0]);
                    if (!checkVal2(inputValue)) return false;
                }
                i++;
            }
        }
    }
    return true;
}
