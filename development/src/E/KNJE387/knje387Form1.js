function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function sortConfirm(sorttype) {
    if (!confirm('{rval MSG108}')) {
        return false;
    }

    document.forms[0].cmd.value = sorttype;
    document.forms[0].submit();

}

function getByteLength(str){
  str = (str==null)?"":str;
  return encodeURI(str).replace(/%../g, "*").length;
}

function chkNumberStr(val) {
  var regexp = new RegExp(/^[-]?([1-9]\d*|0)$/);
  return regexp.test(val);
}

//加減点計チェック
function calcTotal(obj, no, sfFlg) {
    if (0 < getByteLength(obj.value) && getByteLength(obj.value) <= 4) {
        if (chkNumberStr(obj.value)) {
            //元データSCORE_SUBTOTAL 、合計値出力先SCORE_TOTAL
            var set_Obj = document.getElementById("SCORE_TOTAL-"+no);
            var read_Obj = document.forms[0]["SCORE_SUBTOTAL-" + no];
            var clc = parseInt(read_Obj.value, 10) + parseInt(obj.value, 10);
            set_Obj.innerHTML = parseInt(read_Obj.value, 10) + parseInt(obj.value, 10);
        } else {
            alert('{rval MSG907}');
            if (sfFlg == true) obj.focus();
            return false;
        }
    } else if (getByteLength(obj.value) > 0) {
        alert('{rval MSG913}');
        if (sfFlg == true) {
            obj.focus();
        }
        return false;
    }
    return true;
}
var clipValLength;
//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("INPUT_ADJUST");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });

    //以下、insertTsvで取得するclipValLengthが前提なので注意。
    var targetName   = obj.name.split("-")[0];
    var targetNumber = parseInt(obj.name.split("-")[1]);
    var setNumber = 0;
    for (var objCnt = 0; objCnt < clipValLength; objCnt++) {
        setNumber = targetNumber + objCnt;
        //貼り付けた値を元に、チェック処理を実施
        if (!calcTotal(document.forms[0]["INPUT_ADJUST-" + setNumber], setNumber, false)) {
            break;
        }
    }

    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
    return true;
}

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var i;
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;
    
    clipValLength = clipTextArray.length;
    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                i++;
            }
        }
    }
    return true;
}
// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab2(obj, setTextField, cnt) {
    //移動可能なオブジェクト
    var textFieldArray = setTextField.split(",");
    //行数
    var lineCnt = document.forms[0].COUNT.value;
    //1行目の生徒
    var isFirstStudent = cnt == 0 ? true : false;
    //最終行の生徒
    var isLastStudent = cnt == lineCnt - 1 ? true : false;
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }
    var moveEnt = 40;
    for (var i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] + cnt == obj.name) {
            var isFirstItem = i == 0 ? true : false;
            var isLastItem = i == textFieldArray.length - 1 ? true : false;
            if (moveEnt == 37) {
                if (isFirstItem && isFirstStudent) {
                    obj.focus();
                    return;
                }
                if (isFirstItem) {
                    targetname = textFieldArray[(textFieldArray.length - 1)] + (cnt - 1);
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[(i - 1)] + cnt;
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 38) {
                if (isFirstStudent) {
                    obj.focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt - 1);
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 39 || moveEnt == 13) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastItem) {
                    targetname = textFieldArray[0] + (cnt + 1);
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[(i + 1)] + cnt;
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 40) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastStudent) {
                    targetname = textFieldArray[(i + 1)] + 0;
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt + 1);
                document.forms[0].elements[targetname].focus();
                return;
            }
        }
    }
}
