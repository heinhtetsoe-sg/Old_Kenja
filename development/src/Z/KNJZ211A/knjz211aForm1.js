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

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//上限値自動計算
function isNumb(that,level){
    var num;
    var anser;

    that.value = toNumber(that.value);

    if(that.value <= 0){
        return;
    }else{
        anser = (that.value - 1);
        document.getElementById('ASSESSHIGH_ID' + level).innerHTML = anser;
        AssesslowObject  = eval("document.forms[0].Assesshighvalue" + level);
        AssesslowObject.value  = anser;        
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
