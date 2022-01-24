function btn_submit(cmd) {
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    if (cmd == 'load') {
        if (document.forms[0].TOP_SCORE.value != "" && document.forms[0].LEAST_SCORE.value != "") {
            if (parseInt(document.forms[0].LEAST_SCORE.value) > parseInt(document.forms[0].TOP_SCORE.value)) {
                alert('{rval MSG916}');
                return false;
            }
        }
        if (document.forms[0].CHGFLG.value != "0") {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }
    if (cmd == 'sort') {
        if (document.forms[0].CHGFLG.value != "0") {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }
    if (cmd == 'update') {
        //どこか選択されているかをチェック
        var wkFlg = false;
        var splStr = document.forms[0].HID_RECEPTNO.value.split(",");
        for (cnt = 0; cnt < splStr.length; cnt++) {
            if (document.forms[0]["CHECKED_"+splStr[cnt]].checked) {
                wkFlg = true;
            }
        }
        if (!wkFlg) {
            alert('{rval MSG304}'+"どこにもチェックが付いていません。");
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function postSort() {
    btn_submit('sort');
}

//一括入力によるチェック箇所の合否値置き換え
function valReplace() {
    if (document.forms[0].HID_RECEPTNO.value == "") {
        return;
    }
    var setval = document.forms[0].SEL_SETRESULT.value;
    var splStr = document.forms[0].HID_RECEPTNO.value.split(",");
    for (cnt = 0; cnt < splStr.length; cnt++) {
        //チェックが付いていて、かつ、合否の入力が可能なら、設定
        if (document.forms[0]["CHECKED_"+splStr[cnt]].checked && !document.forms[0]["JUDGEDIV-"+splStr[cnt]].disabled) {
            document.forms[0]["JUDGEDIV-"+splStr[cnt]].value = setval;
            outputLAYER('JUDGEDIV_NAME' + +splStr[cnt], judgediv_name[setval]);
        }
    }
}

//全チェック
function setSelChk(obj) {
    var setval = obj.checked;
    var splStr = document.forms[0].HID_RECEPTNO.value.split(",");
    for (cnt = 0; cnt < splStr.length; cnt++) {
        if (!document.forms[0]["CHECKED_"+splStr[cnt]].disabled) {
            document.forms[0]["CHECKED_"+splStr[cnt]].checked = setval;
        }
    }
}

function chkScore(obj) {
    var chkval = obj.value;
    if (chkval == null || chkval == "") {
        return true;
    }
    if (chkval.length > 3) {
        alert('{rval MSG915}');
        obj.focus();
        return false;
    }
    var pattern = /^([1-9]\d*|0)$/;
    if (!pattern.test(chkval)) {
        alert('{rval MSG907}');
        obj.focus();
        return false;
    }

    if (document.forms[0].TOP_SCORE.value != "" && document.forms[0].LEAST_SCORE.value != "") {
        if (parseInt(document.forms[0].LEAST_SCORE.value) > parseInt(document.forms[0].TOP_SCORE.value)) {
            alert('{rval MSG916}');
            return false;
        }
    }

    return true;
}

function Setflg(obj) {
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_COURSECODE.value = document.forms[0].COURSECODE.options[document.forms[0].COURSECODE.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].COURSECODE.disabled = true;

}

function setChgFlg(obj, val) {
    if (obj.value != val) {
        document.forms[0].CHGFLG.value = true;
    }
}

function setName(obj, rowid, flg, val) {
    var idx = obj.value;
    if (obj.value != val) {
        document.forms[0].CHGFLG.value = "1";
    }
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('JUDGEDIV_NAME' + rowid, '');
        }
        return;
    }
    if (flg == '0') {
        if (typeof judgediv_name[idx] != "undefined") {
            outputLAYER('JUDGEDIV_NAME' + rowid, judgediv_name[idx]);
        } else {
            alert('{rval MSG901}');
            outputLAYER('JUDGEDIV_NAME' + rowid, '');
            obj.value = '';
        }
    }
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECEPTNO.value.split(',');
        var chkid = obj.id.split('-');
        var index = setArr.indexOf(chkid[1]);
        var targetId = "";
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            targetId = setArr[index];
            if (document.getElementById("JUDGEDIV-"+targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = setArr[i];
                    if (document.getElementById("JUDGEDIV-"+targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
            targetId = setArr[index];
            if (document.getElementById("JUDGEDIV-"+targetId).disabled == true) {
                for (var i = index; i < (setArr.length - 1); i++) {
                    targetId = setArr[i];
                    if (document.getElementById("JUDGEDIV-"+targetId).disabled == false) break;
                }
            }
        }

        if (targetId != "") {
            document.getElementById("JUDGEDIV-"+targetId).focus();
            document.getElementById("JUDGEDIV-"+targetId).select();
        }
        return false;
    }
}

//貼り付け機能
function showPaste(obj,cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    // var nameArray = new Array("JUDGEDIV");
    // 
    // insertTsv({"clickedObj"      :obj,
    //            "harituke_type"   :"renban",
    //            "objectNameArray" :nameArray
    //            });

    var nameArray     = new Array("JUDGEDIV");

    var renArray = new Array();
    var renArray = document.forms[0].HID_RECEPTNO.value.split(",");

    insertTsv({"clickedObj"      : obj,
               "harituke_type"   : "renban_hairetu",
               "objectNameArray" : nameArray,
               "hairetuCnt"      : cnt,
               "renbanArray"     : renArray
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
                if (clipTextArray[j][i] != undefined) {
                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])){
                        alert('{rval MSG907}');
                        return false;
                    }
                    //長さチェック
                    if (String(clipTextArray[j][i]).length > 1){
                        alert('{rval MSG915}');
                        return false;
                    }
                    //コードが設定されていて、対象コードなのか
                    if (String(clipTextArray[j][i]).length != 0 && typeof judgediv_name[clipTextArray[j][i]] == "undefined") {
                        alert('{rval MSG901}');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    console.log("end");
    return true;
}
//paste後処理(画面更新用)
function afterPasteClip(harituke_jouhou) {
    document.forms[0].CHGFLG.value = true;
    var splStr = document.forms[0].HID_RECEPTNO.value.split(",");
    for (cnt = 0; cnt < splStr.length; cnt++) {
        if (!document.forms[0]["JUDGEDIV-"+splStr[cnt]].disabled) {
            setName(document.forms[0]["JUDGEDIV-"+splStr[cnt]], splStr[cnt], '0', document.forms[0]["JUDGEDIV-"+splStr[cnt]].value);
        }
    }
}

