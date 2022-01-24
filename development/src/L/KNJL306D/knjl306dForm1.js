function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    //確定
    if (cmd == 'kakutei') {
        if (!(document.forms[0].MAX_ASSESSLEVEL.value > 0)) {
            alert('{rval MSG916}\n( 評定段階数に1以上を指定してください。 )');
            return false;
        }
    }

    //次年度コピー
    if (cmd == 'copy') {
        var value = eval(document.forms[0].year.value) + 1;
        var message = document.forms[0].year.value + '年度のデータから、' + value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//上限値自動計算
function isNumb(obj,level) {
    var answer;

    obj.value = toNumber(obj.value);

    if (obj.value <= 0) {
        return;
    } else {
        answer = (obj.value - 1);
        document.getElementById('ASSESSHIGH_ID' + level).innerHTML = answer;
        AssesslowObject  = eval("document.forms[0].Assesshighvalue" + level);
        AssesslowObject.value  = answer;
    }
    return;
}

//値チェック
    function NumCheck(num) {
    //数値チェック
    num = toInteger(num);

    //範囲チェック
    if (num.length > 0 && num == 0) {
        alert('{rval MSG916}\n( 評定段階数に1以上を指定してください。 )');
        num = '';
    }

    return num;
}
