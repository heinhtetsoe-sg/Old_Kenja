function btn_submit(cmd) {
    var nyuryoku_flg = false;
    if (cmd == "main") {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
    if (document.forms[0].JUGYOU_JISU_FLG.value == "") {
        for (var i = 0; i < document.forms[0].length; i++) {
            targetObj = document.forms[0][i];
            if (targetObj.name.match(/(RISYU|SYUTOKU)_(BUNSI|BUNBO)/)) {
                if (targetObj.value.length > 0) {
                    nyuryoku_flg = true;
                }
            }
            if (targetObj.name.match(/JITU_(JIFUN|SYUSU)/)) {
                if (!targetObj.value) {
                    alert('{rval MSG301}');
                    return false;
                }
            }
        }
    } else {
        for (var i = 0; i < document.forms[0].length; i++) {
            targetObj = document.forms[0][i];
            if (targetObj.name.match(/(RISYU_BUNSI|RISYU_BUNBO|SYUTOKU_BUNSI|SYUTOKU_BUNBO|RISYU_BUNSI_SPECIAL|RISYU_BUNBO_SPECIAL|JITU_JIFUN|JITU_JIFUN_SPECIAL|JITU_SYUSU|PREF_CD)/)) {
                if (!targetObj.value) {
                    alert('{rval MSG301}');
                    return false;
                }
            }
        }
    }

    for (var i = 0; i < document.forms[0].length; i++) {
        targetObj = document.forms[0][i];
        if (targetObj.name.match(/(RISYU_BUNSI|RISYU_BUNBO|SYUTOKU_BUNSI|SYUTOKU_BUNBO|RISYU_BUNSI_SPECIAL|RISYU_BUNBO_SPECIAL|JITU_JIFUN|JITU_JIFUN_SPECIAL|JITU_SYUSU|PREF_CD|KESSEKI_OUT_BUNSI|KESSEKI_OUT_BUNBO)/)) {
            if (targetObj.value == '0') {
                alert('{rval MSG901}\n0は入力できません。');
                return false;
            }
        }
    }

    var risyu_bunsi   = document.forms[0].RISYU_BUNSI;
    var risyu_bunbo   = document.forms[0].RISYU_BUNBO;
    var syutoku_bunsi = document.forms[0].SYUTOKU_BUNSI;
    var syutoku_bunbo = document.forms[0].SYUTOKU_BUNBO;

    var risyu_bunsi_special = document.forms[0].RISYU_BUNSI_SPECIAL;
    var risyu_bunbo_special = document.forms[0].RISYU_BUNBO_SPECIAL;

    var kesseki_warn_bunsi = document.forms[0].KESSEKI_WARN_BUNSI;
    var kesseki_warn_bunbo = document.forms[0].KESSEKI_WARN_BUNBO;
    var kesseki_out_bunsi  = document.forms[0].KESSEKI_OUT_BUNSI;
    var kesseki_out_bunbo  = document.forms[0].KESSEKI_OUT_BUNBO;

    if (nyuryoku_flg) {
        if (!confirm('上限値が入力されています。\n授業時数管理区分を設定していない時は空になります。')) {
            return false;
        } else {
            risyu_bunsi.value = '';
            risyu_bunbo.value = '';
            syutoku_bunsi.value = '';
            syutoku_bunbo.value = '';
            risyu_bunsi_special.value = '';
            risyu_bunbo_special.value = '';
        }
    }

    if (parseInt(risyu_bunsi.value) > parseInt(risyu_bunbo.value)) {
        alert('分母より大きい分子があります。');
        return false;
    }

    if (parseInt(syutoku_bunsi.value) > parseInt(syutoku_bunbo.value)) {
        alert('分母より大きい分子があります。');
        return false;
    }

    if (parseInt(risyu_bunsi_special.value) > parseInt(risyu_bunbo_special.value)) {
        alert('分母より大きい分子があります。');
        return false;
    }


    if ((kesseki_warn_bunsi.value || kesseki_warn_bunbo.value) && (!kesseki_warn_bunsi.value || !kesseki_warn_bunbo.value)) {
        alert('分母または分子が入力されていません。');
        return false;
    }

    if ((kesseki_out_bunsi.value || kesseki_out_bunbo.value) && (!kesseki_out_bunsi.value || !kesseki_out_bunbo.value)) {
        alert('分母または分子が入力されていません。');
        return false;
    }

    if (kesseki_warn_bunsi.value && kesseki_warn_bunbo.value) {
        if (parseInt(kesseki_warn_bunsi.value) > parseInt(kesseki_warn_bunbo.value)) {
            alert('分母より大きい分子があります。');
            return false;
        }
    }

    if (kesseki_out_bunsi.value && kesseki_out_bunbo.value) {
        if (parseInt(kesseki_out_bunsi.value) > parseInt(kesseki_out_bunbo.value)) {
            alert('分母より大きい分子があります。');
            return false;
        }
    }

    if (kesseki_warn_bunsi.value && kesseki_warn_bunbo.value && kesseki_out_bunsi.value && kesseki_out_bunbo.value) {
        kesseki_warn = parseInt(kesseki_warn_bunsi.value) / parseInt(kesseki_warn_bunbo.value);
        kesseki_out  = parseInt(kesseki_out_bunsi.value)  / parseInt(kesseki_out_bunbo.value);
        if (kesseki_warn > kesseki_out) {
            alert('欠席日数超過より欠席日数注意が大きくなっています。');
            return false;
        }
    }

    risyu_jougen   = parseInt(risyu_bunsi.value)   / parseInt(risyu_bunbo.value);
    syutoku_jougen = parseInt(syutoku_bunsi.value) / parseInt(syutoku_bunbo.value);

    if (syutoku_jougen > risyu_jougen) {
        alert('履修上限値より修得上限値が大きくなっています。');
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_jisuchange(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if(confirm('{rval MSG107}')) {
        document.forms[0].cmd.value = "main";
        document.forms[0].submit();
    }
    return false;
}

function closing_window() {
    alert('{rval MSG300}');
    closeWin();
    return true;
}
