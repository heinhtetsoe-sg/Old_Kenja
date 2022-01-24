function btn_submit(cmd, positionNum) {

    //肩書変更
    if (cmd == 'change') {
        document.forms[0].POSITIONCD_NUM.value = positionNum;
        if (document.forms[0].POSITIONCD1_MANAGER !== undefined) {
            document.forms[0].POSITION1_EXT.value = getPostPosition(document.forms[0].POSITIONCD1_MANAGER);
        }
        if (document.forms[0].POSITIONCD2_MANAGER !== undefined) {
            document.forms[0].POSITION2_EXT.value = getPostPosition(document.forms[0].POSITIONCD2_MANAGER);
        }
        if (document.forms[0].POSITIONCD3_MANAGER !== undefined) {
            document.forms[0].POSITION3_EXT.value = getPostPosition(document.forms[0].POSITIONCD3_MANAGER);
        }
    }

    //新規
    if (cmd == 'new') {
        if (!confirm('{rval MSG108}')) {
            return;
        }
    }

    //検索（教育委員会）
    if (cmd == 'search') {
        if (document.forms[0].STAFFCD.value == "") {
            alert('{rval MSG304}');
            return true;
        }
    }

    //県職員情報取込（教育委員会）
    if (cmd == 'search_ken') {
        if (document.forms[0].EDBOARD_STAFFCD.value == "") {
            alert('{rval MSG304}');
            return true;
        }
        if (!confirm('上書き表示します。よろしいですか？')) {
            return;
        }
    }

    //更新
    if (cmd == 'add' || cmd == 'update') {
        if (document.forms[0].FROM_DATE.value != "" && document.forms[0].FROM_DIV.value == ""
            || document.forms[0].FROM_DATE.value == "" && document.forms[0].FROM_DIV.value != ""
        ) {
            alert('赴任(入)日付/赴任区分を入力して下さい。');
            return true;
        }
        if (!checkOverlap()) {
            alert('教科が重複しています。');
            return true;
        }
        document.forms[0].POSITIONCD_NUM.value = positionNum;
        if (document.forms[0].POSITIONCD1_MANAGER !== undefined) {
            document.forms[0].POSITION1_EXT.value = getPostPosition(document.forms[0].POSITIONCD1_MANAGER);
        }
        if (document.forms[0].POSITIONCD2_MANAGER !== undefined) {
            document.forms[0].POSITION2_EXT.value = getPostPosition(document.forms[0].POSITIONCD2_MANAGER);
        }
        if (document.forms[0].POSITIONCD3_MANAGER !== undefined) {
            document.forms[0].POSITION3_EXT.value = getPostPosition(document.forms[0].POSITIONCD3_MANAGER);
        }
    }

    //削除
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')){
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chkSelSameValue() {
    var obj1 = document.forms[0].DUTYSHARECD;
    var obj2 = document.forms[0].DUTYSHARECD2;
    var obj3 = document.forms[0].HID_DUTYSHARECD;
    var obj4 = document.forms[0].HID_DUTYSHARECD_2;
    if (obj1.value == obj2.value && obj1.value != "") {
        alert('{rval MSG302}');
        obj1.value = obj3.value;
        obj2.value = obj4.value;
        return true;
    }
    obj3.value = obj1.value;
    obj4.value = obj2.value;
    return true;
}

function getPostPosition(positionObj) {
    var positionVal = "";
    sep = "";
    for (var i = 0; i < positionObj.length; i++) {
        if (positionObj.options[i].selected) {
            positionVal = positionVal + sep + positionObj.options[i].value;
            sep = ",";
        }
    }
    return positionVal;
}

function changeDisplay(positionNum) {
    var targetBtn = document.getElementById("BTN_POSITION" + positionNum);
    var targetBox = document.getElementById("POSITIONCD" +  + positionNum + "_MANAGER");
    targetBox.style.display = targetBtn.value == "教科登録" ? "inline-block" : "none";
    targetBox.style.width = "300px";
    targetBtn.value = targetBtn.value == "教科登録" ? "閉じる" : "教科登録";
}

function checkOverlap() {
    var selectData = new Array();
    isOverlap = getCheckOverlap(1, selectData);
    isOverlap = getCheckOverlap(2, selectData);
    isOverlap = getCheckOverlap(3, selectData);
    return isOverlap;
}

function getCheckOverlap(positionNum, selectData) {
    positionObj = document.getElementById("POSITIONCD" +  + positionNum + "_MANAGER");
    if (null == positionObj) {
        return true;
    }
    for (var i = 0; i < positionObj.length; i++) {
        if (positionObj.options[i].selected) {
            setVal = positionObj.options[i].value;
            if (selectData[setVal] == "1") {
                return false;
            }
            selectData[setVal] = "1";
        }
    }
    return true;
}
