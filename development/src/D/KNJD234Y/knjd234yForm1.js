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

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
