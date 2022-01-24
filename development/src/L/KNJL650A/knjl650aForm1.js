function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled) || (document.forms[0].TESTSUBCLASSCD.disabled) || (document.forms[0].HALLCD.disabled) ) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'read') {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試制度）');
            return true;
        }
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試区分）');
            return true;
        }
        if (document.forms[0].TESTSUBCLASSCD.value == ""){
            alert('{rval MSG304}'+ '\n（試験科目）');
            return true;
        }
        if (document.forms[0].HALLCD.value == ""){
            alert('{rval MSG304}'+ '\n（会場）');
            return true;
        }
    }
    
    if (cmd == 'next') {
       document.forms[0].STARTNO.value = parseInt(document.forms[0].STARTNO.value) + 50; 
    }
    if (cmd == 'back') {
       document.forms[0].STARTNO.value = parseInt(document.forms[0].STARTNO.value) - 50; 
    }

    //CSV出力
    if (cmd == "csvOutput") {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試制度）');
            return true;
        }
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試区分）');
            return true;
        }
    }

    //CSV取込
    if (cmd == "csvInput") {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試制度）');
            return true;
        }
        if (document.forms[0].TESTDIV.value == ""){
            alert('{rval MSG304}'+ '\n（入試区分）');
            return true;
        }

        document.forms[0].btn_input.disabled  = true;
        document.forms[0].btn_output.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled  = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

//得点チェック
function CheckScore(obj,perfect) {
    if (obj.value != "*") {
        if(to_Integer(obj)){
            obj.value = toInteger(obj.value);
            if (obj.value > eval(perfect)) {
                alert('{rval MSG901}' + '\n満点：'+perfect+'以下で入力してください。');
                obj.focus();
                return;
            }
        }
    }
}
//数値チェック
function to_Integer(obj) {
    var checkString = obj.value;
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= "0" && ch <= "9") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数値を入力してください。\n入力された文字列は削除されます。");
        obj.value="";
        return false;
    }
    return true;
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_TESTSUBCLASSCD.value  = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].HID_HALLCD.value          = document.forms[0].HALLCD.value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].TESTSUBCLASSCD.disabled   = true;
    document.forms[0].HALLCD.disabled           = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}

//受験番号変更したら更新ボタンをグレーにする
function btn_disabled() {
    document.forms[0].btn_update.disabled = true;
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECEPTNO.value.split(',');
        var index = setArr.indexOf(obj.id);
        var targetId = "";
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
            targetId = setArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (setArr.length - 1); i++) {
                    targetId = setArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        if (targetId != "") {
            document.getElementById(targetId).focus();
            document.getElementById(targetId).select();
        }
        return false;
    }
}

//貼り付け機能
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SCORE");

    var renArray = new Array();
    var receptNoArray = document.forms[0].HID_RECEPTNO.value.split(",");
    for (var i = 0; i < receptNoArray.length; i++) {
        renArray[i] = receptNoArray[i];
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt"      :cnt,
               "renbanArray"     :renArray
               });

    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, objCnt) {
    if (targetObject.value != val) {
        document.getElementById('ROWID' + targetObject.id).style.background = 'yellow';
        targetObject.style.background = 'yellow';
    }
    targetObject.value = val;
}

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var objCnt = harituke_jouhou.hairetuCnt;
    var renbanArray = harituke_jouhou.renbanArray;

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == harituke_jouhou.clickedObj.name.split("-")[0]) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "-" + renbanArray[objCnt] + "\"]");
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        if (clipTextArray[gyouCnt][retuCnt] == "*") {
                        } else if (isNaN(clipTextArray[gyouCnt][retuCnt])) {
                            alert('{rval MSG907}');
                            return false;
                        } else if (clipTextArray[gyouCnt][retuCnt] > eval(aPerfect[targetObject.id])) {
                            var perfect = aPerfect[targetObject.id];
                            alert('{rval MSG901}' + '\n満点：'+perfect+'以下で入力してください。');
                            return false;
                        }
                    }
                }
                retuCnt++;
            }
        }
        objCnt++;
    }
    return true;
}
