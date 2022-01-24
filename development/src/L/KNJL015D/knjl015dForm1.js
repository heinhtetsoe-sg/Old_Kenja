function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL, printtype) {
    //必須チェック
    if (printtype == 'print_one' && document.forms[0].FINSCHOOLCD.value == '') {
        alert('{rval MSG310}\n( 中学校 )');
        return;
    }

    if (printtype == "print_one" && document.forms[0].FINSCHOOLCD.value == "ZZZZZZZ") {
        document.forms[0].PRINTTYPE.value = "print_all";
    } else {
        document.forms[0].PRINTTYPE.value = printtype;
    }

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

//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SELECTION_REMARK",
                              "SELECTION_REMARK2");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });
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
