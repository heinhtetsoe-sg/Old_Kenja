function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
        //※取消後はテーブルにある件数を段階数とするので、入力欄にある段階数は消しておく
        document.forms[0].MAX_POINTLEVEL.value = "";
    }
    if (cmd=="change") {
        //※コンボ切替時は段階数を初期化
        document.forms[0].MAX_POINTLEVEL.value = "";
    }
    var maxLevel = document.forms[0].MAX_POINTLEVEL.value;
    if (cmd == "kakutei") {
        //確定したのでフラグに0を入れる
        document.forms[0].notKakuteiFlg.value = "0";
    }

    if (cmd == "update") {
        if (maxLevel == "") {
            alert("{rval MSG301}" + "\n(評定段階数)");
            return false;
        }
        if (document.forms[0].notKakuteiFlg.value == "1") {
            alert("{rval MSG203}" + "\n評定段階数が確定されていません。");
            return false;
        }
        var maxLevel = Number(document.forms[0].MAX_POINTLEVEL.value);
        for (var i = maxLevel; i > 0; i--) {
            console.log("POINTHIGH_" + i);
            var pointHighDiv = document.getElementById("POINTHIGH_" + i);
            console.log(pointHighDiv);
            var pointHigh = Number(pointHighDiv.textContent);
            console.log(pointHigh);
            var hidPointHigh = document.forms[0]["HID_POINTHIGH" + i];
            hidPointHigh.value = pointHigh;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function level(obj) {
    var convertedVal = Number(obj.value);
    var hidMaxLevel = document.forms[0].HID_MAX_POINTLEVEL;

    //値に変更がないのでチェックしない
    console.log(convertedVal);
    console.log(hidMaxLevel.value);
    console.log(document.forms[0].notKakuteiFlg.value);

    if (convertedVal === Number(hidMaxLevel.value)) {
        console.log(convertedVal);
        console.log(hidMaxLevel.value);
        return false;
    }

    //空文字チェック
    if (obj.value == '') {
        alert("入力された値は不正な文字列です。\n数値を入力してください。\n。");
        obj.value = hidMaxLevel.value;
        obj.focus();
        return false;
    }
    //数値チェック
    if (obj.value != convertedVal) {
        alert("入力された値は不正な文字列です。\n数値を入力してください。\n入力された文字列は削除されます。");
        obj.value = hidMaxLevel.value;
        obj.focus();
        return false;
    }
    //範囲チェック
    if (obj.value < 0) {
        alert('{rval MSG901}'+'\n評価段階数は0以上の値を入力してください。\n入力された文字列は削除されます。');
        obj.value = hidMaxLevel.value;
        obj.focus();
        return false;
    }
    if (obj.value > 100) {
        alert('{rval MSG901}'+'\n評価段階数は100を超えてはいけません。\n入力された文字列は削除されます。');
        obj.value = hidMaxLevel.value;
        obj.focus();
        return false;
    } 

    //チェックを全て通過した場合はhiddenに保持
    document.forms[0].HID_MAX_POINTLEVEL.value = convertedVal;
    //段階数が変更されたので未確定フラグを立てる
    document.forms[0].notKakuteiFlg.value = 1;

    return false;
}

function inputRange(obj, pointLevel) {
    var convertedVal = Number(obj.value);
    var nextPointLevel = pointLevel - 1;
    var nextPointHighDiv = document.getElementById("POINTHIGH_" + nextPointLevel);

    //空文字チェック
    if (obj.value == '') {
        console.log("POINTHIGH_" + nextPointLevel);
        nextPointHighDiv.textContent = '';
        return false;
    }
    //数値チェック
    if (obj.value != convertedVal) {
        alert("入力された値は不正な文字列です。\n数値を入力してください。\n入力された文字列は削除されます。");
        obj.value = '';
        nextPointHighDiv.textContent = '';
        obj.focus();
        return false;
    }

    //上限・下限チェック
    var maxLevel = document.forms[0].MAX_POINTLEVEL.value;
    var currentPointHighDiv = document.getElementById("POINTHIGH_" + pointLevel);
    var currentPointHigh = currentPointHighDiv.textContent;
    //1番上の段以外は上限値チェック
    if (pointLevel != maxLevel) {
        //現在の段に上限値の設定がある場合、その値以下かチェック
        if (currentPointHigh != '') {
            if (convertedVal > currentPointHigh) {
                alert('{rval MSG901}'+'\n上限値を超えています。\n入力された文字列は削除されます。');
                obj.value = '';
                nextPointHighDiv.textContent = '';
                obj.focus();
                return false;
            }
        } else { 
           //現在の段より上段の下限値より小さい値かチェック
           for (var i = 1; i <= maxLevel - pointLevel; i++) {
                var uePointLow = document.forms[0]["POINTLOW" + (Number(pointLevel) + i)].value;
                if (uePointLow != '' && convertedVal >= uePointLow) {
                    alert('{rval MSG901}'+'\n上段の下限値より小さい値を入力してください。\n入力された文字列は削除されます。');
                    obj.value = '';
                    nextPointHighDiv.textContent = '';
                    obj.focus();
                    return false;
                }
           } 
        }
    }

    //1番下の段以外は上限値チェック
    for (var i = pointLevel - 1; i > 0; i--) {
        var sitaPointHigh = document.getElementById("POINTHIGH_" + i).textContent;
        var sitaPointLow  = document.forms[0]["POINTLOW" + i].value;

        if (sitaPointLow != '') {
            //下段の下限値より大きい値かチェック
            if (convertedVal <= sitaPointLow) {
                alert('{rval MSG901}'+'\n下段の下限値より大きい値を入力してください。\n入力された文字列は削除されます。');
                obj.value = '';
                nextPointHighDiv.textContent = '';
                obj.focus();
                return false;
            }
        }
    }

    //範囲チェック
    var rangeFrom = Number(document.forms[0].rangeFrom.value);
    var rangeTo = Number(document.forms[0].rangeTo.value);
    if (!(rangeFrom <= convertedVal && convertedVal <= rangeTo)) {
        alert('{rval MSG913}'+'\n下限・上限は' + rangeFrom + '以上' + rangeTo + '以下の値を入力してください。\n入力された文字列は削除されます。');
        obj.value = '';
        nextPointHighDiv.textContent = '';
        obj.focus();
        return false;
    }

    //下段の上限値設定
    if (nextPointLevel > 0) {
        var nextPointHighDiv = document.getElementById("POINTHIGH_" + nextPointLevel);
        if (convertedVal - 1 < rangeFrom) {
            nextPointHighDiv.textContent = rangeFrom;
        } else {
            nextPointHighDiv.textContent = convertedVal - 1;
        }
    }

    return false;
}

function ShowConfirm(){
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
