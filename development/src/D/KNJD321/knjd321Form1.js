function btn_submit(cmd) {
    if (cmd == 'read') {
        if (!document.forms[0].KYOUKA_SOUGOU1.checked && !document.forms[0].KYOUKA_SOUGOU2.checked) {
            alert('「教科・科目」または「総合的な時間」どちらかを選択して下さい。');
            return false;
        }
        if (!document.forms[0].SEISEKI_HUSIN1.checked && !document.forms[0].SEISEKI_HUSIN2.checked && !document.forms[0].SEISEKI_HUSIN3.checked) {
            alert('チェックボックスを選択して下さい。');
            return false;
        }

        var seiseki_husin_hyoutei_from = document.forms[0].SEISEKI_HUSIN_HYOUTEI_FROM;
        var seiseki_husin_hyoutei_to   = document.forms[0].SEISEKI_HUSIN_HYOUTEI_TO;

        if (document.forms[0].SEISEKI_HUSIN1.checked) {
            if (document.forms[0].GAKKI2.value == '9') {
                if ((seiseki_husin_hyoutei_from.value < 0 || seiseki_husin_hyoutei_from.value > 1) && seiseki_husin_hyoutei_from.value != '') {
                    alert('{rval MSG916}\n0 ～ 1');
                    return false;
                }
                if ((seiseki_husin_hyoutei_to.value < 0 || seiseki_husin_hyoutei_to.value > 1) && seiseki_husin_hyoutei_to.value != '') {
                    alert('{rval MSG916}\n0 ～ 1');
                    return false;
                }
            }

            if (seiseki_husin_hyoutei_from.value != '' && seiseki_husin_hyoutei_to.value == '') {
                if (document.forms[0].GAKKI2.value == '9') {
                    seiseki_husin_hyoutei_to.value = 1;
                } else {
                    seiseki_husin_hyoutei_to.value = 999;
                }
            }

            if (seiseki_husin_hyoutei_from.value != '' && seiseki_husin_hyoutei_to.value != '') {
                if (seiseki_husin_hyoutei_from.value > seiseki_husin_hyoutei_to.value) {
                    alert('{rval MSG916}');
                    return false;
                }
            }
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//学年平均のチェック
function calc2(obj,counter)
{
    if(obj.value == ""){
        return;
    }

    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        document.all['ASSESS_ID'+counter].innerHTML = "";
        return;
    }

    var score = parseInt(obj.value,10);
    if(score != 11 && score != 22 && score != 33 && score != 0){
        alert('{rval MSG914}'+'11,22,33,0で入力してください。');
        obj.value = obj.defaultValue;
        document.all['ASSESS_ID'+counter].innerHTML = "";
        return;
    }
}

//文字評定の置き換え
function SetAssess(that,counter,amark,alow,ahigh)
{
    var val  = parseInt(that.value,10);
    var mark = amark.split(",");
    var low  = alow.split(",");
    var high = ahigh.split(",");
    for (i=0; i<mark.length; i++) {
        if(val >= parseInt(low[i],10) && val <= parseInt(high[i],10)) {
           document.all['ASSESS_ID'+counter].innerHTML = mark[i];
           return;
        }
    }
    return;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
