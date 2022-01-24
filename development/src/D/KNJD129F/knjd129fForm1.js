window.onload = init;
function init() {       //ウィンドウを開いたら呼ばれる関数
    switchDisabled();   //ラジオボタンを表示したり隠したり
}

function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            //更新中の画面ロック
            updateFrameLock()
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("VALUE");

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
    var setNumber = parseInt(targetNumber);
    var objectNameArray = harituke_jouhou.objectNameArray;

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            var getCheck = "";
            var setName = targetName + '-' + String(setNumber);
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                getCheck = valueCheck(clipTextArray[j][i], setName);
                if (getCheck == 'value_null') {
                    clipTextArray[j][i] = "";
                }
                setNumber++;
                i++;
            }
        }
    }
    return true;
}

//評価チェック
function valueCheck(getvalue, data) {
    var setNameArray = data.split("-");
    var setName = setNameArray[0];//VALUE
    var counter = setNameArray[1];
    var d060_count = document.forms[0].D060_COUNTER.value;
    getvalue = parseInt(getvalue);
    //評価テキストの横に表示するためのID
    var checkValue = document.getElementById(setName + "_CHECK_ID_" + counter);
    //値チェック
    var checkFlg = false;
    for (i = 0; i < parseInt(d060_count); i++) {
        //名称マスタD061の値
        var d060_namcd2 = document.forms[0]["NAMECD2-" + i].value;
        d060_namcd2 = parseInt(d060_namcd2);
        //値をチェック
        if (getvalue === d060_namcd2) {
            checkValue.innerHTML = '<font color="red">' + String(document.forms[0]["NAME1-" + i].value) + '</font>';
            return true;
        }
    }
    if (checkFlg == false) {
        alert('名称マスタ「D060」に登録されている数値のみが有効です。');
        document.forms[0][setName + "-" + counter].value = "";
        checkValue.innerHTML = '';
        return 'value_null';
    }
    return;
}
//左クリック
function kirikae(obj, showName) {
    setValue(obj, showName, document.forms[0].NYURYOKU[1].checked);
}
//右クリック
function kirikae2(obj, showName) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    event.cancelBubble = true
    event.returnValue = false;
    clickList(obj, showName);
}
//値をセット
function setValue(obj, showName, clearCheck) {
    var setNameArray = showName.split("-");
    var setName = setNameArray[0];//VALUE
    var counter = setNameArray[1];
    defObj = document.forms[0][setName + "_FORM_ID" + "-" + counter];
    if (clearCheck) {
        obj.value = "";
        defObj.value = "";
    } else {
        innerName = showName;
        typeValArray = document.forms[0].SETVAL.value.split(",");
        typeShowArray = document.forms[0].SETSHOW.value.split(",");

        for (var i = 0; i < document.forms[0].TYPE_DIV.length; i++) {
            typeDiv = document.forms[0].TYPE_DIV[i];
            if (typeDiv.checked) {
                obj.value = typeShowArray[typeDiv.value - 1];
                defObj.value = typeValArray[typeDiv.value - 1];
            }
        }
    }
}

function clickList(obj, showName) {
    innerName = showName;

    setObj = obj;
    myObj = document.getElementById("myID_Menu").style;
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top  = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function setClickValue(val) {
    if (val != '999') {
        typeShowArray = document.forms[0].SETSHOW.value.split(",");
        setObj.value = typeShowArray[val - 1];

        var setNameArray = setObj.name.split("-");
        var setName = setNameArray[0];//VALUE
        var counter = setNameArray[1];
        defObj = document.forms[0][setName + "_FORM_ID" + "-" + counter];
        typeValArray = document.forms[0].SETVAL.value.split(",");
        defObj.value = typeValArray[val - 1];
    }
    myHidden();
    setObj.focus();
}

function myHidden() {
    document.getElementById("myID_Menu").style.visibility = "hidden";
    switchDisabled();
}
//disabled（入力方法の値）
function switchDisabled() {
    obj = document.getElementById("NYURYOKU1");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/TYPE_DIV/)) {
            document.forms[0].elements[i].disabled = !obj.checked;
        }
    }
}
