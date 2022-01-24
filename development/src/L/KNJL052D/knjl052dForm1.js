function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    //次年度コピー
    if (cmd == 'copy') {
        var value = parseInt(document.forms[0].YEAR.value) + 1;
        var message = document.forms[0].YEAR.value + '年度のデータから、' + value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//全チェック選択（チェックボックスon/off）
function chkDataALL(obj) {
    for (var i=1; i <= 100; i++) {
        document.forms[0]["QUEST_FLAG-" + i].checked = obj.checked;
        if (obj.checked == true) {
            document.forms[0]["PATTERN_CD-" + i].disabled = false;
            document.forms[0]["ANSWER1-" + i].disabled    = false;
            document.forms[0]["POINT1-" + i].disabled     = false;
            document.forms[0]["ANSWER2-" + i].disabled    = false;
            document.forms[0]["POINT2-" + i].disabled     = false;
            document.forms[0]["ANSWER3-" + i].disabled    = false;
            document.forms[0]["POINT3-" + i].disabled     = false;
        } else {
            document.forms[0]["PATTERN_CD-" + i].disabled = true;
            document.forms[0]["ANSWER1-" + i].disabled    = true;
            document.forms[0]["POINT1-" + i].disabled     = true;
            document.forms[0]["ANSWER2-" + i].disabled    = true;
            document.forms[0]["POINT2-" + i].disabled     = true;
            document.forms[0]["ANSWER3-" + i].disabled    = true;
            document.forms[0]["POINT3-" + i].disabled     = true;
        }
    }
}
//チェックon時にテキストボックス使用可
function chkUnDisabled(obj) {
    var i = obj.name.split("-")[1];
    if (obj.checked == true) {
        document.forms[0]["PATTERN_CD-" + i].disabled = false;
        document.forms[0]["ANSWER1-" + i].disabled    = false;
        document.forms[0]["POINT1-" + i].disabled     = false;
        document.forms[0]["ANSWER2-" + i].disabled    = false;
        document.forms[0]["POINT2-" + i].disabled     = false;
        document.forms[0]["ANSWER3-" + i].disabled    = false;
        document.forms[0]["POINT3-" + i].disabled     = false;
    } else {
        document.forms[0]["PATTERN_CD-" + i].disabled = true;
        document.forms[0]["ANSWER1-" + i].disabled    = true;
        document.forms[0]["POINT1-" + i].disabled     = true;
        document.forms[0]["ANSWER2-" + i].disabled    = true;
        document.forms[0]["POINT2-" + i].disabled     = true;
        document.forms[0]["ANSWER3-" + i].disabled    = true;
        document.forms[0]["POINT3-" + i].disabled     = true;
    }
}
//入力チェック
function checkVal(obj, div){
    var str = obj.value;
    var nam = obj.name;
    var rowNo = nam.split("-")[1];

    //空欄
    if (str == '') { 
        return;
    }

    //指定数字のみ
    //パターンコード
    if (div == '1') {
        if (!str.match(/1|2|3/)) {
            alert('{rval MSG901}'+'「1～3」を入力して下さい。');
            obj.value = "";
            obj.focus();
            return;
        }

    //正解-番号
    } else if (div == '2') {
        if (str.length == '2') {
            if (!str.match(/[1][0]/)) { 
                alert('{rval MSG901}'+'「1～10」を入力して下さい。');
                obj.value = "";
                obj.focus();
                return;
            }
        }
        if (!str.match(/[1-9]/)) { 
            alert('{rval MSG901}'+'「1～10」を入力して下さい。');
            obj.value = "";
            obj.focus();
            return;
        }

    //配点
    } else {
        if (!str.match(/\d/)) {
            alert('{rval MSG901}'+'「数値」を入力して下さい。');
            obj.value = "";
            obj.focus();
            return;
        }
    }

    //パターンコードの値"1"の時のエラーチェック
    var pat = document.forms[0]["PATTERN_CD-" + rowNo];
    var ans = document.forms[0]["ANSWER2-" + rowNo];
    var poi = document.forms[0]["POINT2-" + rowNo];
    if (pat.value == "1" && pat.value != "") {
        if (ans.value || poi.value) {
            alert('パターンコードに「1」が入力されています。\n正解２は入力出来ません');
            ans.value = "";
            poi.value = "";
            return;
        }

    //パターンコードの値"2"の時のエラーチェック
    } else if (pat.value == "2" && pat.value != "") {
        if (poi.value != "0" && poi.value != "") {
            alert('パターンコードに「2」が入力されています。\n正解２（配点）に「0」をセットします');
            poi.value = "0";
            return;
        }
    }

    return;
}
// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj) {
    // Ent13
    var nam = obj.name;
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    } else {
        if (nam.split("-")[0] == "POINT2") {
            var nextNo = parseInt(nam.split("-")[1]) + 1;
            var setFormName;
            var targetObject;
            for (var i=nextNo; i <= 100; i++) {
                setFormName  = "PATTERN_CD-" + nextNo;
                targetObject = document.forms[0][setFormName];
                if (targetObject.disabled == true) {
                    nextNo++;
                } else {
                    break;
                }
            }
            targetObject.focus();
            targetObject.select();
            return;
        }
        e.keyCode = 9;
    }
}
