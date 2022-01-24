
var currentFocus = null;

function btn_submit(cmd, columnname, patternDataDiv, itemname) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            //更新中の画面ロック
            updateFrameLock()
        }
    } else if (cmd == 'teikei') {
        if (null == currentFocus || currentFocus.name.indexOf(columnname) == -1) {
            alert(itemname + "の入力欄を選択してください");
            return;
        }
        chr = document.forms[0].CHAIRCD.value;
        if (document.forms[0].KNJD133C_semesCombo) {
            sendSemester = document.forms[0].SEMESTER.value;
        } else {
            sendSemester = "";
        }

        link = 'knjd133cindex.php?cmd='+cmd+'&CHR='+chr+'&SEMESTER='+sendSemester+'&COLUMNNAME='+columnname+"&PATTERN_DATA_DIV="+patternDataDiv+"&ITEMNAME="+itemname+"&TARGETID="+currentFocus.id;
        loadwindow(link, event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 450);
        return true;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//画面切り替え
function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

if (window.addEventListener) {
    window.addEventListener('load', function (e) {
        var mainHeight = document.getElementById("REC_MAIN").clientHeight;
        var recHeight = document.getElementById("REC").clientHeight;
        var prop;
        if (0 < recHeight && mainHeight < recHeight) {
            prop = "scroll";
        } else {
            prop = "auto";
        }
        document.getElementById("item_header").style.overflowY = prop;

    }, false);
}

function focused(obj) {
    currentFocus = obj;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = [];
    for (key in itemMstJson()) {
        nameArray.push(key);
    }

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

function itemMstJson() {
    return JSON.parse(document.getElementsByName("itemMstJson")[0].value);
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
    var parsed = itemMstJson();

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    var moji = parsed[objectNameArray[k]].moji;
                    var gyou = parsed[objectNameArray[k]].gyou;
                    var label = parsed[objectNameArray[k]].ITEMNAME;

                    if (validate_row_cnt(String(clipTextArray[j][i]), (moji * 2)) > gyou) {
                        alert(label+'は'+gyou+'行までです');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}
