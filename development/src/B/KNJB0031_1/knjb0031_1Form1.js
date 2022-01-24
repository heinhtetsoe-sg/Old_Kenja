function btn_submit(cmd) {
    //データ指定チェック
    if (cmd == 'update') {
        var dataCnt = document.forms[0].DATA_CNT.value;
        if (dataCnt == 0) {
            alert('{rval MSG304}');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("TRGTCLASS1",
                              "HR_NAMEABBV1",
                              "TRGTCLASS2",
                              "HR_NAMEABBV2",
                              "TRGTCLASS3",
                              "HR_NAMEABBV3",
                              "TRGTCLASS4",
                              "HR_NAMEABBV4",
                              "TRGTCLASS5",
                              "HR_NAMEABBV5",
                              "TRGTCLASS6",
                              "HR_NAMEABBV6",
                              "TRGTCLASS7",
                              "HR_NAMEABBV7",
                              "TRGTCLASS8",
                              "HR_NAMEABBV8",
                              "TRGTCLASS9",
                              "HR_NAMEABBV9",
                              "TRGTCLASS10",
                              "HR_NAMEABBV10",
                              "TRGTCLASS11",
                              "HR_NAMEABBV11",
                              "TRGTCLASS12",
                              "HR_NAMEABBV12",
                              "TRGTCLASS13",
                              "HR_NAMEABBV13",
                              "TRGTCLASS14",
                              "HR_NAMEABBV14",
                              "TRGTCLASS15",
                              "HR_NAMEABBV15",
                              "TRGTCLASS16",
                              "HR_NAMEABBV16",
                              "TRGTCLASS17",
                              "HR_NAMEABBV17",
                              "TRGTCLASS18",
                              "HR_NAMEABBV18",
                              "TRGTCLASS19",
                              "HR_NAMEABBV19",
                              "TRGTCLASS20",
                              "HR_NAMEABBV20");

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

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    if (objectNameArray[k].match(/HR_NAMEABBV/)) {
                        i++;
                        continue;
                    }

                    //スペース削除
                    var str_num = new String(clipTextArray[j][i]);
                    clipTextArray[j][i] = str_num.replace(/ |　/g,"");

                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])){
                        alert('{rval MSG907}');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}
//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}
