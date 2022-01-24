function btn_submit(cmd) {
    //成績優良者
    if (cmd == "csv1") {
        if (document.forms[0].SEISEKI_YURYOU_HYOUTEI.value == "") {
            alert('{rval MSG916}');
            return false;
        }
        if (document.forms[0].GAKKI2.value == '9') {
            if (document.forms[0].SEISEKI_YURYOU_HYOUTEI.value < 1 || document.forms[0].SEISEKI_YURYOU_HYOUTEI.value > 5) {
                alert('{rval MSG916}\n1 ～ 5');
                return false;
            }
        }
    }

    //成績不振者
    if (cmd == "csv2") {
        if (!document.forms[0].KYOUKA_SOUGOU1.checked && !document.forms[0].KYOUKA_SOUGOU2.checked) {
            alert('「教科・科目」または「総合的な時間」どちらかを選択して下さい。');
            return false;
        }

        var seiseki_husin_hyoutei_from = document.forms[0].SEISEKI_HUSIN_HYOUTEI_FROM;
        var seiseki_husin_hyoutei_to   = document.forms[0].SEISEKI_HUSIN_HYOUTEI_TO;

        if (document.forms[0].SEISEKI_HUSIN1.checked) {
            if (document.forms[0].GAKKI2.value == '9') {
                if ((seiseki_husin_hyoutei_from.value < 0 || seiseki_husin_hyoutei_from.value > 5) && seiseki_husin_hyoutei_from.value != '') {
                    alert('{rval MSG916}\n0 ～ 5');
                    return false;
                }
                if ((seiseki_husin_hyoutei_to.value < 0 || seiseki_husin_hyoutei_to.value > 5) && seiseki_husin_hyoutei_to.value != '') {
                    alert('{rval MSG916}\n0 ～ 5');
                    return false;
                }
            }

            if (seiseki_husin_hyoutei_from.value != '' && seiseki_husin_hyoutei_to.value == '') {
                if (document.forms[0].GAKKI2.value == '9') {
                    seiseki_husin_hyoutei_to.value = 5;
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

    //出欠状況
    if (cmd == "csv4") {
        for (i = 0; i < document.forms[0].SYUKKETU_JOUKYOU.length; i++) {
            if (document.forms[0].SYUKKETU_JOUKYOU[i].checked) {
                if (document.forms[0].SYUKKETU_JOUKYOU[i].value == '1') {
                    if (!document.forms[0].SYUKKETU_SYUKKETU_TIKOKU.value &&
                        !document.forms[0].SYUKKETU_SYUKKETU_SOUTAI.value &&
                        !document.forms[0].SYUKKETU_SYUKKETU_KESSEKI.value)
                    {
                        alert('{rval MSG916}');
                        return false;
                    }
                } else if (document.forms[0].SYUKKETU_JOUKYOU[i].value == '2') {
                    if (!document.forms[0].SYUKKETU_KYOUKA_TIKOKU.value &&
                        !document.forms[0].SYUKKETU_KYOUKA_SOUTAI.value &&
                        !document.forms[0].SYUKKETU_KYOUKA_KEKKA.value)
                    {
                        alert('{rval MSG916}');
                        return false;
                    }
                } else if (document.forms[0].SYUKKETU_JOUKYOU[i].value == '3') {
                    if (!document.forms[0].SYUKKETU_IGAI_TIKOKU.value &&
                        !document.forms[0].SYUKKETU_IGAI_SOUTAI.value &&
                        !document.forms[0].SYUKKETU_IGAI_KEKKA.value)
                    {
                        alert('{rval MSG916}');
                        return false;
                    }
                }
            }
        }
    }

    //皆勤者
    if (cmd == "csv5") {
        for (i = 0; i < document.forms[0].KAIKINSYA.length; i++) {
            if (document.forms[0].KAIKINSYA[i].checked) {
                if (document.forms[0].KAIKINSYA[i].value == '1') {
                    if (!document.forms[0].KAIKIN_KAIKIN_TIKOKU.value &&
                        !document.forms[0].KAIKIN_KAIKIN_SOUTAI.value &&
                        !document.forms[0].KAIKIN_KAIKIN_KESSEKI.value &&
                        !document.forms[0].KAIKIN_KAIKIN_KEKKA.value &&
                        !document.forms[0].KAIKIN_KAIKIN_JUGYO_TIKOKU.value &&
                        !document.forms[0].KAIKIN_KAIKIN_JUGYO_SOUTAI.value)
                    {
                        alert('{rval MSG916}');
                        return false;
                    }
                } else if (document.forms[0].KAIKINSYA[i].value == '2') {
                    if (!document.forms[0].KAIKIN_SEIKIN_TIKOKU.value &&
                        !document.forms[0].KAIKIN_SEIKIN_SOUTAI.value &&
                        !document.forms[0].KAIKIN_SEIKIN_KESSEKI.value &&
                        !document.forms[0].KAIKIN_SEIKIN_KEKKA.value &&
                        !document.forms[0].KAIKIN_SEIKIN_JUGYO_TIKOKU.value &&
                        !document.forms[0].KAIKIN_SEIKIN_JUGYO_SOUTAI.value &&
                        !document.forms[0].KAIKIN_SEIKIN_SHR_TIKOKU.value &&
                        !document.forms[0].KAIKIN_SEIKIN_SHR_SOUTAI.value)
                    {
                        alert('{rval MSG916}');
                        return false;
                    }
                }
            }
        }
    }

    if(cmd == "csv1" || cmd == "csv2" || cmd == "csv3" || cmd == "csv4" || cmd == "csv5") {
        if (document.forms[0].DATE.value < document.forms[0].SDATE.value ||
            document.forms[0].DATE.value > document.forms[0].EDATE.value) {
            alert("日付が学期範囲外です。");
            return;
        }
    }


    if (document.forms[0].DATE.value == "") {
        alert('日付を指定してください。');
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function useOption() {
    if (document.forms[0].SYUKKETU_SYUKEI[0].checked){
        document.forms[0].SEISEKI_HUSIN2.disabled = true;
        document.forms[0].SEISEKI_HUSIN3.disabled = true;
        document.forms[0].btn_csv2_1.disabled = true;
        document.forms[0].ZAISEKI_ALL.disabled = true;
    }else {
        document.forms[0].SEISEKI_HUSIN2.disabled = false;
        document.forms[0].SEISEKI_HUSIN3.disabled = false;
        document.forms[0].btn_csv2_1.disabled = false;
        document.forms[0].ZAISEKI_ALL.disabled = false;
    }
}
