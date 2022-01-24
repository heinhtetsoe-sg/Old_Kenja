function btn_submit(cmd) {
    bodyWidth  = (window.innerWidth  || document.body.clientWidth || 0);
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.forms[0].windowWidth.value  = bodyWidth;
    document.forms[0].windowHeight.value = bodyHeight;

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//画面リサイズ
function submit_reSize() {
    bodyWidth  = (window.innerWidth  || document.body.clientWidth || 0);
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.getElementById("table1").style.width = bodyWidth  - 36;
    document.getElementById("trow").style.width   = bodyWidth  - 237;
    document.getElementById("tbody").style.width  = bodyWidth  - 220;
    document.getElementById("tbody").style.height = bodyHeight - 210;
    document.getElementById("tcol").style.height  = bodyHeight - 227;
}

//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop  = document.getElementById('tbody').scrollTop;
}
//印刷
function newwin(SERVLET_URL){
    //入力チェック
    if (document.forms[0].SEMESTER.value == "") {
        alert('{rval MSG304}' + "\n( 学期 )");
        return false;
    }
    if (document.forms[0].GRADE_HR_CLASS.value == "") {
        alert('{rval MSG304}' + "\n( 年組 )");
        return false;
    }

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

//テキスト内でEnterを押してもsubmitされないようにする
//Submitしない
function btn_keypress(){
    if (event.keyCode == 13){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
//    var nameArray = new Array("COMMUNICATION");
    var textFieldName = document.forms[0].TEXT_FIELD_NAME.value;
    var nameArray     = textFieldName.split(",");

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
                    //if (String(clipTextArray[j][i]).length > 30) {
                    //   alert('全角30文字までです');
                    //   return false;
                    //}
                }
                i++;
            }
        }
    }
    return true;
}

