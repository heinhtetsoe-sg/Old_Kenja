function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            //更新中の画面ロック
            updateFrameLock()
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//評価チェック
function valueCheck(getvalue, data) {
    var setNameArray = data.split("-");
    var setName = setNameArray[0];//SCORE_PASS
    var counter = setNameArray[1];
    var dataCount = document.forms[0].COUNTESR_SUM.value;
    //補充点コンボの値getvalue;
    //ライン得点に表示するためのID
    var checkValue = document.getElementById(setName + "_ID_" + counter);
    //値チェック
    var checkFlg = false;
    for (i = 0; i < parseInt(dataCount); i++) {
        //値をチェック
        if (getvalue === '1') {
            document.forms[0][setName + "-" + counter].value = document.forms[0].GET_LINE_SCORE.value;
            checkValue.innerHTML = document.forms[0].GET_LINE_SCORE.value;
            return true;
        } else if (getvalue === '2') {
            document.forms[0][setName + "-" + counter].value = document.forms[0]["SCORE-" + counter].value;
            checkValue.innerHTML = '<font color="red">' + String(document.forms[0]["SCORE-" + counter].value) + '</font>';
            return true;
        }
    }
    if (checkFlg == false) {
        document.forms[0][setName + "-" + counter].value = "";
        checkValue.innerHTML = '';
    }
    return;
}
