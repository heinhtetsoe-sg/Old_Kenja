/* Add by PP for CurrentCursor 2020-01-20 start */
window.onload = function () {
    if (sessionStorage.getItem("KNJA143aForm1_CurrentCursor915") != null) {
        document.getElementsByName(sessionStorage.getItem("KNJA143aForm1_CurrentCursor915"))[0].focus();
        var a = document.getElementsByName(sessionStorage.getItem("KNJA143aForm1_CurrentCursor915"))[0].value;
        document.getElementsByName(sessionStorage.getItem("KNJA143aForm1_CurrentCursor915"))[0].value = "";
        document.getElementsByName(sessionStorage.getItem("KNJA143aForm1_CurrentCursor915"))[0].value = a;
        sessionStorage.removeItem("KNJA143aForm1_CurrentCursor915");
    } else {
        sessionStorage.removeItem("KNJA143aForm1_CurrentCursor915");
        if (sessionStorage.getItem("KNJD143aForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJD143aForm1_CurrentCursor")).focus();
        }
    }
}

function current_cursor(para) {
    sessionStorage.setItem("KNJD143aForm1_CurrentCursor", para);
}
/* Add by PP for CurrentCursor 2020-01-31 end */
function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2020-01-10 start */
    if (sessionStorage.getItem("KNJD143aForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJD143aForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-01-17 end */
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            //更新中の画面ロック
            updateFrameLock()
        }
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//ALLチェック(単位自動計算)
function check_all(obj) {
    var i;
    for (i = 0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        if (e.type == 'checkbox' && e.name.match(/CHK_CALC_CREDIT./) && !e.disabled) {
            e.checked = obj.checked;
        }
    }
}

//定型文セット
function tmpSet(obj, chkFlg, id) {
    //数値チェック
    var value = obj.value;
    obj.value = toInteger(obj.value);
    if (obj.value != value) {
        document.getElementById(id).focus();
    }

    if(chkFlg == 'true'){
        if (obj.value != "") {
            if (obj.value > 3) {
                alert("1～3までの値を入力してください。\n入力された文字列は削除されます。");
                obj.value = "";
            }
        }
    }
    return;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = ["CONDUCT_EVAL", "GRAD_VALUE"];

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
    var i, j, k;
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;
    var objName;
    var conductEvalMoji = parseInt(document.forms[0].conduct_eval_moji);
    var conductEvalGyo = parseInt(document.forms[0].conduct_eval_gyo);

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            objName = objectNameArray[k];
            if (objName == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (objName == 'CONDUCT_EVAL') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), (conductEvalMoji * conductEvalGyo)) > conductEvalGyo) {
                            alert(conductEvalGyo + '行までです');
                            return false;
                        }
                    }
                    if (objName == 'GRAD_VALUE') {
                        if (isNaN(clipTextArray[j][i])){
                            alert('{rval MSG907}');
                            return false;
                        }
                    }
                }
                i++;
            }
        }
    }
    return true;
}

