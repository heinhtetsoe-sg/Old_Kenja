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

    document.forms[0].encoding = 'multipart/form-data';
    //新規
    if (cmd == 'new') {
        if (!confirm('{rval MSG108}')) {
            return;
        }
    }

    //検索（教育委員会）
    if (cmd == 'search') {
        if (document.forms[0].STAFFCD.value == '') {
            alert('{rval MSG304}');
            return true;
        }
    }

    //削除
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) return false;
    }

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    //CSV
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = 'downloadHead';
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = 'uploadCsv';
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = 'downloadCsv';
        } else if (document.forms[0].OUTPUT[3].checked) {
            cmd = 'downloadError';
        } else {
            alert('ラジオボタンを選択してください。');
            return false;
        }
    }

    //更新
    if (cmd == 'add' || cmd == 'update') {
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

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chkSelSameValue() {
    var obj1 = document.forms[0].DUTYSHARECD;
    var obj2 = document.forms[0].DUTYSHARECD2;
    var obj3 = document.forms[0].HID_DUTYSHARECD;
    var obj4 = document.forms[0].HID_DUTYSHARECD_2;
    if (obj1.value == obj2.value && obj1.value != '') {
        alert('{rval MSG302}');
        obj1.value = obj3.value;
        obj2.value = obj4.value;
        return true;
    }
    obj3.value = obj1.value;
    obj4.value = obj2.value;
    return true;
}

function changeRadio(obj) {
    var type_file;
    if (obj.value == '1') {
        //1は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById('type_file'); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}

function getPostPosition(positionObj) {
    var positionVal = '';
    sep = '';
    for (var i = 0; i < positionObj.length; i++) {
        if (positionObj.options[i].selected) {
            positionVal = positionVal + sep + positionObj.options[i].value;
            sep = ',';
        }
    }
    return positionVal;
}

function changeDisplay(positionNum) {
    var targetBtn = document.getElementById('BTN_POSITION' + positionNum);
    var targetBox = document.getElementById('POSITIONCD' + +positionNum + '_MANAGER');
    targetBox.style.display = targetBtn.value == '教科登録' ? 'inline-block' : 'none';
    targetBox.style.width = '300px';
    targetBtn.value = targetBtn.value == '教科登録' ? '閉じる' : '教科登録';
}

function checkOverlap() {
    var selectData = new Array();
    isOverlap = getCheckOverlap(1, selectData);
    isOverlap = getCheckOverlap(2, selectData);
    isOverlap = getCheckOverlap(3, selectData);
    return isOverlap;
}

function getCheckOverlap(positionNum, selectData) {
    positionObj = document.getElementById('POSITIONCD' + +positionNum + '_MANAGER');
    if (null == positionObj) {
        return true;
    }
    for (var i = 0; i < positionObj.length; i++) {
        if (positionObj.options[i].selected) {
            setVal = positionObj.options[i].value;
            if (selectData[setVal] == '1') {
                return false;
            }
            selectData[setVal] = '1';
        }
    }
    return true;
}

function changeDateStr(obj, target, defaultYearStr, yearsName, dateName) {
    var str = defaultYearStr + ' ヶ月';
    if (obj.value == '') {
        document.getElementById(target).innerText = str;
        return;
    }

    var ctrlDate = new Date(document.forms[0].CTRL_DATE.value);
    var thisDate = new Date(obj.value);

    /// 現在日時までのミリ秒と日数を計算
    var timeTillNow = ctrlDate.getTime() - thisDate.getTime();
    var daysTillNow = timeTillNow / (1000 * 3600 * 24);

    // 年部分・月部分をそれぞれ計算
    var DAYS_PER_MONTH = 365 / 12;
    var years = Math.floor(daysTillNow / 365);
    var months = Math.floor((daysTillNow - 365 * years) / DAYS_PER_MONTH);

    str = '';
    if (years != 0) {
        str = years + defaultYearStr + ' ';
    }
    str += months + 'ヶ月';

    if (years < 0) {
        alert(yearsName + 'がマイナスとなっているため' + dateName + 'を確認してください。');
    }

    document.getElementById(target).innerText = str;
}
