function btn_submit(cmd) {
    if (cmd == 'perfectReset') {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
        if (document.forms[0].MANTENDIV.value == '2') {
            //※取消後はテーブルにある件数を段階数とするので、入力欄にある段階数は消しておく
            document.forms[0].MAX_POINTLEVEL.value = '';
        }
    }

    if (cmd != 'mantendivChange') {
        if (document.forms[0].MANTENDIV.value == '2') {
            if (cmd == 'perfectChange') {
                //※コンボ切替時は段階数を初期化
                document.forms[0].MAX_POINTLEVEL.value = '';
            }
            var maxLevel = document.forms[0].MAX_POINTLEVEL.value;
            if (cmd == 'perfectKakutei') {
                //確定したのでフラグに0を入れる
                document.forms[0].notKakuteiFlg.value = '0';
            }
        }

        if (cmd == 'perfectUpd') {
            var maxLevel = 1;

            if (document.forms[0].MANTENDIV.value == '2') {
                if (document.forms[0].MAX_POINTLEVEL.value == '' || document.forms[0].MAX_POINTLEVEL.value == '0') {
                    alert('{rval MSG301}' + '\n(評価段階数)');
                    return false;
                }
                if (document.forms[0].notKakuteiFlg.value == '1') {
                    alert('{rval MSG203}' + '\n評価段階数が確定されていません。');
                    return false;
                }
                maxLevel = Number(document.forms[0].MAX_POINTLEVEL.value);
            }

            errorFlag = false;
            for (var i = 1; i <= maxLevel; i++) {
                if (document.forms[0].MANTENDIV.value == '2') {
                    if (document.getElementById('LABEL' + i).value == '') {
                        console.log('LABEL' + i);
                        errorFlag = true;
                    }
                }
                if (document.getElementById('VALUE' + i).value == '') {
                    console.log('VALUE' + i + ', ' + document.getElementById('VALUE' + i).value);
                    errorFlag = true;
                }
            }

            if (errorFlag) {
                alert('{rval MSG301}');
                return false;
            }
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
    if (convertedVal === Number(hidMaxLevel.value)) {
        return false;
    }

    //空文字チェック
    if (obj.value == '') {
        alert('入力された値は不正な文字列です。\n数値を入力してください。');
        obj.value = hidMaxLevel.value;
        obj.focus();
        return false;
    }
    //数値チェック
    if (obj.value != convertedVal) {
        alert('入力された値は不正な文字列です。\n数値を入力してください。\n入力された文字列は削除されます。');
        obj.value = hidMaxLevel.value;
        obj.focus();
        return false;
    }
    //範囲チェック
    if (obj.value < 0) {
        alert('{rval MSG901}' + '\n評価段階数は0以上の値を入力してください。\n入力された文字列は削除されます。');
        obj.value = hidMaxLevel.value;
        obj.focus();
        return false;
    }
    if (obj.value > 100) {
        alert('{rval MSG901}' + '\n評価段階数は100を超えてはいけません。\n入力された文字列は削除されます。');
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
