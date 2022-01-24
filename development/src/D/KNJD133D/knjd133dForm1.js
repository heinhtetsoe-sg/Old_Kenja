
var currentFocus = null;

function btn_submit(cmd) {
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

//入力コードのチェックおよび定型文の取込
function chkTorikomi(schregno, grade) {
    var gradeTeikeiList = document.forms[0]["TEIKEI_GRADE_" + grade].value.split(",");
    var inputCd = document.forms[0]["PATTERN_CD_" + schregno].value;

    var flg = false; //入力コードに対応する定型文があるかのフラグ
    var errorMsg = "";
    var sep = "";
    var patternCd = "";
    var remark = "";


    //空値の場合
    if (inputCd === "") {
        var inputRemark = document.getElementById("REMARK_" + schregno);
        var hidRemark = document.forms[0]["REMARK_" + schregno];
        inputRemark.innerHTML = "";
        hidRemark.value = "";
        return true;
    }

    //値を入力した場合
    for (var i = 0; i < gradeTeikeiList.length; i++) {
        if (gradeTeikeiList[i] == "") break;
        var tmp = gradeTeikeiList[i].split("-");
        if (tmp.length != 2) break;
        patternCd = tmp[0];
        remark = tmp[1];
        errorMsg += sep + patternCd; 
        sep = ",";
        if (patternCd === inputCd) {
            flg = true;
            break;
        }
    }

    if (flg) {
        //入力コードに対応する定型文を取り込む
        var inputRemark = document.getElementById("REMARK_" + schregno);
        var hidRemark = document.forms[0]["REMARK_" + schregno];
        inputRemark.innerHTML = remark;
        hidRemark.value = remark;
        return true;
    } else {
        //入力したコードは誤りなのでエラーメッセージ
        var inputRemark = document.getElementById("REMARK_" + schregno);
        var hidRemark = document.forms[0]["REMARK_" + schregno];
        inputRemark.innerHTML = "";
        hidRemark.value = "";

        alert('{rval MSG901}\n入力できる値は「' + errorMsg + '」のいずれかです。');
        document.forms[0]["PATTERN_CD_" + schregno].value = "";
        document.forms[0]["PATTERN_CD_" + schregno].focus();
        return false;
    }
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function moveEnter(obj, cnt) {
    //行数
    var totalCnt = document.forms[0].COUNTER_TOTAL.value;
    var e = window.event;
    //方向キー
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }

    //移動先オブジェクト名取得
    var nextCnt = (Number(cnt) + 1) % totalCnt;
    var targetname = document.forms[0]["COUNTER_" + nextCnt].value;
    var targetObject = document.forms[0][targetname];

    targetObject.focus();
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
